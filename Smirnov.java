package vstu.isd.notebin.controller.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jose4j.jwt.JwtClaims;
import org.jose4j.jwt.MalformedClaimException;
import org.jose4j.jwt.consumer.JwtConsumer;
import org.jose4j.jwt.consumer.JwtConsumerBuilder;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import vstu.isd.notebin.api.auth.AuthApi;
import vstu.isd.notebin.api.auth.VerifyAccessTokenRequest;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthFilter extends OncePerRequestFilter {

    private final AuthApi authApi;
    private final String userIdHeaderAttribute;

    private final String TOKEN_PREFIX = "Bearer ";

    private final String USER_ID_TOKEN_KEY = "user_id";

    private final JwtConsumer jwtParser = new JwtConsumerBuilder()
            .setSkipSignatureVerification()
            .build();

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String token = extractToken(request);

        if (token != null) {

            JwtClaims claims = verifyToken(token);
            boolean isTokenValid = claims != null;

            if (isTokenValid) {
                String userId = getUserId(claims);
                saveAuthentication(userId);
                request.setAttribute(userIdHeaderAttribute, userId);
            } else {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (header == null || !header.startsWith(TOKEN_PREFIX)) {
            return null;
        }
        return header.substring(TOKEN_PREFIX.length());
    }

    private JwtClaims verifyToken(String token) {
        try {

            JwtClaims claims = getClaims(token); // also throws exception if token expired or in invalid format

            if (!claimsContainsMandatoryFields(claims)) {
                return null;
            }

            boolean isTokenValid = false;
            try {
                isTokenValid = authApi.verifyAccessToken(new VerifyAccessTokenRequest(token));
            } catch (Exception e) {
                log.error("Failed to verify token", e);
            }

            return isTokenValid ? claims : null;
        } catch (Exception e) {
            return null;
        }
    }

    private JwtClaims getClaims(String token) {
        try {
            return jwtParser.processToClaims(token);
        } catch (Exception e) {
            throw new IllegalArgumentException("Invalid token", e);
        }
    }

    private boolean claimsContainsMandatoryFields(JwtClaims claims) throws MalformedClaimException {
        return claims.hasClaim(USER_ID_TOKEN_KEY) &&
                claims.getSubject() != null &&
                claims.getIssuedAt() != null;
    }

    private String getUserId(JwtClaims claims) {
        if (!claims.hasClaim(USER_ID_TOKEN_KEY)) {
            throw new IllegalArgumentException("user_id is not present in token");
        }
        return claims.getClaimValueAsString(USER_ID_TOKEN_KEY);
    }

    private void saveAuthentication(String userId) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(
                        userId,
                        null, // no password due using JWT
                        List.of()
                )
        );
    }
}
