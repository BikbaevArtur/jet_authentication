package ru.bikbaev.jwt_authentication;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithAnonymousUser;
import org.springframework.test.web.servlet.MockMvc;
import ru.bikbaev.jwt_authentication.configs.SecurityConfiguration;
import ru.bikbaev.jwt_authentication.controller.AuthenticationController;
import ru.bikbaev.jwt_authentication.mapper.UserMapper;
import ru.bikbaev.jwt_authentication.model.dtos.LoginUserDTO;
import ru.bikbaev.jwt_authentication.model.dtos.RegisterUserDTO;
import ru.bikbaev.jwt_authentication.model.entity.User;
import ru.bikbaev.jwt_authentication.service.AuthenticationService;
import ru.bikbaev.jwt_authentication.service.JwtAccessService;
import ru.bikbaev.jwt_authentication.service.JwtRefreshService;

import java.time.LocalDate;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(AuthenticationController.class)
@Import(SecurityConfiguration.class)
public class AuthenticationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserMapper userMapper;


    @MockBean
    private PasswordEncoder passwordEncoder;

    @MockBean
    private AuthenticationProvider authenticationProvider;


    @MockBean
    private JwtAccessService jwtAccessService;

    @MockBean
    private JwtRefreshService jwtRefreshService;

    @MockBean
    private AuthenticationService authenticationService;


    private ObjectMapper objectMapper;

    @BeforeEach
    public void setUp() {
        objectMapper = new ObjectMapper();
    }


    public User testUser() {

        String password = passwordEncoder.encode("testPassword");

        User user = new User();
        user.setLastName("testLastName");
        user.setFirstName("testFirstName");
        user.setMiddleName("testMiddleName");
        user.setEmail("test@mail.ru");
        user.setPassword(password);
        return user;
    }

    @Test
    @WithAnonymousUser
    public void testRegister() throws Exception {
        RegisterUserDTO registerUserDTO = new RegisterUserDTO();

        registerUserDTO.setLastName("testLastName");
        registerUserDTO.setFirstName("testFirstName");
        registerUserDTO.setMiddleName("testMiddleName");
        registerUserDTO.setBirthday(LocalDate.now());
        registerUserDTO.setGender("M");
        registerUserDTO.setEmail("test@mail.ru");
        registerUserDTO.setPassword("testPassword");


        User user = testUser();



        String password = passwordEncoder.encode("testPassword");

        objectMapper.registerModule(new JavaTimeModule());


        String json = objectMapper.writeValueAsString(registerUserDTO);

        when(authenticationService.signup(any(RegisterUserDTO.class))).thenReturn(user);


        mockMvc.perform(
                        post("/auth/signup")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.lastName").value("testLastName"))
                .andExpect(jsonPath("$.firstName").value("testFirstName"))
                .andExpect(jsonPath("$.middleName").value("testMiddleName"))
                .andExpect(jsonPath("$.email").value("test@mail.ru"))
                .andExpect(jsonPath("$.password").value(password));
    }


    @Test
    @WithAnonymousUser

    public void testLogin() throws Exception {

        LoginUserDTO loginUserDTO = new LoginUserDTO();

        loginUserDTO.setEmail("test@mail.ru");
        loginUserDTO.setPassword("testPassword");

        User user = testUser();

        when(authenticationService.authenticate(loginUserDTO)).thenReturn(user);
        when(jwtAccessService.generateTokenAccess(user, user.getId())).thenReturn("accessToken");
        when(jwtAccessService.getJwtExpirationAccess()).thenReturn(3600L);
        when(jwtRefreshService.generateTokenRefresh(user)).thenReturn("refreshToken");
        when(jwtRefreshService.getJwtExpirationRefresh()).thenReturn(7200L);

        mockMvc.perform(
                        post("/auth/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(loginUserDTO)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").value("accessToken"))
                .andExpect(jsonPath("$.expiresInAccess").value(3600L))
                .andExpect(jsonPath("$.refreshToken").value("refreshToken"))
                .andExpect(jsonPath("$.expiresInRefresh").value(7200L));
    }


    @Test
    public void isValidateAccessTokenTest() {

        User user = testUser();
        String token = jwtAccessService.generateTokenAccess(user, user.getId());


        when(jwtAccessService.extractUsername(token)).thenReturn(user.getEmail());
        when(jwtAccessService.isTokenExpired(token)).thenReturn(false);
        when(jwtAccessService.isTokenValid(token, user)).thenReturn(true);


    }


}
