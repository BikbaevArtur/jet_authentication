package ru.bikbaev.jwt_authentication.model.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.util.Date;
import java.util.UUID;

@Entity
@Table(name = "tokens")
@Data
public class RefreshToken {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", updatable = false, nullable = false)
    private UUID id;

    @Column(name = "user_id")
    private int userId;

    @Column(name = "refresh_token")
    private String token;

    @Column(name = "expires_in")
    private long expiresIn;

    @Column(name = "creat_at")
    private Date createdAt;

}
