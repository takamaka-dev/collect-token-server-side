/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.collectTokenServer;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.takamaka.collectTokenServer.utils.ErrorMessageBean;
import io.takamaka.wallet.utils.TkmTextUtils;
import java.util.AbstractMap;
import java.util.List;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;

/**
 *
 * @author giovanni
 */
@Slf4j
public class SerialUtils {

    public static final TypeReference<MultiValueMap<String, String>> type_MultiValueMap_String_String = new TypeReference<MultiValueMap<String, String>>() {
    };

    public static final TypeReference<List<String>> type_List_String = new TypeReference<List<String>>() {
    };

    public static final MultiValueMap<String, String> deepCopy(MultiValueMap<String, String> origMap, ErrorMessageBean errorMessageBean) {
        try {
            ObjectMapper m = new ObjectMapper();
            MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
            Set<String> keySet = origMap.keySet();
            for (String key : keySet) {
                List<String> resList = origMap.get(key);
                List<String> readValue = m.readValue(m.writeValueAsString(resList), type_List_String);
                formData.addAll(key, readValue);
            }
            return formData;
        } catch (Exception ex) {
            errorMessageBean.getErrors().add(ex.getLocalizedMessage());
            errorMessageBean.setT(ex);
        }
        return null;
    }

    public static final MultiValueMap<String, String> parseBody(String rawBody, ErrorMessageBean errorMessageBean) {
        MultiValueMap<String, String> formData = new LinkedMultiValueMap<>();
        try {
            log.info("looking for limit value");
            if (TkmTextUtils.isNullOrBlank(rawBody)) {
                log.info("null body");
                errorMessageBean.getErrors().add("null request body");
            }
            String trimmedFlatReq = rawBody.trim();
            if (trimmedFlatReq.contains("\u0000")) {
                log.info("found null character, exiting...");
                errorMessageBean.getErrors().add("found null character, exiting...");
            }
            log.info("the request " + trimmedFlatReq);
            if (!trimmedFlatReq.contains("=")) {
                log.info("missing equal sign");
                errorMessageBean.getErrors().add("missing equal sign");
            }
            if (!errorMessageBean.containErrors()) {
                if (trimmedFlatReq.contains("&")) {
                    String[] split = trimmedFlatReq.split("&");
                    for (String rawParam : split) {
                        AbstractMap.SimpleEntry<String, String> param = parseStringParam(rawParam, errorMessageBean);
                        formData.add(param.getKey(), param.getValue());
                    }
                } else {
                    AbstractMap.SimpleEntry<String, String> param = parseStringParam(trimmedFlatReq, errorMessageBean);
                    formData.add(param.getKey(), param.getValue());
                }

            }
        } catch (Exception e) {
            errorMessageBean.getErrors().add(e.getLocalizedMessage());
            errorMessageBean.setT(e);
        }
        return formData;
    }

    //org.javatuples.
    public static AbstractMap.SimpleEntry<String, String> parseStringParam(String rawEntry, ErrorMessageBean errorMessageBean) {
        AbstractMap.SimpleEntry<String, String> entry;

        String key;
        String val;
        try {
            if (!rawEntry.contains("=")) {
                log.info("missing equal sign");
                errorMessageBean.getErrors().add("missing equal sign");
            } else {
                String[] split = rawEntry.split("=");
                key = split[0];
                val = split[1];
                return new AbstractMap.SimpleEntry<>(key, val);
            }

        } catch (Exception e) {
            errorMessageBean.getErrors().add(e.getLocalizedMessage());
            errorMessageBean.setT(e);
        }
        return null;

    }

    public static Integer parseNamedIntegerParam(String name, String param) {
        try {
            String[] split = param.split("=");
            if (!name.equals(split[0])) {
                return null;
            }
            return Integer.parseInt(split[1]);
        } catch (Exception e) {
            log.info("malformed int param n: " + name + " v: " + param, e);
            return null;
        }

    }

    public static Boolean parseNamedBooleanParam(String name, String param) {
        try {
            String[] split = param.split("=");
            if (!name.equals(split[0])) {
                return null;
            }
            return Boolean.parseBoolean(split[1]);
        } catch (Exception e) {
            log.info("malformed boolean param n: " + name + " v: " + param, e);
            return null;
        }

    }

    public static String parseNamedStringParam(String name, String param) {
        try {
            String[] split = param.split("=");
            if (!name.equals(split[0])) {
                return null;
            }
            return split[1];
        } catch (Exception e) {
            log.info("malformed string param n: " + name + " v: " + param, e);
            return null;
        }

    }

    public static int parseIntParam(String param) {
        String[] split = param.split("=");
        return Integer.parseInt(split[1]);
    }

    public static final void renameParam(MultiValueMap<String, String> mapToBeModified, String originalFieldName, String newFieldName, ErrorMessageBean errorMessageBean) {
        if (mapToBeModified == null) {
            errorMessageBean.getErrors().add("null multi value map");
        } else {
            if (mapToBeModified.containsKey(originalFieldName)) {
                List<String> values = mapToBeModified.get(originalFieldName);
                mapToBeModified.remove(originalFieldName);

                for (String value : values) {
                    mapToBeModified.add(newFieldName, value);
                }

            }
        }
    }

}
