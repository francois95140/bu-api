package fr.esgi.bibliotheque.shared.logging;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.UUID;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class RequestLoggingFilter extends OncePerRequestFilter {

    private static final Logger log = LoggerFactory.getLogger(RequestLoggingFilter.class);
    private static final String TRACE_ID_HEADER = "X-Trace-Id";
    private static final String MDC_TRACE_KEY = "traceId";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain chain) throws ServletException, IOException {
        String traceId = request.getHeader(TRACE_ID_HEADER);
        if (traceId == null || traceId.isBlank()) {
            traceId = UUID.randomUUID().toString();
        }
        MDC.put(MDC_TRACE_KEY, traceId);
        response.setHeader(TRACE_ID_HEADER, traceId);

        long start = System.currentTimeMillis();
        log.info("→ {} {}", request.getMethod(), request.getRequestURI());
        try {
            chain.doFilter(request, response);
        } finally {
            log.info("← {} {} {}ms", request.getMethod(), request.getRequestURI(),
                System.currentTimeMillis() - start);
            MDC.remove(MDC_TRACE_KEY);
        }
    }
}
