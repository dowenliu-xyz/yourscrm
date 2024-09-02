package cn.yourscrm.mono.config;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jetbrains.annotations.NotNull;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@RequiredArgsConstructor
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@ConditionalOnBean({WebConfigProps.class, Tracer.class})
public class TraceIdRespHeaderFilter extends OncePerRequestFilter {
    private final WebConfigProps props;
    private final Tracer tracer;

    @Override
    protected void doFilterInternal(@NotNull HttpServletRequest request,
                                    @NotNull HttpServletResponse response,
                                    @NotNull FilterChain filterChain) throws ServletException, IOException {
        filterChain.doFilter(request, response);
        Span span = tracer.currentSpan();
        if (span == null) return;
        response.addHeader(props.getTraceIdResponseHeaderName(), span.context().traceId());
    }
}
