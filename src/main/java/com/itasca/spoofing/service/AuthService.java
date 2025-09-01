package com.itasca.spoofing.service;

import java.util.Map;

public interface AuthService {
    Map<String, Object> signup(String email, String password, String firstName, String lastName);
    Map<String, Object> signin(String email, String password);
    Map<String, Object> verifyAndRefreshToken(String token);
}