package ru.bikbaev.jwt_authentication.model.dtos;

import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterUserDTO {

    private String lastName;
    private String firstName;
    private String middleName;
    private LocalDate birthday;
    private String gender;
    private String email;
    private String phone;
    private String password;


}
