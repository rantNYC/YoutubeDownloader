package com.example.demo.utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class Utilities {

    public static final PasswordEncoder PASSWORD_ENCODER = new BCryptPasswordEncoder();

    public static String encodePassword(String password){
        return PASSWORD_ENCODER.encode(password);
    }
}
