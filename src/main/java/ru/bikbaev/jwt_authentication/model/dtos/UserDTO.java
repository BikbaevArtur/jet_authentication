package ru.bikbaev.jwt_authentication.model.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.time.LocalDate;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class UserDTO implements Serializable {
    private long id;
    private String lastName;
    private String firstName;
    private String middleName;
    private String email;
    private String phone;
    private boolean activateEmail;
    private boolean activatePhone;
    private String avatar;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "dd-MM-yyyy")
    private LocalDate timeRegistration;
}
