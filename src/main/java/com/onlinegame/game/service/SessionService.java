package com.onlinegame.game.service;

import com.onlinegame.game.exceptions.SessionException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;

@Service("expireUserService")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Slf4j
public class SessionService {

    private final SessionRegistry sessionRegistry;
    private String logoutUrl = "http://localhost:8080/auth/logout";

    public SessionService(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    //Метод для удаления сессии любого пользователя
    public void closeUserSessions(String username) throws SessionException {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof User) {
                UserDetails userDetails = (UserDetails) principal;
                if (userDetails.getUsername().equals(username)) {
                    for (SessionInformation information : sessionRegistry
                            .getAllSessions(userDetails, true)) {
                        information.expireNow();
                        killExpiredSessionForSure(information.getSessionId());
                        log.info(username + " session was successfully closed");
                    }
                }
            }
        }
    }

    private void killExpiredSessionForSure(String sessionId) throws SessionException {
        try {
            HttpHeaders requestHeaders = new HttpHeaders();
            requestHeaders.add("Cookie", "JSESSIONID=" + sessionId);
            HttpEntity requestEntity = new HttpEntity(null, requestHeaders);
            RestTemplate rt = new RestTemplate();
            rt.exchange(logoutUrl, HttpMethod.POST,
                    requestEntity, String.class);
        } catch (RestClientException e){
            log.error("Error closing opened session");
            throw new SessionException("Server can't close opened session", e);
        }
    }
}
