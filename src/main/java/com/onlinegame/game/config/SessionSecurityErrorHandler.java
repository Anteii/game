package com.onlinegame.game.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class SessionSecurityErrorHandler extends SimpleUrlAuthenticationFailureHandler {

    private SessionRegistry sessionRegistry;
    @Autowired
    public SessionSecurityErrorHandler(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        if (exception.getClass().isAssignableFrom(SessionAuthenticationException.class)) {
            request.getRequestDispatcher("/auth/double_login_warning")
                    .forward(request, response);

        }
        else{
            request.getRequestDispatcher("/auth/login-error").forward(request, response);
        }
    }
}
