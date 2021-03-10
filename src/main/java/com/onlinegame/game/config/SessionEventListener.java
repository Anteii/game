package com.onlinegame.game.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.session.SessionInformation;
import org.springframework.security.core.session.SessionRegistry;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.session.HttpSessionEventPublisher;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpSessionEvent;

@Component
public class SessionEventListener extends HttpSessionEventPublisher {

    private final SessionRegistry sessionRegistry;

    @Autowired
    public SessionEventListener(SessionRegistry sessionRegistry) {
        this.sessionRegistry = sessionRegistry;
    }

    @Override
    public void sessionCreated(HttpSessionEvent event) {
        super.sessionCreated(event);
        event.getSession().setMaxInactiveInterval(60*10);

        System.out.println("session created");
    }

    @Override
    public void sessionDestroyed(HttpSessionEvent event) {
        String name=null;
        //----Находим login пользователя с помощью SessionRegistry
        SessionInformation sessionInfo = (sessionRegistry != null ? sessionRegistry
                .getSessionInformation(event.getSession().getId()) : null);
        UserDetails ud = null;
        if (sessionInfo != null) ud = (UserDetails) sessionInfo.getPrincipal();
        if (ud != null) {
            name=ud.getUsername();
            System.out.println(name + " session was destroyed");
            //Меняем статус на оффлайн
        }
        super.sessionDestroyed(event);
    }
}
