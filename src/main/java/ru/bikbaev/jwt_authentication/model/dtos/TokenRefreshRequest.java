package ru.bikbaev.jwt_authentication.model.dtos;

import lombok.Getter;

@Getter
public class TokenRefreshRequest {
    private  String refreshToken;
}
