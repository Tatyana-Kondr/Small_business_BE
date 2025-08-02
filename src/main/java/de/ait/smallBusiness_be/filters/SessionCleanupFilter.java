package de.ait.smallBusiness_be.filters;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.io.Serializable;
import java.util.Enumeration;

@Component
public class SessionCleanupFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        if (request instanceof HttpServletRequest httpRequest) {
            HttpSession session = httpRequest.getSession(false);
            if (session != null) {
                Enumeration<String> names = session.getAttributeNames();
                while (names.hasMoreElements()) {
                    String name = names.nextElement();
                    Object value = session.getAttribute(name);

                    if (!(value instanceof Serializable)) {
                        session.removeAttribute(name);
                        System.out.println("⚠️ Removed non-serializable session attribute: " + name);
                    }

                    if (name.startsWith("org.springframework")) {
                        session.removeAttribute(name);
                        System.out.println("🧹 Removed Spring-specific attribute: " + name);
                    }
                }
            }
        }

        chain.doFilter(request, response);
    }
}

