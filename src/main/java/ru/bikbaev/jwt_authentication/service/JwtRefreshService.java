package ru.bikbaev.jwt_authentication.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import ru.bikbaev.jwt_authentication.model.entity.RefreshToken;
import ru.bikbaev.jwt_authentication.repository.RefreshTokenRepository;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtRefreshService {

    @Value("${spring.security.jwt.secret-key-access}")
    private String secretKeyRefresh;

    @Getter
    @Value("${spring.security.jwt.expiration-time-refresh}")
    private long jwtExpirationRefresh;

    private final RefreshTokenRepository refreshTokenRepository;

    public JwtRefreshService(RefreshTokenRepository refreshTokenRepository) {
        this.refreshTokenRepository = refreshTokenRepository;
    }


    public void saveToken(String refreshToken, long userId) {
        List<RefreshToken> refreshTokens = refreshTokenRepository.findRefreshTokensByUserId((int) userId);
        refreshTokenRepository.deleteAll(refreshTokens);

        RefreshToken token = new RefreshToken();
        token.setUserId((int) userId);
        token.setToken(refreshToken);
        token.setExpiresIn(jwtExpirationRefresh);
        token.setCreatedAt(new Date(System.currentTimeMillis()));

        refreshTokenRepository.save(token);

    }


    public String generateTokenRefresh(UserDetails userDetails) {
        return generateTokenRefresh(new HashMap<>(), userDetails);
    }


    public String generateTokenRefresh(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpirationRefresh);
    }


    public void killToken(int id) {

        List<RefreshToken> tokens = refreshTokenRepository.findRefreshTokensByUserId(id);
        refreshTokenRepository.deleteAll(tokens);
    }


    public boolean isTokenValid(String token) {
        return !isTokenExpired(token);
    }


    public boolean isTokenRevoked(String token) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(token).orElse(null);
        return refreshToken == null;
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }


    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }


    public boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }


    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }


    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKeyAccess())
                .build()
                .parseSignedClaims(token).getPayload();
    }


    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {

        return
                Jwts.builder()
                        .claims()
                        .empty().add(extraClaims)
                        .and()
                        .subject(userDetails.getUsername())
                        .issuedAt(new Date(System.currentTimeMillis()))
                        .expiration(new Date(System.currentTimeMillis() + expiration))
                        .signWith(getSecretKeyAccess(), SignatureAlgorithm.HS256).compact();


    }


    private SecretKey getSecretKeyAccess() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyRefresh);
        return Keys.hmacShaKeyFor(keyBytes);
    }


}
