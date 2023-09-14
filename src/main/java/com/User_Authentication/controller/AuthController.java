package com.User_Authentication.controller;


import com.User_Authentication.entity.Role;
import com.User_Authentication.entity.User;
import com.User_Authentication.payload.JWTAuthResponse;
import com.User_Authentication.payload.LoginDto;
import com.User_Authentication.payload.ResetPasswordDto;
import com.User_Authentication.payload.SignupDto;
import com.User_Authentication.repository.RoleRepository;
import com.User_Authentication.repository.UserRepository;

import com.User_Authentication.security.JwtTokenProvider;
import com.User_Authentication.util.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;

    private RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService; // Inject the EmailService


    private final JwtTokenProvider jwtTokenProvider;

    private AuthenticationManager authenticationManager;
    @Value("${app.jwt-expiration-milliseconds}")
    private long jwtExpirationInMs;

    @Autowired
    public AuthController(UserRepository userRepository, PasswordEncoder passwordEncoder,RoleRepository roleRepository, EmailService emailService,JwtTokenProvider jwtTokenProvider,AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtTokenProvider=jwtTokenProvider;
        this.authenticationManager=authenticationManager;
        this.roleRepository=roleRepository;
    }

    //http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginDto loginDto,
                                   @RequestParam(value = "rememberMe", required = false) boolean rememberMe) {

        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginDto.getUsernameOrEmail(), loginDto.getPassword())
            );

            SecurityContextHolder.getContext().setAuthentication(authentication);

            String token = jwtTokenProvider.createToken(loginDto.getUsernameOrEmail());

            // Clear login attempts upon successful login (if you have login attempt tracking)
            // loginAttemptService.resetAttempts(loginDTO.getUsernameOrEmail());
            Optional<User> byCaptcha = userRepository.findByCaptcha(loginDto.getCaptch());
            try {
                String captcha = byCaptcha.get().getCaptcha();
            }catch (Exception e){
                return new ResponseEntity<>("Please enter write captcha ! ", HttpStatus.NOT_FOUND);
            }

            return ResponseEntity.ok(new JWTAuthResponse(token));

        } catch (BadCredentialsException e) {
            // Handle authentication failure here
            User user = userRepository.findByUsernameOrEmail(loginDto.getUsernameOrEmail(), loginDto.getUsernameOrEmail()).get();

            if (user != null) {
                // Check if the account is already locked
                if (user.isAccountLocked()) {
                    return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Account is locked.");
                }

                // Update failed login attempts and last login attempt time
                user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
                user.setLastLoginAttempt(new Date());

                // Check if the user has reached the maximum number of failed login attempts
                if (user.getFailedLoginAttempts() >= user.getLockoutThreshold()) {
                    // Lock the account and set the lockout end time
                    user.setAccountLocked(true);
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MINUTE, user.getLockoutDurationMinutes());
                    user.setLockoutEndTime(calendar.getTime());
                }

                userRepository.save(user);
            }

            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Invalid username/email or password.");
        }
    }


    //http://localhost:8080/api/auth/signup
    @PostMapping("/signup")
    public ResponseEntity<String> registerUser(@Valid @RequestBody SignupDto signupDto, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            // Validation errors occurred
            StringBuilder errorMessage = new StringBuilder();
            for (FieldError fieldError : bindingResult.getFieldErrors()) {
                errorMessage.append(fieldError.getDefaultMessage()).append("\n");
            }
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorMessage.toString());
        }

        // Check if passwords match
        if (!signupDto.getPassword().equals(signupDto.getConfirmPassword())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Passwords do not match");
        }

        // Check if a user with the same Username already exists
       else if (userRepository.existsByUsername(signupDto.getUsername())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Username  is already taken. Please choose different ones.");
        }

        // Check if a user with the same Username already exists
         else if (userRepository.existsByEmail(signupDto.getEmail())){
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Email  is already taken. Please choose different ones.");
        }

         try {
            // Create a new user entity
            User user = new User();
            user.setUsername(signupDto.getUsername());
            user.setEmail(signupDto.getEmail());
            user.setPassword(passwordEncoder.encode(signupDto.getPassword()));

            // Set optional profile information if provided
            if (signupDto.getFirstName() != null) {
                user.setFirstName(signupDto.getFirstName());
            }
            if (signupDto.getLastName() != null) {
                user.setLastName(signupDto.getLastName());
            }

            Role role=roleRepository.findByName("ROLE_USER");
            user.setRoles(Collections.singleton(role));
            // Save the user entity to the database
            userRepository.save(user);

            // Generate and send the verification email
            String verificationToken = generateVerificationToken(user.getEmail());
            emailService.sendVerificationEmail(user.getEmail(), verificationToken);

            return ResponseEntity.status(HttpStatus.CREATED).body("User registered successfully. Check your email for verification.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("An error occurred while processing your request.");
        }
    }

    private String generateVerificationToken(String email) {
        // Generate a unique verification token (e.g., using UUID)
        return UUID.randomUUID().toString();
    }

    @PostMapping("/logout")
    public ResponseEntity<Map<String, String>> logout(HttpServletRequest request, HttpServletResponse response) {
        // Invalidate the user's session
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }

        // Clear the authentication context
        SecurityContextHolder.clearContext();

        // Optionally, you can remove any tokens or credentials stored on the client-side
        // For example, if you're using JWTs, you can expire the token

        // Create a simple success response message
        Map<String, String> responseBody = new HashMap<>();
        responseBody.put("message", "Logout successful");

        return ResponseEntity.ok(responseBody);
    }



    @PostMapping("/forgot-password")
    public ResponseEntity<String> forgotPassword(@RequestParam("email") String email) {
        // Find the user by email
        User user = userRepository.findByEmail(email).get();

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("User with this email does not exist.");
        }

        // Generate a password reset token and set its expiration time (e.g., 1 hour from now)
        String resetToken = generatePasswordResetToken();
        LocalDateTime resetTokenExpiry = LocalDateTime.now().plusHours(1);

        user.setResetToken(resetToken);
        user.setResetTokenExpiry(resetTokenExpiry);

        // Save the user entity with the reset token and its expiry time
        userRepository.save(user);

        // Send the password reset email with a link that includes the resetToken
        String resetLink = "https://yourapp.com/reset-password?token=" + resetToken;
        emailService.sendPasswordResetEmail(email, resetLink);

        return ResponseEntity.status(HttpStatus.OK).body("Password reset link sent to your email.");
    }

    private String generatePasswordResetToken() {
        return UUID.randomUUID().toString();
    }

    @PostMapping("/reset-password")
    public ResponseEntity<String> resetPassword(@RequestBody ResetPasswordDto resetPasswordDto) {
        // Find the user by the reset token
        User user = userRepository.findByResetToken(resetPasswordDto.getToken());

        if (user == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid or expired reset token.");
        }

        // Check if the reset token has expired
//        if (user.getResetTokenExpiry().isBefore(LocalDateTime.now())) {
//            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Reset token has expired.");
//        }

        // Update the user's password and clear the reset token
        user.setPassword(passwordEncoder.encode(resetPasswordDto.getNewPassword()));
        user.setResetToken(null);
        user.setResetTokenExpiry(null);

        // Save the updated user entity
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.OK).body("Password reset successful.");
    }

    //http://localhost:8080/api/auth/captcha
    @PostMapping("/captcha")
    public ResponseEntity<?> createCaptcha(@RequestParam("email") String email) throws Exception {
        Optional<User> byEmail = userRepository.findByEmail(email);
        if(!byEmail.isPresent()){
            return new ResponseEntity<>("User not found for this Email : "+email, HttpStatus.NOT_FOUND);
        }
        String captcha = jwtTokenProvider.captchaGenerator(email);
        return new ResponseEntity<>(captcha, HttpStatus.OK);
    }




}
