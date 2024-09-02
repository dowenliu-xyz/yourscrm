package cn.yourscrm.mono.auth.security;

import cn.yourscrm.mono.auth.domain.jwt.SignKeyService;
import com.nimbusds.jwt.SignedJWT;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Component;

import java.text.ParseException;
import java.util.List;

@Component
public class OpsJwtAuthenticationProvider extends AbstractJwtAuthenticationProvider {
    private final OpsAuthenticationProps props;

    public OpsJwtAuthenticationProvider(SignKeyService signKeyService,
                                        OpsAuthenticationProps props) {
        super(signKeyService);
        this.props = props;
    }

    @Override
    protected JwtAuthenticationToken authenticated(BearerTokenAuthenticationToken bearer,
                                                   SignedJWT jwt) {
        WebAuthenticationDetails details = (WebAuthenticationDetails) bearer.getDetails();
        String remoteAddress = details.getRemoteAddress();
        boolean allowFromIP = false;
        for (var matcher : props.getMatchers()) {
            if (matcher.matches(remoteAddress)) {
                allowFromIP = true;
                break;
            }
        }
        if (!allowFromIP) return null;
        String usage;
        try {
            usage = jwt.getJWTClaimsSet().getStringClaim("usg");
        } catch (ParseException e) {
            throw new BadCredentialsException("Invalid JWT", e);
        }
        if (!"ops".equals(usage)) return null;
        return new JwtAuthenticationToken(
                List.of(new SimpleGrantedAuthority("ROLE_OPS")), jwt, jwt);
    }
}
