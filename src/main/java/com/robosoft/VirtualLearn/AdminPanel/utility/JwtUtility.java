package com.robosoft.VirtualLearn.AdminPanel.utility;

import io.jsonwebtoken.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import java.io.Serializable;
import java.util.*;


@Component
public class JwtUtility implements Serializable
{
    private static final long serialVersionUID = 234234523523L;
    private String secret;
    private int jwtExpirationInMs;
    private int refreshExpirationDateInMs;
    @Value("${jwt.secret}")
    public void setSecret(String secret)
    {
        this.secret = secret;
    }
    @Value("${jwt.expirationDateInMs}")
    public void setJwtExpirationInMs(int jwtExpirationInMs)
    {
        this.jwtExpirationInMs = jwtExpirationInMs;
    }
    @Value("${jwt.refreshExpirationDateInMs}")
    public void setRefreshExpirationDateInMs(int refreshExpirationDateInMs)
    {
        this.refreshExpirationDateInMs = refreshExpirationDateInMs;
    }

    //retrieve username from jwt token
//    public String getUsernameFromToken(String token) {   //  Number 1
//        System.out.println("inside getUsernameFromToken");
//        return getClaimFromToken(token, Claims::getSubject);
//    }
//
//    //retrieve expiration date from jwt token
//    public Date getExpirationDateFromToken(String token) {
//        System.out.println("inside getExpirationDateFromToken");
//        return getClaimFromToken(token, Claims::getExpiration);
//    }
//
//
//    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {  // Number 2
//        System.out.println("inside getClaimFromToken");
//        final Claims claims = getAllClaimsFromToken(token);
//        return claimsResolver.apply(claims);
//    }
//
//
//    //for retrieving any information from token we will need the secret key
//    private Claims getAllClaimsFromToken(String token) { // Number 3
//        System.out.println("inside getAllClaimsFromToken");
//        return Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token).getBody();
//    }
//
//
//    //check if the token has expired
//    private Boolean isTokenExpired(String token) {
//        System.out.println("inside isTokenExpired");
//        final Date expiration = getExpirationDateFromToken(token);
//        return expiration.before(new Date());
//    }

    //generate token for user
    public String generateToken(UserDetails userDetails)
    {
        Map<String, Object> claims = new HashMap<>();
        Collection<? extends GrantedAuthority> roles = userDetails.getAuthorities();
        if (roles.contains(new SimpleGrantedAuthority("ROLE_ADMIN")))
        {
            claims.put("isAdmin", true);
        }
        if (roles.contains(new SimpleGrantedAuthority("ROLE_USER")))
        {
            claims.put("isUser", true);
        }
        return doGenerateToken(claims, userDetails.getUsername());
    }

    //while creating the token -
    //1. Define  claims of the token, like Issuer, Expiration, Subject, and the ID
    //2. Sign the JWT using the HS512 algorithm and secret key.
    private String doGenerateToken(Map<String, Object> claims, String subject)
    {
        System.out.println("Token Generation");
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + jwtExpirationInMs))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }
    public String doGenerateRefreshToken(Map<String, Object> claims, String subject)
    {
        System.out.println("Inside Do Token Generation");
        return Jwts.builder().setClaims(claims).setSubject(subject).setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + refreshExpirationDateInMs))
                .signWith(SignatureAlgorithm.HS512, secret).compact();
    }

    public boolean validateToken(String authToken)
    {
        try
        {
            // Jwt token has not been tampered with
            Jws<Claims> claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken);
            return true;
        }
        catch (SignatureException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException ex)
        {
            throw new BadCredentialsException("INVALID_CREDENTIALS", ex);
        }
        catch (ExpiredJwtException ex)
        {
            throw ex;
        }
    }
    public String getUsernameFromToken(String token)
    {
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(token).getBody();
        return claims.getSubject();
    }
    public List<SimpleGrantedAuthority> getRolesFromToken(String authToken)
    {
        List<SimpleGrantedAuthority> roles = null;
        Claims claims = Jwts.parser().setSigningKey(secret).parseClaimsJws(authToken).getBody();
        Boolean isAdmin = claims.get("isAdmin", Boolean.class);
        Boolean isUser = claims.get("isUser", Boolean.class);
        if (isAdmin != null && isAdmin == true)
        {
            roles = Arrays.asList(new SimpleGrantedAuthority("ROLE_ADMIN"));
        }
        if (isUser != null && isUser == true)
        {
            roles = Arrays.asList(new SimpleGrantedAuthority("ROLE_USER"));
        }
        return roles;
    }
}
