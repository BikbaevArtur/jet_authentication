package ru.bikbaev.jwt_authentication.model.dtos;

import lombok.Data;

@Data
public class LoginUserDTO {
    String email;
    String password;
}
