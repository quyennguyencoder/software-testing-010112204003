package com.utephonehub.backend.util;

import org.springframework.stereotype.Component;

import java.util.Random;

@Component
public class OtpGenerator {

    private static final Random random = new Random();

    public String generateOtp() {
        int otp = 100000 + random.nextInt(900000);
        return String.valueOf(otp);
    }

    public boolean isValidOtp(String otp) {
        return otp != null && otp.length() == 6 && otp.matches("\\d{6}");
    }
}

