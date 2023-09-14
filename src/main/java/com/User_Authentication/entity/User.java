package com.User_Authentication.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.Date;
import java.util.Set;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name="users",uniqueConstraints = {
        @UniqueConstraint(name = "unique_username", columnNames = "username"),
        @UniqueConstraint(name = "unique_email", columnNames = "email")
})

public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String username;

    private String email;

    private String password;

    private String firstName; // Optional field
    private String lastName;  // Optional field
    private String captcha;

    private int failedLoginAttempts = 0;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lastLoginAttempt;

    private boolean accountLocked = false;

    private int lockoutThreshold = 3;
    private int lockoutDurationMinutes = 15;

    @Temporal(TemporalType.TIMESTAMP)
    private Date lockoutEndTime;

    private boolean isActive;

    @Column(name = "reset_token")
    private String resetToken;

    @Column(name = "reset_token_expiry")
    private LocalDateTime resetTokenExpiry;

    @ManyToMany(fetch = FetchType.EAGER,cascade = CascadeType.ALL)
    @JoinTable(name = "user_roles",
            joinColumns = @JoinColumn(name = "user_id", referencedColumnName ="id"),
            inverseJoinColumns = @JoinColumn(name = "role_id",referencedColumnName = "id"))
    private Set<Role> roles ;


}


