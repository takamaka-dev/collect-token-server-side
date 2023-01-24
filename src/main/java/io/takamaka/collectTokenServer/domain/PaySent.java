/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.collectTokenServer.domain;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;

/**
 *
 * @author isacco
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table("pay_sent")
public class PaySent implements Persistable<String>{
    
    @Id
    String walletAddress;
    
    @Transient
    private boolean newPaySent = false;

    @Override
    public String getId() {
        return walletAddress;
    }

    @Override
    public boolean isNew() {
        return newPaySent;
    }
    
}
