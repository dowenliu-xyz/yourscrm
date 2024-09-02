package cn.yourscrm.mono.auth.security;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;

import java.security.SecureRandom;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {
    @Bean
    public BearerTokenResolver bearerTokenResolver() {
        return new DefaultBearerTokenResolver();
    }

    @Bean
    public BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter(
            AuthenticationManager authenticationManager, BearerTokenResolver bearerTokenResolver) {
        return new BearerTokenAuthenticationFilter(authenticationManager, bearerTokenResolver);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            @Value("${openapi.auth.base-path:/api/v1/auth}") String basePath,
            BearerTokenAuthenticationFilter bearerTokenAuthenticationFilter
    ) throws Exception {
        http.authorizeHttpRequests(registry -> registry
                        .requestMatchers(basePath + "/login").anonymous()
                        .anyRequest().authenticated())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(config ->
                        config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .addFilterBefore(bearerTokenAuthenticationFilter, AuthorizationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用最低加密等级。因为前端传来的密码已经正常 10 强度加密，这里没必要再浪费服务器性能作强加密
        return new BCryptPasswordEncoder(4, new SecureRandom());
    }

    @Bean
    public AuthenticationManager authenticationManager(
            OpsJwtAuthenticationProvider opsJwtAuthenticationProvider) {
        return new ProviderManager(
                opsJwtAuthenticationProvider);
    }
}
