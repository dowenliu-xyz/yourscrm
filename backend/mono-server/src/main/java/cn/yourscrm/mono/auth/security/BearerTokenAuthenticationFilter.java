package cn.yourscrm.mono.auth.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.AuthenticationDetailsSource;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.context.SecurityContextHolderStrategy;
import org.springframework.security.web.authentication.AuthenticationEntryPointFailureHandler;
import org.springframework.security.web.authentication.AuthenticationFailureHandler;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
public class BearerTokenAuthenticationFilter extends OncePerRequestFilter {
    private final SecurityContextHolderStrategy securityContextHolderStrategy =
            SecurityContextHolder.getContextHolderStrategy();
    private final AuthenticationFailureHandler authenticationFailureHandler =
            new AuthenticationEntryPointFailureHandler((request, response, authException) ->
                    response.setStatus(HttpStatus.UNAUTHORIZED.value()));
    private final SecurityContextRepository securityContextRepository =
            new RequestAttributeSecurityContextRepository();
    private final AuthenticationDetailsSource<HttpServletRequest, ?> authenticationDetailsSource =
            new WebAuthenticationDetailsSource();
    private final AuthenticationManager authenticationManager;
    private final BearerTokenResolver bearerTokenResolver;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain)
            throws ServletException, IOException {
        String token = bearerTokenResolver.resolve(request);
        if (token == null) {
            filterChain.doFilter(request, response);
            return;
        }

        BearerTokenAuthenticationToken authRequest = new BearerTokenAuthenticationToken(token);
        authRequest.setDetails(this.authenticationDetailsSource.buildDetails(request));
        try {
            AbstractAuthenticationToken authResult =
                    (AbstractAuthenticationToken) authenticationManager.authenticate(authRequest);
            SecurityContext context = securityContextHolderStrategy.createEmptyContext();
            context.setAuthentication(authResult);
            securityContextHolderStrategy.setContext(context);
            securityContextRepository.saveContext(context, request, response);
            filterChain.doFilter(request, response);
        } catch (AuthenticationException e) {
            securityContextHolderStrategy.clearContext();
            authenticationFailureHandler.onAuthenticationFailure(request, response, e);
        }
    }
}
