package com.itasca.spoofing.controller;


import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller

@RequestMapping("/api/v1")
public class HelloController {

    @RequestMapping(value = "/hello", method = RequestMethod.GET)
    ResponseEntity<String> status( ) {
       return ResponseEntity.ok().body("Hello World");
    }

}
