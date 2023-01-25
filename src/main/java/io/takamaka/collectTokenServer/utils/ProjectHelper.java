/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package io.takamaka.collectTokenServer.utils;

import io.takamaka.wallet.utils.TkmTextUtils;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.stream.IntStream;

/**
 *
 * @author isacco
 */
public class ProjectHelper {

    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    
    public static final String doPost(String passedUrl, String key, String param) throws MalformedURLException, ProtocolException, IOException {
        String r = null;
        URL url = new URL(passedUrl);
        HttpURLConnection http = (HttpURLConnection) url.openConnection();
        http.setRequestMethod("POST");
        http.setDoOutput(true);
        http.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

        if (!TkmTextUtils.isNullOrBlank(key) && !TkmTextUtils.isNullOrBlank(param)) {
            String data = key + "=" + param;

            byte[] out = data.getBytes(StandardCharsets.UTF_8);

            OutputStream stream = http.getOutputStream();
            stream.write(out);
        }

        int status = http.getResponseCode();

        switch (status) {
            case 200:
            case 201:
                BufferedReader br = new BufferedReader(new InputStreamReader(http.getInputStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line + "\n");
                }
                br.close();
                r = sb.toString();
                break;
            default:
                return null;
        }

        http.disconnect();

        return r;
    }
    
    public static int getRandomNumberInRange(int min, int max) {

        if (min >= max) {
            throw new IllegalArgumentException("max must be greater than min");
        }

        Random r = new Random();
        return r.nextInt((max - min) + 1) + min;
    }

    public static final String fromBytesToHexString(byte[] bytes) {
        int lnC = bytes.length;
        char[] cRes = new char[lnC * 2];
        IntStream.range(0, lnC).parallel().forEach(i -> {
            int v = bytes[i] & 0xFF;
            cRes[i * 2] = HEX_ARRAY[v >>> 4];
            cRes[i * 2 + 1] = HEX_ARRAY[v & 0x0F];
        });
        return String.valueOf(cRes);
    }
}
