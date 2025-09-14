package com.fc.authservice.dto;

import lombok.Data;

@Data
public class OtpVerificationRequest {
    private String currentEmail;
    private String otp;
    private boolean isUpdateEmail;
    private String newEmail;
}
