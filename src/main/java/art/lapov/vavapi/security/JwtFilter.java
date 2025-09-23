package art.lapov.vavapi.security;

import art.lapov.vavapi.utils.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authorization.AuthorizationDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@AllArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        //On récupère le contenu du header Authorization où peut se trouver le token
        String authHeader = request.getHeader("Authorization");
        // Si on est sur la route login ou refresh, qu'on a pas de header ou pas de type jwt, on passe, on s'arrête ici
        if (request.getRequestURI().startsWith("/api/login")
                || request.getRequestURI().startsWith("/api/refresh-token")
                || authHeader == null
                || !authHeader.startsWith("Bearer")) {
            filterChain.doFilter(request, response);
            return;
        }
        //on récupère spécifiquement le token du header en retirant le "Bearer " qui se trouve avant
        String jwt = authHeader.substring(7);

        try {
            //On valide le token et on récupère le User lié à son identifiant
            UserDetails user = jwtUtil.validateToken(jwt);
            //On met ce user dans l'authentification spring security le temps de la requête, ce qui permettra d'autoriser l'accès aux routes ou non et de récupérer le @AuthenticationPrincipal dans les contrôleurs
            SecurityContextHolder.getContext().setAuthentication(
                    new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
            filterChain.doFilter(request, response);
        } catch (BadCredentialsException e) {
            //Si le token n'est pas valide/expiré, on renvoie un 401 unauthorized
            response.sendError(HttpStatus.UNAUTHORIZED.value(), e.getMessage());
        } catch (AuthorizationDeniedException e) {
            //Si l'utilisateur n'a pas les droits nécessaires, on renvoie un 403 forbidden
            response.sendError(HttpStatus.FORBIDDEN.value(), e.getMessage());
        }

    }

}
