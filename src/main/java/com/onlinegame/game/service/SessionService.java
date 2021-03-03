package com.onlinegame.game.service;

import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.client.RestTemplate;

@Service("expireUserService")
@Scope(value = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
public class SessionService {

    //Инжектим sessionRegistry
    private SessionRegistry sessionRegistry;
    @Autowired
    public void setSessionRegistry(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    //Метод для удаления сессии любого пользователя
    public void expireUserSessions(String username) {
        for (Object principal : sessionRegistry.getAllPrincipals()) {
            if (principal instanceof User) {
                UserDetails userDetails = (UserDetails) principal;
                if (userDetails.getUsername().equals(username)) {
                    for (SessionInformation information : sessionRegistry
                            .getAllSessions(userDetails, true)) {

                        //Заветное действие
                        information.expireNow();
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
            rt.exchange("http://localhost:8080/auth/logout", HttpMethod.POST,
                    requestEntity, String.class);
        } catch (Exception ex) {} //для простоты не допустим никаких исключений
    }
}
