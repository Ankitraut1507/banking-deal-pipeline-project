package com.bank.pipeline.service;

import com.bank.pipeline.model.RefreshToken;
import com.bank.pipeline.model.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken validateRefreshToken(String token);

    void revokeRefreshToken(String token);
}
