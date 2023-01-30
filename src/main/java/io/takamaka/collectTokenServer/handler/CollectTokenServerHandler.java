/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.collectTokenServer.handler;

import io.takamaka.collectTokenServer.PropUtils;
import io.takamaka.collectTokenServer.SerialUtils;
import io.takamaka.collectTokenServer.domain.ChallengeResponseBean;
import io.takamaka.collectTokenServer.domain.PayToDo;
import io.takamaka.collectTokenServer.repositories.PayToDoRepository;
import io.takamaka.collectTokenServer.repositories.TokenCollectedRepository;
import io.takamaka.collectTokenServer.utils.ErrorMessageBean;
import io.takamaka.collectTokenServer.utils.ProjectHelper;
import io.takamaka.wallet.InstanceWalletKeyStoreBCED25519;
import io.takamaka.wallet.InstanceWalletKeystoreInterface;
import io.takamaka.wallet.beans.FeeBean;
import io.takamaka.wallet.beans.InternalTransactionBean;
import io.takamaka.wallet.beans.TransactionBean;
import io.takamaka.wallet.beans.TransactionBox;
import io.takamaka.wallet.exceptions.HashAlgorithmNotFoundException;
import io.takamaka.wallet.exceptions.HashEncodeException;
import io.takamaka.wallet.exceptions.HashProviderNotFoundException;
import io.takamaka.wallet.exceptions.UnlockWalletException;
import io.takamaka.wallet.exceptions.WalletException;
import io.takamaka.wallet.utils.BuilderITB;
import static io.takamaka.wallet.utils.DefaultInitParameters.NUMBER_OF_ZEROS;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.logging.Level;
import java.util.logging.Logger;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import io.takamaka.wallet.utils.FixedParameters;
import io.takamaka.wallet.utils.TkmSignUtils;
import io.takamaka.wallet.utils.TkmTK;
import io.takamaka.wallet.utils.TkmTextUtils;
import io.takamaka.wallet.utils.TkmWallet;
import io.takamaka.wallet.utils.TransactionFeeCalculator;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.net.ProtocolException;
import java.security.NoSuchProviderException;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import org.springframework.http.HttpStatus;
import reactor.core.publisher.Flux;

/**
 *
 * @author isacco
 */
@Slf4j
@Component
@AllArgsConstructor
public class CollectTokenServerHandler {

    TokenCollectedRepository tokenCollectedRepository;
    PayToDoRepository payToDoRepository;
    public static final String SOURCE_WALLET_NAME = "my_example_wallet_source";
    public static final String SOURCE_WALLET_PASSWORD = "my_example_wallet_source_password";

    public Mono<ServerResponse> helloworld(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Hello world!");
    }

