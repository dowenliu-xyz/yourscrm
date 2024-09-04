package cn.yourscrm.mono.auth.security;

import cn.yourscrm.common.id.ArbitraryLongID;
import cn.yourscrm.mono.auth.domain.jwt.SignKey;
import cn.yourscrm.mono.auth.domain.jwt.SignKeyService;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.crypto.MACVerifier;
import com.nimbusds.jwt.JWT;
import com.nimbusds.jwt.JWTParser;
import com.nimbusds.jwt.SignedJWT;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

import java.text.ParseException;

@Slf4j
public abstract class AbstractJwtAuthenticationProvider implements AuthenticationProvider {
    private final SignKeyService signKeyService;

    protected AbstractJwtAuthenticationProvider(SignKeyService signKeyService) {
        this.signKeyService = signKeyService;
    }

    @Override
    public Authentication authenticate(Authentication authentication)
            throws AuthenticationException {
        BearerTokenAuthenticationToken bearer = (BearerTokenAuthenticationToken) authentication;
        JWT jwt = parseJwt(bearer.getToken());
        if (!(jwt instanceof SignedJWT signedJWT)) throw new BadCredentialsException("Invalid JWT");
        validateJwt(signedJWT);
        JwtAuthenticationToken authResult = authenticated(bearer, signedJWT);
        if (authResult == null) return null;
        if (authResult.getDetails() == null) {
            authResult.setDetails(bearer.getDetails());
        }
        return authResult;
    }

    private @NotNull JWT parseJwt(@NotNull String token) {
        try {
            return JWTParser.parse(token);
        } catch (ParseException e) {
            throw new BadCredentialsException("Invalid JWT", e);
        }
    }

    private void validateJwt(@NotNull SignedJWT jwt) throws AuthenticationException {
        long keyId;
        try {
            keyId = Long.parseLong(jwt.getHeader().getKeyID());
        } catch (NumberFormatException e) {
            throw new BadCredentialsException("Invalid JWT");
        }
        SignKey signKey = signKeyService.findByKeyId(new ArbitraryLongID(keyId));
        if (signKey == null || !signKey.canVerifyToken()) {
            throw new BadCredentialsException("Invalid JWT");
        }
        try {
            MACVerifier verifier = new MACVerifier(signKey.getSecret());
            if (!jwt.verify(verifier)) {
                throw new BadCredentialsException("Invalid JWT");
            }
        } catch (JOSEException e) {
            throw new BadCredentialsException("Invalid JWT", e);
        }
    }

    protected abstract JwtAuthenticationToken authenticated(BearerTokenAuthenticationToken bearer,
                                                            SignedJWT jwt);

    @Override
    public boolean supports(Class<?> authentication) {
        return BearerTokenAuthenticationToken.class.isAssignableFrom(authentication);
    }
}
