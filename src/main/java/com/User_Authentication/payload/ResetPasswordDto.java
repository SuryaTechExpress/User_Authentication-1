package com.User_Authentication.payload;

import lombok.Data;

@Data
public class ResetPasswordDto {
    private String token;
    private String newPassword;
}
