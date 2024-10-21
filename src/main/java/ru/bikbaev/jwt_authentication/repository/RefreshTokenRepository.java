package ru.bikbaev.jwt_authentication.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.bikbaev.jwt_authentication.model.entity.RefreshToken;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface RefreshTokenRepository  extends JpaRepository<RefreshToken, UUID> {
    List<RefreshToken> findRefreshTokensByUserId(int id);

    Optional<RefreshToken> findByToken(String token);

}
