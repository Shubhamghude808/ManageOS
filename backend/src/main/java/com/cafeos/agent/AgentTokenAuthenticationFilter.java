package com.cafeos.agent;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.List;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

public class AgentTokenAuthenticationFilter extends OncePerRequestFilter {
    private final AgentTokenService tokens;
    public AgentTokenAuthenticationFilter(AgentTokenService tokens) { this.tokens = tokens; }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !request.getRequestURI().startsWith(request.getContextPath() + "/api/agent/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String authorization = request.getHeader("Authorization");
        if (authorization != null && authorization.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String rawToken = authorization.substring(7).trim();
            tokens.authenticate(rawToken).ifPresent(pcId -> {
                var auth = new UsernamePasswordAuthenticationToken(pcId, null,
                        List.of(new SimpleGrantedAuthority("ROLE_AGENT")));
                SecurityContextHolder.getContext().setAuthentication(auth);
            });
        }
        chain.doFilter(request, response);
    }
}
