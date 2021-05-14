package com.onlinegame.game.service;

import com.onlinegame.game.exceptions.InvalidTokenException;
import com.onlinegame.game.model.User;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.encrypt.Encryptors;
import org.springframework.security.crypto.encrypt.TextEncryptor;
import org.springframework.security.crypto.keygen.KeyGenerators;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import javax.annotation.PostConstruct;

@Service
public class EmailBuilder {
    private final TemplateEngine templateEngine;
    @Value("${email.token}")
    private String emailToken;
    private String host;
    private String scheme;
    private String verifyPath;
    private String recoverPath;
    private TextEncryptor emailCipherObject;

    public EmailBuilder(TemplateEngine templateEngine) {
        host = "localhost:8080";
        scheme = "http";
        verifyPath = "/auth/verify";
        recoverPath = "/auth/recover";
        this.templateEngine = templateEngine;
    }
    @PostConstruct
    void init(){
        emailCipherObject = Encryptors.text(emailToken, KeyGenerators.string().generateKey());
    }

    String buildVerificationEmail(User user) {
        Context context = new Context();
        String token = emailCipherObject.encrypt(user.getUsername()+"."+emailToken);
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(scheme).host(host).path(verifyPath).queryParam("token", token).build();

        context.setVariable("link", uriComponents.toString());

        return templateEngine.process("mail/mail-verification-template", context);
    }

    String getUsernameFromToken(String token) throws InvalidTokenException {
        try {
            return emailCipherObject.decrypt(token).split("\\.")[0];
        } catch (Exception e){
            throw new InvalidTokenException("Invalid token", e);
        }
    }

    boolean verifyToken(String token) throws InvalidTokenException{
        try {
            return emailCipherObject.decrypt(token).split("\\.")[1].equals(emailToken);
        } catch (Exception e){
            throw new InvalidTokenException("Invalid token", e);
        }

    }

    public String buildRecoverEmail(User user) {
        Context context = new Context();
        String token = emailCipherObject.encrypt(user.getUsername()+"."+emailToken);
        UriComponents uriComponents = UriComponentsBuilder.newInstance()
                .scheme(scheme).host(host).path(recoverPath)
                .queryParam("token", token)
                .build();

        context.setVariable("link", uriComponents.toString());

        return templateEngine.process("mail/mail-password-recovery-template.html", context);
    }
}
