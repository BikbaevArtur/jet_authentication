package ru.bikbaev.jwt_authentication.service;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.bikbaev.jwt_authentication.model.dtos.LoginUserDTO;
import ru.bikbaev.jwt_authentication.model.dtos.RegisterUserDTO;
import ru.bikbaev.jwt_authentication.model.entity.User;
import ru.bikbaev.jwt_authentication.repository.UserRepository;

@Service
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;

    public AuthenticationService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
    }


    public User signup(RegisterUserDTO input){
        User user = new User();
        user.setLastName(input.getLastName());
        user.setFirstName(input.getFirstName());
        user.setMiddleName(input.getMiddleName());
        user.setBirthday(input.getBirthday());
        user.setGender(input.getGender().toUpperCase());
        user.setEmail(input.getEmail());
        user.setPhone(input.getPhone());
        user.setPassword(passwordEncoder.encode(input.getPassword()));
        return userRepository.save(user);
    }


    public User authenticate(LoginUserDTO input){
        authenticationManager.authenticate(
          new UsernamePasswordAuthenticationToken(
                  input.getEmail(),
                  input.getPassword()
          )
        );

        return userRepository.findByEmail(input.getEmail()).orElseThrow();
    }


    public User findUser(String email){
        return userRepository.findByEmail(email).orElseThrow();
    }

}
