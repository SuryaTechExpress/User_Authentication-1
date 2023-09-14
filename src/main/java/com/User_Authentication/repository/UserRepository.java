package com.User_Authentication.repository;


import com.User_Authentication.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByUsernameOrEmail(String username, String email);
    Optional<User> findByUsername(String username);
    Optional<User> findByEmail(String email);
    Optional<User> findByCaptcha(String captcha);
     Boolean existsByUsernameOrEmail(String username, String email);
     Boolean existsByUsername(String username);
     Boolean existsByEmail(String email);


    User findByResetToken(String token);
}

