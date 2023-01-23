/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.collectTokenServer.utils;

import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 *
 * @author giovanni
 */
@Slf4j
@Data
@AllArgsConstructor
public class ErrorMessageBean {

    private List<String> errors;
    private Throwable t;

    public ErrorMessageBean() {
        errors = new ArrayList<>();
    }

    public boolean containErrors() {
        return !errors.isEmpty() || t != null;
    }

}
