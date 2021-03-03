package com.onlinegame.game.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationFailureHandler;
import org.springframework.security.web.authentication.session.SessionAuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.context.request.RequestContextHolder;

import javax.servlet.ServletContext;
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
            //Переход на warning-page, передаем login через URL
            //Упрощено для примера (так передавать login не следует)
            request.getRequestDispatcher("/auth/double_login_warning")
                    .forward(request, response);

        }
    }

    public void expireUserSessions(String username) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof User) {
                UserDetails userDetails = (UserDetails) principal;
                if (userDetails.getUsername().equals(username)) {
                    for (SessionInformation information : sessionRegistry
                            .getAllSessions(userDetails, true)) {
                        //Заветное действие
                        information.expireNow();
                        //information.refreshLastRequest();
                        killExpiredSessionForSure(information.getSessionId());
                    }
                }
            }
        }
    }
    public void killExpiredSessionForSure(String id) {
        //Упрошен для примера
        //id - это SessionID, которую можно получить через
        //вызов метода  getSessionId() объекта SessionInformation
        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Cookie", "JSESSIONID=" + id);
            HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
            RestTemplate rt = new RestTemplate();
            rt.exchange("http://localhost:8080", HttpMethod.GET,
                    requestEntity, String.class);
        } catch (Exception ex) {} //для простоты не допустим никаких исключений
    }
}
