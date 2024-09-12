package com.DATA.DataCodeAnalysing.Util;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

@Component
public class JwtUtil {

    private String SECRET_KEY = "++MCLjxMQ3XkDs9nGAua3FcALs7t8XmT0Cy1NJ7e/BcnRAJCZWRMQ4vdLVw6fRz1Zqh0ISuV8ZTsBmd5AeHcQNZBQNhZQYAO+FVwoA0/6pcPc/krKV3Skikcsh7aZ4C7lCFQbB775UyMjSEFu2eM/3ZkAL7X2AIPTABbEGH9urqmHgcWiJaIGg6aRciXHqeDf2KbKk7/X+ainAH5QlB1WxOlwBQomyWv6SV/daD4jxrzBlN5ROSaxmvnh8+p93a4trjFTmmsgdHYHOTZX15vIa1ZMbKbZJGZEshfDxitB9yLISER6qYggddc5qBMWnvIuHE7t5iJFmv7pbNS0lyaPrmqLFu73QeRAd8igrwkqfM="; //Use a secure secret key

    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser().setSigningKey(SECRET_KEY).parseClaimsJws(token).getBody();
    }

    private Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }

    public String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + 1000 * 60 )) // Token valid for 10 hours 1000 * 60 * 60 * 10
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY).compact();
    }

    public Boolean validateToken(String token, String username) {
        final String extractedUsername = extractUsername(token);
        return (extractedUsername.equals(username) && !isTokenExpired(token));
    }
}
