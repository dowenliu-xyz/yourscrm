package cn.yourscrm.mono.auth.security;

import jakarta.servlet.http.HttpServletRequest;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@FunctionalInterface
public interface BearerTokenResolver {
    @Nullable String resolve(@NotNull HttpServletRequest request);
}
