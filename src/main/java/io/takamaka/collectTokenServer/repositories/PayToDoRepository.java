/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.takamaka.collectTokenServer.repositories;

import io.takamaka.collectTokenServer.domain.PayToDo;
import io.takamaka.collectTokenServer.domain.TokenCollected;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

/**
 *
 * @author isacco
 */
public interface PayToDoRepository extends ReactiveCrudRepository<PayToDo, Long>{    
    @Query("DELETE FROM pay_todo WHERE id= :id")
    Mono<PayToDo> removePayToDoRows(Integer id);
    
    @Query("SELECT * FROM pay_todo")
    Flux<PayToDo> getAllPayToDo();
    
    @Query("INSERT INTO pay_todo(wallet_address, hex_trx) VALUES(:walletAddress, :hexTrx)")
    Mono<PayToDo> savePayToDo(String walletAddress, String hexTrx);
}
