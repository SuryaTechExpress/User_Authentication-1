package com.User_Authentication.payload;


import lombok.Data;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;
@Data
public class SignupDto {

    @NotBlank(message = "Username is required")
    private String username;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email address")
    private String email;

    @NotBlank(message = "Password is required")
    @Pattern(
            regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,}$",
            message = "Password must contain at least 8 characters, including one uppercase letter, one lowercase letter, one digit, and one special character."
    )
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    private String firstName;

    private String lastName;

    private String captcha;


}

