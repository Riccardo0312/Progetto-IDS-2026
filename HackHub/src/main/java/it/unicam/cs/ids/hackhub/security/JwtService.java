package it.unicam.cs.ids.hackhub.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import javax.crypto.SecretKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Servizio JWT stateless (JJWT 0.12.6). Replica del modello di riferimento:
 * access token firmato HMAC, subject = email utente, scadenza configurabile.
 *
 * <p>Nessun refresh token, nessuna revoca/blacklist (logout client-side).
 */
@Service
public class JwtService {

	@Value("${jwt.secret.key}")
	private String secretKey;

	@Value("${jwt.expiration.access-token}")
	private long accessTokenExpiration;

	/** Subject del token = email. */
	public String extractUsername(String token) {
		return extractClaim(token, Claims::getSubject);
	}

	public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
		final Claims claims = extractAllClaims(token);
		return claimsResolver.apply(claims);
	}

	public String generateToken(UserDetails userDetails) {
		return generateToken(new HashMap<>(), userDetails);
	}

	public String generateToken(Map<String, Object> extraClaims, UserDetails userDetails) {
		long now = System.currentTimeMillis();
		return Jwts.builder()
				.claims(extraClaims)
				.id(UUID.randomUUID().toString())
				.subject(userDetails.getUsername())
				.issuedAt(new Date(now))
				.expiration(new Date(now + accessTokenExpiration))
				.signWith(getSignInKey())
				.compact();
	}

	public String extractJti(String token) {
		return extractClaim(token, Claims::getId);
	}

	/** Token valido se il subject coincide con lo username e non è scaduto. */
	public boolean isTokenValid(String token, UserDetails userDetails) {
		final String username = extractUsername(token);
		return username.equals(userDetails.getUsername()) && !isTokenExpired(token);
	}

	public Date extractExpiration(String token) {
		return extractClaim(token, Claims::getExpiration);
	}

	private boolean isTokenExpired(String token) {
		return extractExpiration(token).before(new Date());
	}

	private Claims extractAllClaims(String token) {
		return Jwts.parser()
				.verifyWith(getSignInKey())
				.build()
				.parseSignedClaims(token)
				.getPayload();
	}

	private SecretKey getSignInKey() {
		byte[] keyBytes = Decoders.BASE64.decode(secretKey);
		return Keys.hmacShaKeyFor(keyBytes);
	}
}
