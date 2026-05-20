package com.utephonehub.backend.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Component
public class PasswordEncoder {

    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    public boolean isValidPassword(String password) {
        // Kiểm tra độ mạnh của mật khẩu
        // Ít nhất 8 ký tự, chứa chữ hoa, chữ thường, số
        return password.length() >= 8 &&
                password.matches(".*[A-Z].*") &&
                password.matches(".*[a-z].*") &&
                password.matches(".*\\d.*");
    }
}

