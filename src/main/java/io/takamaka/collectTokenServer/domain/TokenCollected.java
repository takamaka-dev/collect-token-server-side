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
@Table("token_collected")
public class TokenCollected implements Persistable<String> {

    @Id
    String walletAddress;
    @Id
    Integer challengeId;
    @Transient
    private boolean newTokenCollected = false;

    @Override
    public String getId() {
        return walletAddress + "-" + challengeId;
    }

    @Override
    public boolean isNew() {
        return newTokenCollected;
    }

}
