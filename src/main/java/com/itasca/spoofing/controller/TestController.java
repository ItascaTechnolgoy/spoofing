package com.itasca.spoofing.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/test")
public class TestController {

    @GetMapping("/health")
    public String health() {
        return "OK";
    }
}