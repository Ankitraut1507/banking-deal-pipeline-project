package com.bank.pipeline.service;

import com.bank.pipeline.dto.AuthResponse;
import com.bank.pipeline.dto.LoginRequest;

/**Authentication service contract**/
public interface AuthService {

    AuthResponse login(LoginRequest request);

    AuthResponse refresh(String refreshToken);

    void logout(String refreshToken);
}
