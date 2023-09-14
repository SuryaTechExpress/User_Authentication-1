package com.User_Authentication.util;

import ch.qos.logback.core.CoreConstants;
import ch.qos.logback.core.net.SyslogOutputStream;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Arrays;
import java.util.Optional;

public class MainClass {
    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();
        System.out.println(encoder.encode("testing"));



    }
}
