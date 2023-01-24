/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.collectTokenServer;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UncheckedIOException;
import static java.nio.charset.StandardCharsets.UTF_8;
import java.util.Properties;
import java.util.regex.Pattern;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;

/**
 *
 * @author giovanni
 */
@Slf4j
@Getter
public class PropUtils {

    private final int retries;
    private final String prodApi;
    private final String testApi;
    private final Boolean targetingProd;
    private final String prodBalanceOfApi;
    private final String testBalanceOfApi;
    private final String tokenServerAddressReference;
    private final String tokenServerPassword;
    private final String tokenServerPasswordTest;
    private final String tokenServerWalletName;
    private final String tokenServerWalletPass;
    private final Properties initalSettings;
    private final String tokenServerSecret;
    private final String tokenServerDifficulty;

    public static final String WALLET_PARAM_STRING = "^[0-9a-zA-Z-_.]+$";
    public static final Pattern WALLET_PARAM_PATTERN = Pattern
            .compile(WALLET_PARAM_STRING);
    
    private PropUtils() {
        initalSettings = getPropertyResource("initial_settings.properties");
        String property = initalSettings.getProperty("tkm.client.max-retries");
        retries = Integer.parseInt(property);
        log.info("readed prop " + property);
        prodApi = initalSettings.getProperty("prod.tkm.node.api");
        testApi = initalSettings.getProperty("test.tkm.node.api");
        prodBalanceOfApi = initalSettings.getProperty("prod.tkm.node.api.balanceof");
        testBalanceOfApi = initalSettings.getProperty("test.tkm.node.api.balanceof");
        targetingProd = Boolean.parseBoolean(property);
        tokenServerAddressReference = initalSettings.getProperty("tkm.tokenserver.address.reference");
        tokenServerPassword = initalSettings.getProperty("tkm.tokenserver.password");
        tokenServerPasswordTest = initalSettings.getProperty("tkm.tokenserver.password.test");
        tokenServerWalletName = initalSettings.getProperty("tkm.tokenserver.wallet.name");
        tokenServerWalletPass = initalSettings.getProperty("tkm.tokenserver.wallet.pass");
        tokenServerSecret = initalSettings.getProperty("tkm.server.secret");
        tokenServerDifficulty = initalSettings.getProperty("tkm.server.difficulty");
    }

    private static final class PU {

        public static final PropUtils P = new PropUtils();
    }

    public static PropUtils i() {
        return PU.P;
    }
    
    public String getCurrentApiBase() {
        return targetingProd ? prodApi : testApi;
    }
    
    public String getCurrentApiBalanceOf() {
        return targetingProd ? prodBalanceOfApi : testBalanceOfApi;
    }
    
    public String getTokenServerDifficulty() {
        return tokenServerDifficulty;
    }
    
    public String getTokenServerSecret() {
        return tokenServerSecret;
    }
    
    public String getTokenServerWalletName() {
        return tokenServerWalletName;
    }
    
    public String getTokenServerWalletPass() {
        return tokenServerWalletPass;
    }
    
    public String getTokenServerPassword() {
        return tokenServerPasswordTest;
    }
    
    public String getTokenServerAddressReference() {
        return tokenServerAddressReference;
    }

    public static Properties getPropertyResource(String fileName) {
        log.info("loading resource " + fileName);
        ResourceLoader resourceLoader = new DefaultResourceLoader();
        Resource resource = resourceLoader.getResource(fileName);
        return asProperties(resource);
    }
    
    public static Properties asProperties(Resource resource) {
        try ( Reader reader = new InputStreamReader(resource.getInputStream(), UTF_8)) {
            Properties prop = null;
            prop = new Properties();
            prop.load(reader);
            return prop;
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
