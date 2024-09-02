package cn.yourscrm.mono.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.springframework.http.HttpHeaders;

import java.util.regex.Pattern;

public class DefaultBearerTokenResolver implements BearerTokenResolver {
    private static final Pattern AUTHORIZATION_PATTERN =
            Pattern.compile("^Bearer (?<token>[a-zA-Z0-9-._~+/]+=*)$",
                    Pattern.CASE_INSENSITIVE);
    private final String bearerTokenHeaderName;

    public DefaultBearerTokenResolver(String bearerTokenHeaderName) {
        this.bearerTokenHeaderName = bearerTokenHeaderName;
    }

    public DefaultBearerTokenResolver() {
        this(HttpHeaders.AUTHORIZATION);
    }

    @Override
    public @Nullable String resolve(@NotNull HttpServletRequest request) {
        String header = request.getHeader(bearerTokenHeaderName);
        if (header == null) {
            return null;
        }
        var matcher = AUTHORIZATION_PATTERN.matcher(header);
        if (!matcher.matches()) {
            return null;
        }
        return matcher.group("token");
    }
}
