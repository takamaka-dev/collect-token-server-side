/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.collectTokenServer.router;

import io.takamaka.collectTokenServer.handler.CollectTokenServerHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import static org.springframework.web.reactive.function.server.RequestPredicates.path;
import org.springframework.web.reactive.function.server.RouterFunction;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import org.springframework.web.reactive.function.server.ServerResponse;

/**
 *
 * @author isacco
 */
@Slf4j
@Configuration(proxyBeanMethods = false)
public class CollectTokenServerRouter {
    @Bean
    public RouterFunction<ServerResponse> transactionDetailsEndpoint(CollectTokenServerHandler collectTokenHandler) {
        return route()
                .nest(path("/helloworld"),
                        (builder) -> {
                            builder
                                    .GET("", (request) -> collectTokenHandler.helloworld(request));
                        }).build();

    }
    
    @Bean
    public RouterFunction<ServerResponse> requireChallenge(CollectTokenServerHandler collectTokenHandler) {
        return route()
                .nest(path("/requirechallenge"),
                        (builder) -> {
                            builder
                                    .POST("", (request) -> collectTokenHandler.requireChallenge(request));
                        }).build();

    }
    
    @Bean
    public RouterFunction<ServerResponse> getHexTrx(CollectTokenServerHandler collectTokenHandler) {
        return route()
                .nest(path("/gethextrx"),
                        (builder) -> {
                            builder
                                    .POST("", (request) -> collectTokenHandler.getHexTrx(request));
                        }).build();

    }
    
    @Bean
    public RouterFunction<ServerResponse> checkClamingSolutions(CollectTokenServerHandler collectTokenHandler) {
        return route()
                .nest(path("/checkclamingsolutions"),
                        (builder) -> {
                            builder
                                    .POST("", (request) -> collectTokenHandler.checkClamingSolutions(request));
                        }).build();

    }
    
    
    @Bean
    public RouterFunction<ServerResponse> checkResult(CollectTokenServerHandler collectTokenHandler) {
        return route()
                .nest(path("/checkresult"),
                        (builder) -> {
                            builder
                                    .POST("", (request) -> collectTokenHandler.checkResult(request));
                        }).build();

    }
    
    @Bean
    public RouterFunction<ServerResponse> doPendingPay(CollectTokenServerHandler collectTokenHandler) {
        return route()
                .nest(path("/dopendingpay"),
                        (builder) -> {
                            builder
                                    .POST("", (request) -> collectTokenHandler.doPendingPay(request));
                        }).build();

    }
    
    @Bean
    public RouterFunction<ServerResponse> savePayToDo(CollectTokenServerHandler collectTokenHandler) {
        return route()
                .nest(path("/savepaytodo"),
                        (builder) -> {
                            builder
                                    .POST("", (request) -> collectTokenHandler.savePayToDo(request));
                        }).build();

    }
    
    @Bean
    public RouterFunction<ServerResponse> updateClamingSolutions(CollectTokenServerHandler collectTokenHandler) {
        return route()
                .nest(path("/updateclaimsolutions"),
                        (builder) -> {
                            builder
                                    .POST("", (request) -> collectTokenHandler.updateClamingSolutions(request));
                        }).build();

    }
    
}
