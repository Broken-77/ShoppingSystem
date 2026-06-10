package com.wms.shoppingsys.common;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * SPA fallback filter: non-API GET requests that would 404 get index.html instead.
 */
@Component
public class SpaFallbackFilter implements Filter {
    private String indexHtml;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        try {
            indexHtml = StreamUtils.copyToString(
                new ClassPathResource("static/index.html").getInputStream(),
                StandardCharsets.UTF_8);
        } catch (IOException e) {
            indexHtml = "<html><body>Shopping System</body></html>";
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String path = req.getRequestURI();
        // Only intercept GET requests to non-API, non-static-resource paths
        if ("GET".equalsIgnoreCase(req.getMethod()) && !path.startsWith("/api/")
                && !path.contains(".")) {
            // Check if this path maps to a real resource
            String resourcePath = "static" + path;
            if (!new ClassPathResource(resourcePath).exists()
                    && !new ClassPathResource(resourcePath + ".html").exists()) {
                response.setContentType("text/html;charset=UTF-8");
                response.getWriter().write(indexHtml);
                return;
            }
        }
        chain.doFilter(request, response);
    }
}
