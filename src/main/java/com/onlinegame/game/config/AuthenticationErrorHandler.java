package com.onlinegame.game.config;

import com.onlinegame.game.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class AuthenticationErrorHandler extends SimpleUrlAuthenticationFailureHandler {

    @Autowired
    private UserService userService;
/*
* All these comments below are here just in case
* */
    @Override
    public void onAuthenticationFailure(HttpServletRequest request, HttpServletResponse response, AuthenticationException exception)
            throws IOException, ServletException {
        if (exception.getClass().isAssignableFrom(SessionAuthenticationException.class)) {
            request.getRequestDispatcher("/auth/double_login_warning").forward(request, response);
            //System.out.println(request.getParameter("username"));
            //System.out.println(request.getParameter("password"));
            //response.setHeader("username", request.getParameter("username"));
            //response.setHeader("password", request.getParameter("password"));
            //PrintWriter writer = response.getWriter();
            //writer.print("username="+request.getParameter("username") + "&");
            //writer.print("password="+request.getParameter("password"));
            //response.sendRedirect("/auth/double_login_warning");
        }
        else if (exception.getClass().isAssignableFrom(LockedException.class)) {
            response.sendRedirect("/auth/you-are-banned");
        }
        else{
            //request.getRequestDispatcher("/auth/login-error").forward(request, response);
            //String existsParam = userService.isExisted(request.getParameter("username")).toString();
            //response.sendRedirect("/auth/login-error?exists="+existsParam);
            response.sendRedirect("/auth/login-error");
        }
    }
}
