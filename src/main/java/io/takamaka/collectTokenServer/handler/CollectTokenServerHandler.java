/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.collectTokenServer.handler;

import com.h2tcoin.takamakachain.exceptions.threadSafeUtils.HashAlgorithmNotFoundException;
import com.h2tcoin.takamakachain.exceptions.threadSafeUtils.HashEncodeException;
import com.h2tcoin.takamakachain.exceptions.threadSafeUtils.HashProviderNotFoundException;
import com.h2tcoin.takamakachain.utils.threadSafeUtils.TkmSignUtils;
import com.h2tcoin.takamakachain.utils.threadSafeUtils.TkmTextUtils;
import io.takamaka.collectTokenServer.PropUtils;
import io.takamaka.collectTokenServer.SerialUtils;
import io.takamaka.collectTokenServer.domain.ChallengeResponseBean;
import io.takamaka.collectTokenServer.domain.TokenCollected;
import io.takamaka.collectTokenServer.repositories.TokenCollectedRepository;
import io.takamaka.collectTokenServer.utils.ErrorMessageBean;
import io.takamaka.collectTokenServer.utils.ProjectHelper;
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
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;
import io.takamaka.wallet.utils.FixedParameters;
import java.math.BigInteger;
import java.security.NoSuchProviderException;

/**
 *
 * @author isacco
 */
@Slf4j
@Component
@AllArgsConstructor
public class CollectTokenServerHandler {

    TokenCollectedRepository tokenCollectedRepository;

    public Mono<ServerResponse> helloworld(ServerRequest serverRequest) {
        return ServerResponse.ok()
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("Hello world!");
    }
    
    public Mono<ServerResponse> claimSolutions(ServerRequest serverRequest) {
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
            
            return tokenCollectedRepository.updateClamingSolutions(walletAddress).flatMap((t) -> {
                return ServerResponse.ok().bodyValue("OK");
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
            BigInteger tkgAmount) {
        return "";
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
