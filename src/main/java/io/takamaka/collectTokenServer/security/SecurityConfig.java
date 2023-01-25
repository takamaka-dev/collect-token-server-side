/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package io.takamaka.collectTokenServer.security;

import io.takamaka.collectTokenServer.PropUtils;
import io.takamaka.collectTokenServer.PropUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 *
 * @author isacco.borsani
 */
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        http.authorizeExchange()
                .pathMatchers(
                        "/test"
                )
                .authenticated()
                .pathMatchers("/helloworld",
                        "/requirechallenge",
                        "/checkresult",
                        "/checkclamingsolutions",
                        "/claimsolutions"
                )
                .permitAll()
                .and().csrf((csrf) -> csrf.disable())
                .httpBasic();
        return http.build();
    }

    @Bean
    MapReactiveUserDetailsService userDetailService() {
        UserDetails user = User.builder()
                .username("user")
                //password
                .password("{bcrypt}" + PropUtils.i().getTokenServerPassword())
                .roles("USER")
                .build();
        return new MapReactiveUserDetailsService(user);
    }
}
