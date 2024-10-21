package ru.bikbaev.jwt_authentication.mapper;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.bikbaev.jwt_authentication.model.dtos.UserDTO;
import ru.bikbaev.jwt_authentication.model.entity.User;

@Slf4j
@Component
public class UserMapper {


    public String convertUserForJson(UserDTO user) {

        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        try {
            return objectMapper.writeValueAsString(user);

        } catch (JsonProcessingException e) {
            log.info(e.getMessage());
            return "{false}";
        }
    }

    public UserDTO userConvertDTO(User user){
        return new UserDTO(
                user.getId(),
                user.getLastName(),
                user.getFirstName(),
                user.getMiddleName(),
                user.getEmail(),
                user.getPhone(),
                user.isActivateEmail(),
                user.isActivatePhone(),
                user.getAvatar(),
                user.getTimeRegistration()
        );

    }
}
