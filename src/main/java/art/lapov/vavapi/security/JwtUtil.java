package art.lapov.vavapi.security;

import com.auth0.jwt.JWT;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import lombok.AllArgsConstructor;
import org.springframework.security.authorization.AuthorizationDeniedException;
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
     * Génère un JWT contenant l'identifiant du user passé en paramètre
     * par défaut son temps d'expiration est de 30 minutes
     * @param user Le User pour lequel on souhaite créer un JWT
     * @return Le JWT généré
     */
    public String generateToken(UserDetails user) {
        return generateToken(user, Instant.now().plus(30, ChronoUnit.MINUTES));
    }

    /**
     * Génère un JWT contenant l'identifiant du user passé en paramètre
     *
     * @param user Le User pour lequel on souhaite créer un JWT
     * @param expiration Le temps d'expiration du token
     * @return Le JWT généré
     */
    public String generateToken(UserDetails user, Instant expiration) {

        return JWT.create()
                .withSubject(user.getUsername())
                .withExpiresAt(expiration)
                .sign(keyManager.getAlgorithm());
    }

    /**
     * Méthode pour vérifier la validité d'un token et récupérer le User associé
     * au token en question
     *
     * @param token Le token en chaîne de caractères
     * @return Le User lié au token
     */
    public UserDetails validateToken(String token) {
        try {
            DecodedJWT decodedJWT = JWT
                    .require(keyManager.getAlgorithm())
                    .build()
                    .verify(token);
            String userIdentifier = decodedJWT.getSubject();
            return userService.loadUserByUsername(userIdentifier);
        } catch (JWTVerificationException | UsernameNotFoundException e) {
            throw new AuthorizationDeniedException("Error verifying token");
        }
    }


}

