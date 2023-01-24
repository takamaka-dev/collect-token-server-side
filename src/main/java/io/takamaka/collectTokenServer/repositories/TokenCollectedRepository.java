/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package io.takamaka.collectTokenServer.repositories;

import io.takamaka.collectTokenServer.domain.TokenCollected;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;

/**
 *
 * @author isacco
 */
public interface TokenCollectedRepository extends ReactiveCrudRepository<TokenCollected, String>{
    
}
