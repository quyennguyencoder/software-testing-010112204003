package com.utephonehub.backend.dto.request.auth;

import com.utephonehub.backend.enums.EGender;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RegisterRequest {

    private String username;

    private String fullName;

    private String email;

    private String phoneNumber;

    private EGender gender;

    private LocalDate dateOfBirth;

    private String password;

    private String confirmPassword;
}

