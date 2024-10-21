package ru.bikbaev.jwt_authentication.controller;

import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;
import ru.bikbaev.jwt_authentication.mapper.UserMapper;
import ru.bikbaev.jwt_authentication.model.dtos.*;
import ru.bikbaev.jwt_authentication.model.entity.User;
import ru.bikbaev.jwt_authentication.service.AuthenticationService;
import ru.bikbaev.jwt_authentication.service.JwtAccessService;
import ru.bikbaev.jwt_authentication.service.JwtRefreshService;

@RequestMapping("/auth")
@RestController
public class AuthenticationController {
    private final JwtAccessService jwtService;
    private final AuthenticationService authenticationService;
    private final JwtRefreshService jwtRefreshService;
    private final UserMapper userMapper;
    private final KafkaTemplate<String, String> kafkaTemplate;

    private final String TOPIC_USERS = "users";

    public AuthenticationController(
            JwtAccessService jwtService,
            AuthenticationService authenticationService, JwtRefreshService jwtRefreshService, UserMapper userMapper, KafkaTemplate<String, String> kafkaTemplate) {
        this.jwtService = jwtService;
        this.authenticationService = authenticationService;
        this.jwtRefreshService = jwtRefreshService;
        this.userMapper = userMapper;
        this.kafkaTemplate = kafkaTemplate;
    }


    @Tag(name = "Регистрация", description = "Регистрация нового пользователя")
    @PostMapping("/signup")
    public ResponseEntity<UserDTO> register(@RequestBody
                                            @Parameter
                                                    (description = "данные для регистрации нового пользователя")
                                            RegisterUserDTO registerUserDTO) {
        User register = authenticationService.signup(registerUserDTO);

        UserDTO userResponse = userMapper.userConvertDTO(register);

        String messageUser = userMapper.convertUserForJson(userResponse);

        String topicKeyUserId = String.valueOf(register.getId());

        kafkaTemplate.send(TOPIC_USERS,topicKeyUserId,messageUser);

        return ResponseEntity.ok(userResponse);
    }


    @Tag(name = "аутентификация", description = "вход в сервис")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> authenticate(@RequestBody
                                                      @Parameter(description = "логин и пароль пользователя")
                                                      LoginUserDTO loginUserDTO) {
        User authUser = authenticationService.authenticate(loginUserDTO);

        String jwtToken = jwtService.generateTokenAccess(authUser, authUser.getId());

        String refreshToken = jwtRefreshService.generateTokenRefresh(authUser);
        jwtRefreshService.saveToken(refreshToken, authUser.getId());


        LoginResponse loginResponse = new LoginResponse();

        loginResponse.setAccessToken(jwtToken);
        loginResponse.setExpiresInAccess(jwtService.getJwtExpirationAccess());

        loginResponse.setRefreshToken(refreshToken);
        loginResponse.setExpiresInRefresh(jwtRefreshService.getJwtExpirationRefresh());


        return ResponseEntity.ok(loginResponse);
    }

    @Tag(name = "refresh token", description = "обнавление access,refresh токена с помощью переданного через post refresh токена  ")
    @PostMapping("/refresh")
    public ResponseEntity<LoginResponse> refreshToken(@RequestBody
                                                      @Parameter(description = "refresh token")
                                                      TokenRefreshRequest tokenRefreshRequest) {

        String userName = jwtRefreshService.extractUsername(tokenRefreshRequest.getRefreshToken());


        User authUser = authenticationService.findUser(userName);


        if ((jwtRefreshService.isTokenRevoked(tokenRefreshRequest.getRefreshToken()))) {


            return new ResponseEntity<>(HttpStatus.FORBIDDEN);

        } else {

            if (!jwtRefreshService.isTokenValid(tokenRefreshRequest.getRefreshToken())) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }


            String tokenAccess = jwtService.generateTokenAccess(authUser, authUser.getId());

            String refreshToken = jwtRefreshService.generateTokenRefresh(authUser);

            jwtRefreshService.saveToken(refreshToken
                    , authenticationService
                            .findUser(authUser.getUsername()).getId());

            LoginResponse loginResponse = new LoginResponse();

            loginResponse.setAccessToken(tokenAccess);
            loginResponse.setExpiresInAccess(jwtService.getJwtExpirationAccess());

            loginResponse.setRefreshToken(refreshToken);
            loginResponse.setExpiresInRefresh(jwtRefreshService.getJwtExpirationRefresh());

            return ResponseEntity.ok(loginResponse);
        }


    }

    @Tag(name = "Выход", description = "выход из системы, удаление refresh токена с бд, не удаляет access токен! access удалить на фронте. Для выхода нужен access токен ")
    @PostMapping("/logout")
    public ResponseEntity<Void> logout(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);


        String username = jwtRefreshService.extractUsername(token);
        User user = authenticationService.findUser(username);

        jwtRefreshService.killToken((int) user.getId());

        return ResponseEntity.ok().build();
    }


}
