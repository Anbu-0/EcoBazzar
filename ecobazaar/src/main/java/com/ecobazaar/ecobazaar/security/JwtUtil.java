package com.ecobazaar.ecobazaar.security;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;


@Component
public class JwtUtil{
	private final String SECRET_KEY = "farmchainproject_789456123_here_orignal";
	private final long EXPIRATION = 1000*60*60; //1 hour validity
	
	private Key getSigningKey() {
		return Keys.hmacShaKeyFor(SECRET_KEY.getBytes());
	}
	// 1. Generate JWT token
	public String generateToken(String email) {
		
		return Jwts.builder()
				.setSubject(email)
				.setIssuedAt(new Date())
				.setExpiration(new Date(System.currentTimeMillis()+EXPIRATION))
				.signWith(getSigningKey(),SignatureAlgorithm.HS256)
				.compact();
	}
	// 2. Extract email from token
	public String extractEmail(String token) {

		return Jwts.parserBuilder()
		.setSigningKey(getSigningKey()) // verify signature
		.build()
		.parseClaimsJws(token)
		.getBody()
		.getSubject();
		}
	// 3. Validate token
	public boolean validateToken(String token, String email) {
		String extractedEmail = extractEmail(token);
		return(extractedEmail.equals(email)&&!isExpired(token));
	}
	//4. check expiry
	private boolean isExpired(String token) {
		Date exp = Jwts.parserBuilder()
				.setSigningKey(getSigningKey())
				.build()
				.parseClaimsJws(token)
				.getBody()
				.getExpiration();
		return exp.before(new Date());
	}
}