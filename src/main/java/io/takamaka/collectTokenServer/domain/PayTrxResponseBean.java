/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.collectTokenServer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author isacco
 */
@Slf4j
@Data
@AllArgsConstructor
@NoArgsConstructor
public class PayTrxResponseBean {
    
    private String hexTrx;
    private String transactionHash;
    
}
