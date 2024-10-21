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

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
public class JwtAccessService {
    @Value("${spring.security.jwt.secret-key-access}")
    private String secretKeyAccess;

    @Getter
    @Value("${spring.security.jwt.expiration-time-access}")
    private long jwtExpirationAccess;


    public String generateTokenAccess(UserDetails userDetails, Long userId) {
        Map<String, Object> extraClaims = new HashMap<>();
        extraClaims.put("userId", userId);
        return generateTokenAccess(extraClaims, userDetails);
    }


    public String generateTokenAccess(Map<String, Object> extraClaims, UserDetails userDetails) {
        return buildToken(extraClaims, userDetails, jwtExpirationAccess);
    }



    public boolean isTokenValid(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }


    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);

    }


    public long extractIdUser(String token) {
        return extractClaim(token, claims -> claims.get("userId", Long.class));
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
        return Jwts.parser().
                verifyWith(getSecretKeyAccess()).
                build()
                .parseSignedClaims(token).getPayload();
    }

    private String buildToken(
            Map<String, Object> extraClaims,
            UserDetails userDetails,
            long expiration
    ) {


        return Jwts
                .builder()
                .claims()
                .empty()
                .add(extraClaims)
                .and()
                .subject(userDetails.getUsername())
                .issuedAt(new Date(System.currentTimeMillis()))
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSecretKeyAccess(), SignatureAlgorithm.HS256)
                .compact();
    }


    private SecretKey getSecretKeyAccess() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKeyAccess);
        return Keys.hmacShaKeyFor(keyBytes);
    }

}
