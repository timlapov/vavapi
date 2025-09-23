package art.lapov.vavapi.utils;

import art.lapov.vavapi.security.KeyManager;
import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;

@Service
@AllArgsConstructor
public class JwtUtil {

    private UserDetailsService userService;
    private KeyManager keyManager;

    /**
     * Generates a JWT containing the identifier of the user passed as parameter
     * Default expiration time is 30 minutes
     * @param user The User for which to create a JWT
     * @return The generated JWT
     */
    public String generateToken(UserDetails user) {
        return generateToken(user, Instant.now().plus(30, ChronoUnit.MINUTES));
    }

    /**
     * Generates a JWT containing the identifier of the user passed as parameter
     *
     * @param user The User for which to create a JWT
     * @param expiration The token expiration time
     * @return The generated JWT
     */
    public String generateToken(UserDetails user, Instant expiration) {

        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(expiration)
                .sign(keyManager.getAlgorithm());
    }

    /**
     * Method to verify the validity of a token and retrieve the User associated
     * with the token in question
     *
     * @param token The token as a string
     * @return The User linked to the token
     */
    public UserDetails validateToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT
                    .require(keyManager.getAlgorithm())
                    .build()
                    .verify(token);
            String userIdentifier = decodedJWT.getSubject();
            return userService.loadUserByUsername(userIdentifier);
        } catch (JWTVerificationException e) {
            throw new BadCredentialsException("Invalid or expired token", e);
        } catch (UsernameNotFoundException e) {
            throw new BadCredentialsException("User not found", e);
        }
    }


}

