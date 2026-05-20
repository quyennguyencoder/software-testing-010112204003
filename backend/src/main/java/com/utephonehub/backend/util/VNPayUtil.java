package com.utephonehub.backend.util;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class VNPayUtil {
    
    /**
     * Generate HMAC SHA512 signature
     */
    public static String hmacSHA512(String key, String data) {
        try {
            Mac hmac512 = Mac.getInstance("HmacSHA512");
            SecretKeySpec secretKey = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA512");
            hmac512.init(secretKey);
            byte[] result = hmac512.doFinal(data.getBytes(StandardCharsets.UTF_8));
            
            StringBuilder sb = new StringBuilder(2 * result.length);
            for (byte b : result) {
                sb.append(String.format("%02x", b & 0xff));
            }
            return sb.toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating HMAC SHA512", e);
        }
    }
    
    /**
     * Build query string from params map and sort by key
     */
    public static String buildQuery(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder query = new StringBuilder();
        Iterator<String> itr = fieldNames.iterator();
        
        while (itr.hasNext()) {
            String fieldName = itr.next();
            String fieldValue = params.get(fieldName);
            
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    query.append(fieldName)
                         .append('=')
                         .append(URLEncoder.encode(fieldValue, StandardCharsets.UTF_8.toString()));
                    
                    if (itr.hasNext()) {
                        query.append('&');
                    }
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            }
        }
        
        return query.toString();
    }
    
    /**
     * Build hash data (WITH URL encoding like VNPay sample code)
     */
    public static String buildHashData(Map<String, String> params) {
        List<String> fieldNames = new ArrayList<>(params.keySet());
        Collections.sort(fieldNames);
        
        StringBuilder hashData = new StringBuilder();
        boolean isFirst = true;
        
        for (String fieldName : fieldNames) {
            String fieldValue = params.get(fieldName);
            
            if (fieldValue != null && !fieldValue.isEmpty()) {
                try {
                    if (!isFirst) {
                        hashData.append('&');
                    }
                    // URL encode both fieldName and fieldValue for hash data
                    hashData.append(URLEncoder.encode(fieldName, StandardCharsets.US_ASCII.toString()));
                    hashData.append('=');
                    hashData.append(URLEncoder.encode(fieldValue, StandardCharsets.US_ASCII.toString()));
                    isFirst = false;
                } catch (UnsupportedEncodingException e) {
                    throw new RuntimeException("Error encoding hash data", e);
                }
            }
        }
        
        return hashData.toString();
    }
    
    /**
     * Get random number for transaction reference
     */
    public static String getRandomNumber(int len) {
        Random rnd = new Random();
        String chars = "0123456789";
        StringBuilder sb = new StringBuilder(len);
        for (int i = 0; i < len; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }
}
