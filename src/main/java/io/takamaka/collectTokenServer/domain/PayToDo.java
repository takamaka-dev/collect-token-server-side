/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.collectTokenServer.domain;

import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
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
@Table("pay_todo")
public class PayToDo implements Persistable<String> {

    public PayToDo(String walletAddress, String hexTrx, boolean isNew) {
        this.walletAddress = walletAddress;
        this.hexTrx = hexTrx;
        this.newPayToDo = isNew;
    }
    
    
    String hexTrx;
    String walletAddress;
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    Long id;
    
    @Override
    public String getId() {
        return this.id + "";
    }
    
    @Transient
    private boolean newPayToDo = false;

    @Override
    public boolean isNew() {
        return newPayToDo;
    }

}
