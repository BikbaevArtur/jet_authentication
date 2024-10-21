package ru.bikbaev.jwt_authentication.model.dtos;

import lombok.Data;

@Data
public class LoginResponse {
    private String accessToken;
    private long expiresInAccess;
    private String refreshToken;
    private long expiresInRefresh;
}