    public Mono<ServerResponse> doPendingPay(ServerRequest serverRequest) {

        return payToDoRepository.getAllPayToDo().flatMap((singlePayToDo) -> {
            try {
                ProjectHelper.doPost(
                        PropUtils.i().getCurrentApiBase() + "/transaction", // main network verify endpoint (for verify main or test network is the same)
                        "tx", //form var
                        singlePayToDo.getHexTrx());
            } catch (ProtocolException ex) {
                Logger.getLogger(CollectTokenServerHandler.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IOException ex) {
                Logger.getLogger(CollectTokenServerHandler.class.getName()).log(Level.SEVERE, null, ex);
            }
            return payToDoRepository.removePayToDoRows(Integer.valueOf(singlePayToDo.getId()));
        }).collectList().flatMap((collectedList) -> {
            return ServerResponse.ok().bodyValue("OK");
        });

    }

    public Mono<ServerResponse> getHexTrx(ServerRequest serverRequest) {
        ErrorMessageBean errorMessageBean = new ErrorMessageBean();
        return serverRequest.bodyToMono(String.class).flatMap((flatBody) -> {
            if (TkmTextUtils.isNullOrBlank(flatBody)) {
                log.info("null body");
            }
            String trimmedFlatReq = flatBody.trim();
            if (trimmedFlatReq.contains("\u0000")) {
                log.info("found null character, exiting...");
                errorMessageBean.getErrors().add("found null character, exiting...");
            }
            log.info("the request " + trimmedFlatReq);
            MultiValueMap<String, String> resMap = SerialUtils.parseBody(flatBody, errorMessageBean);

            String walletAddress = resMap.getFirst("walletAddress");

            final String trimWalletAddress = walletAddress.trim();
            if (PropUtils.WALLET_PARAM_PATTERN.matcher(trimWalletAddress).find()) {
                walletAddress = trimWalletAddress;
            } else {
                errorMessageBean.getErrors().add("wrong format wallet address");
                log.info("wrong format wallet address");
            }

            return tokenCollectedRepository.getClamingSolutions(walletAddress).flatMap((numberOfSol) -> {

                BigDecimal tkrRewardBase = PropUtils.i().getTkrReward();
                BigDecimal tkgRewardBase = PropUtils.i().getTkgReward();

                double ratioShardCompleted
                        = numberOfSol.doubleValue()
                        / PropUtils.i().getShardsGoal();

                BigDecimal bdRatio = new BigDecimal(ratioShardCompleted);
                BigDecimal tkrAmountBD = tkrRewardBase.multiply(bdRatio).multiply(new BigDecimal(BigInteger.TEN.pow(NUMBER_OF_ZEROS)));
                BigDecimal tkgAmountBD = tkgRewardBase.multiply(bdRatio).multiply(new BigDecimal(BigInteger.TEN.pow(NUMBER_OF_ZEROS)));
                BigInteger tkgAmountBI = tkgAmountBD.toBigInteger();
                BigInteger tkrAmountBI = tkrAmountBD.toBigInteger();

                try {
                    String doPayResultHex = doPay(
                            trimWalletAddress,
                            trimWalletAddress,
                            PropUtils.i().getCurrentApiBase(),
                            tkrAmountBI,
                            tkgAmountBI
                    );

                    return ServerResponse.ok().bodyValue(doPayResultHex);
                } catch (WalletException | IOException ex) {
                    Logger.getLogger(CollectTokenServerHandler.class.getName()).log(Level.SEVERE, null, ex);
                }

                return ServerResponse.ok().bodyValue(numberOfSol.toString());
            });

        });
    }

    public Mono<ServerResponse> updateClamingSolutions(ServerRequest serverRequest) {

        ErrorMessageBean errorMessageBean = new ErrorMessageBean();

        return serverRequest.bodyToMono(String.class).flatMap((flatBody) -> {
            if (TkmTextUtils.isNullOrBlank(flatBody)) {
                log.info("null body");
            }
            String trimmedFlatReq = flatBody.trim();
            if (trimmedFlatReq.contains("\u0000")) {
                log.info("found null character, exiting...");
                errorMessageBean.getErrors().add("found null character, exiting...");
            }
            log.info("the request " + trimmedFlatReq);
            MultiValueMap<String, String> resMap = SerialUtils.parseBody(flatBody, errorMessageBean);

            String walletAddress = resMap.getFirst("walletAddress");

            final String trimWalletAddress = walletAddress.trim();
            if (PropUtils.WALLET_PARAM_PATTERN.matcher(trimWalletAddress).find()) {
                walletAddress = trimWalletAddress;
            } else {
                errorMessageBean.getErrors().add("wrong format wallet address");
                log.info("wrong format wallet address");
            }

            return tokenCollectedRepository.updateClamingSolutions(trimWalletAddress).flatMap((t) -> {
                return ServerResponse.ok().bodyValue("OK");
            });
        });
    }

    public Mono<ServerResponse> savePayToDo(ServerRequest serverRequest) {

        ErrorMessageBean errorMessageBean = new ErrorMessageBean();

        return serverRequest.bodyToMono(String.class).flatMap((flatBody) -> {
            if (TkmTextUtils.isNullOrBlank(flatBody)) {
                log.info("null body");
            }
            String trimmedFlatReq = flatBody.trim();
            if (trimmedFlatReq.contains("\u0000")) {
                log.info("found null character, exiting...");
                errorMessageBean.getErrors().add("found null character, exiting...");
            }
            log.info("the request " + trimmedFlatReq);
            MultiValueMap<String, String> resMap = SerialUtils.parseBody(flatBody, errorMessageBean);

            String walletAddress = resMap.getFirst("walletAddress");

            String doPayResultHex = resMap.getFirst("hex");

            final String trimWalletAddress = walletAddress.trim();
            if (PropUtils.WALLET_PARAM_PATTERN.matcher(trimWalletAddress).find()) {
                walletAddress = trimWalletAddress;
            } else {
                errorMessageBean.getErrors().add("wrong format wallet address");
                log.info("wrong format wallet address");
            }

            return payToDoRepository.savePayToDo(trimWalletAddress, doPayResultHex).flatMap((result) -> {
                return ServerResponse.ok().bodyValue(result.getWalletAddress());
            });
        });

    }

    public Mono<ServerResponse> checkClamingSolutions(ServerRequest serverRequest) {
        ErrorMessageBean errorMessageBean = new ErrorMessageBean();

        return serverRequest.bodyToMono(String.class).flatMap((flatBody) -> {
            if (TkmTextUtils.isNullOrBlank(flatBody)) {
                log.info("null body");
            }
            String trimmedFlatReq = flatBody.trim();
            if (trimmedFlatReq.contains("\u0000")) {
                log.info("found null character, exiting...");
                errorMessageBean.getErrors().add("found null character, exiting...");
            }
            log.info("the request " + trimmedFlatReq);
            MultiValueMap<String, String> resMap = SerialUtils.parseBody(flatBody, errorMessageBean);

            String walletAddress = resMap.getFirst("walletAddress");

            final String trimWalletAddress = walletAddress.trim();
            if (PropUtils.WALLET_PARAM_PATTERN.matcher(trimWalletAddress).find()) {
                walletAddress = trimWalletAddress;
            } else {
                errorMessageBean.getErrors().add("wrong format wallet address");
                log.info("wrong format wallet address");
            }

            return tokenCollectedRepository.getClamingSolutions(walletAddress).flatMap((numberOfSol) -> {
                return ServerResponse.ok().bodyValue(numberOfSol.toString());
            });
        });
    }

    public static final String doPay(
            String fromAddress,
            String toAddress,
            String networkTarget,
            BigInteger tkrAmount,
            BigInteger tkgAmount) throws UnlockWalletException, WalletException, ProtocolException, IOException {
        final InstanceWalletKeystoreInterface iwkEDSource = new InstanceWalletKeyStoreBCED25519(SOURCE_WALLET_NAME, SOURCE_WALLET_PASSWORD);
        final String publicKeySource = iwkEDSource.getPublicKeyAtIndexURL64(0);
        final Date transactionInclusionTime = TkmTK.getTransactionTime();
        InternalTransactionBean payITB = BuilderITB.pay(
                publicKeySource, toAddress,
                tkgAmount, tkrAmount,
                "mining reward for " + toAddress,
                transactionInclusionTime);
        TransactionBean myPayObject
                = TkmWallet.createGenericTransaction(
                        payITB,
                        iwkEDSource, // source wallet 
                        0 // same wallet and KEY INDEX of publicKeySource
                );
        String payTransactionJson = TkmTextUtils.toJson(myPayObject);
        String payHexBody = TkmSignUtils.fromStringToHexString(payTransactionJson);
        return payHexBody;
    }

    public Mono<ServerResponse> checkResult(ServerRequest serverRequest) {
        ErrorMessageBean errorMessageBean = new ErrorMessageBean();

        return serverRequest.bodyToMono(String.class).flatMap((flatBody) -> {
            flatBody = URLDecoder.decode(flatBody, StandardCharsets.UTF_8);
            if (TkmTextUtils.isNullOrBlank(flatBody)) {
                log.info("null body");
            }
            String trimmedFlatReq = flatBody.trim();
            if (trimmedFlatReq.contains("\u0000")) {
                log.info("found null character, exiting...");
                errorMessageBean.getErrors().add("found null character, exiting...");
            }
            log.info("the request " + trimmedFlatReq);
            MultiValueMap<String, String> resMap = SerialUtils.parseBody(flatBody, errorMessageBean);

            String solutionInt = resMap.getFirst("interoSoluzione");
            if (TkmTextUtils.isNullOrBlank(solutionInt)) {
                log.info("solution int is empty");
                errorMessageBean.getErrors().add("solution int is empty");
            }

            String walletAddress = resMap.getFirst("walletAddress");

            final String trimWalletAddress = walletAddress.trim();
            if (PropUtils.WALLET_PARAM_PATTERN.matcher(trimWalletAddress).find()) {
                walletAddress = trimWalletAddress;
            } else {
                errorMessageBean.getErrors().add("wrong format wallet address");
                log.info("wrong format wallet address");
            }

            String challenge = resMap.getFirst("challenge");

            if (TkmTextUtils.isNullOrBlank(challenge)) {
                log.info("challenge is empty");
                errorMessageBean.getErrors().add("challenge is empty");
            }

            if (!errorMessageBean.getErrors().isEmpty()) {
                return ServerResponse.badRequest().build();
            }

            boolean check = false;
            byte[] hash256Byte;
            try {
                hash256Byte = TkmSignUtils.Hash256Byte((solutionInt + challenge).getBytes(), FixedParameters.HASH_256_ALGORITHM);
                String fromBytesToHexString = ProjectHelper.fromBytesToHexString(hash256Byte);
                check = fromBytesToHexString.startsWith(PropUtils.i().getTokenServerDifficulty());
            } catch (NoSuchAlgorithmException | NoSuchProviderException ex) {
                Logger.getLogger(CollectTokenServerHandler.class.getName()).log(Level.SEVERE, null, ex);
            }

            if (check) {
                return tokenCollectedRepository.saveTokenCollected(walletAddress,
                        challenge,
                        solutionInt).flatMap((savedRow) -> {
                            return ServerResponse.ok().bodyValue("OK");
                        });

            }

            return ServerResponse.badRequest().bodyValue("Wrong solution...");
        });

    }

    public Mono<ServerResponse> loadWithChallengeId(int challengeId, String walletAddress) {
        ChallengeResponseBean challengeResponseBean
                = new ChallengeResponseBean(
                        PropUtils.i().getTokenServerDifficulty(),
                        challengeId,
                        ""
                );

        try {
            byte[] passwordDigest = TkmSignUtils.PWHash(
                    PropUtils.i().getTokenServerSecret() + PropUtils.i().getTokenServerDifficulty() + walletAddress, PropUtils.i().getTokenServerSecret(), challengeId, 256);

            String challenge = TkmSignUtils.fromByteArrayToB64URL(passwordDigest);

            challengeResponseBean.setChallenge(challenge);

        } catch (HashEncodeException | HashAlgorithmNotFoundException | HashProviderNotFoundException | InvalidKeySpecException | NoSuchAlgorithmException ex) {
            Logger.getLogger(CollectTokenServerHandler.class.getName()).log(Level.SEVERE, null, ex);
        }

        return ServerResponse.ok().contentType(MediaType.APPLICATION_JSON).bodyValue(challengeResponseBean);
    }

    public Mono<ServerResponse> requireChallenge(ServerRequest serverRequest) {
        ErrorMessageBean errorMessageBean = new ErrorMessageBean();

        String secret = PropUtils.i().getTokenServerSecret();

        return serverRequest.bodyToMono(String.class).flatMap((flatBody) -> {
            flatBody = URLDecoder.decode(flatBody, StandardCharsets.UTF_8);
            if (TkmTextUtils.isNullOrBlank(flatBody)) {
                log.info("null body");
            }
            String trimmedFlatReq = flatBody.trim();
            if (trimmedFlatReq.contains("\u0000")) {
                log.info("found null character, exiting...");
                errorMessageBean.getErrors().add("found null character, exiting...");
            }

            log.info("the request " + trimmedFlatReq);
            MultiValueMap<String, String> resMap = SerialUtils.parseBody(flatBody, errorMessageBean);

            String walletAddress = resMap.getFirst("walletAddress");

            final String trimWalletAddress = walletAddress.trim();
            if (PropUtils.WALLET_PARAM_PATTERN.matcher(trimWalletAddress).find()) {
                walletAddress = trimWalletAddress;
            } else {
                return ServerResponse.badRequest().bodyValue("Bad request");
            }

            return tokenCollectedRepository.findMaxChallengeIdValue().flatMap((single) -> {
                return loadWithChallengeId(single, trimWalletAddress);
            }).switchIfEmpty(loadWithChallengeId(1, trimWalletAddress));
        });

    }

}
