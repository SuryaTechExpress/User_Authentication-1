package com.User_Authentication.controller;


import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class Welcome {

    @GetMapping("/welcomes")
    public ResponseEntity<?> welcome(){
        return new ResponseEntity<>("ok", HttpStatus.OK);
    }

}
