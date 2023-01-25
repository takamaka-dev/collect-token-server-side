/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.takamaka.collectTokenServer.repositories;

import io.takamaka.collectTokenServer.domain.TokenCollected;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

/**
 *
 * @author isacco
 */
public interface TokenCollectedRepository extends ReactiveCrudRepository<TokenCollected, Long>{
    @Query("SELECT * FROM token_collected WHERE wallet_address= :walletAddress LIMIT 1;")
    Mono<TokenCollected> findByWalletAddress(String walletAddress);
    
    @Query("UPDATE token_collected set pay_sent=true where wallet_address= :walletAddress")
    Mono<TokenCollected> updateClamingSolutions(String walletAddress);
    
    @Query("SELECT MAX(challenge_id) FROM token_collected")
    Mono<Integer> findMaxChallengeIdValue();
    
    @Query("SELECT COUNT(*) as claming_solutions from token_collected where pay_sent = false and wallet_address= :walletAddress")
    Mono<Integer> getClamingSolutions(String wallAddress);
    
    @Query("INSERT INTO token_collected(wallet_address, solution_string, sent_challenge) values(:walletAddress, :solutionString, :sentChallenge)")
    Mono<TokenCollected> saveTokenCollected(String walletAddress, String solutionString, String sentChallenge);
}
