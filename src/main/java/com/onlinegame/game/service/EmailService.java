package com.onlinegame.game.service;

import com.onlinegame.game.exceptions.EmailClientException;
import com.onlinegame.game.exceptions.InvalidTokenException;
import com.onlinegame.game.model.User;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.mail.javamail.MimeMessagePreparator;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;
    private final EmailBuilder mailContentBuilder;

    void sendVerificationMail(User user) throws EmailClientException {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("WhatWhereWhen@email.com");
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject("Verify your account");
            messageHelper.setText(mailContentBuilder.buildVerificationEmail(user), true);
        };
        try {
            mailSender.send(messagePreparator);
            log.info("Activation email sent!!");
        } catch (MailException e) {
            log.error("Server couldn't send activation email", e);
            throw new EmailClientException("Server couldn't send activation email", e);
        }
    }
    String getUsernameFromToken(String token) throws InvalidTokenException {
        return mailContentBuilder.getUsernameFromToken(token);
    }

    boolean verifyToken(String token) throws InvalidTokenException {
        return mailContentBuilder.verifyToken(token);
    }

    void sendRecoverEmail(User user) throws EmailClientException {
        MimeMessagePreparator messagePreparator = mimeMessage -> {
            MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage);
            messageHelper.setFrom("WhatWhereWhen@email.com");
            messageHelper.setTo(user.getEmail());
            messageHelper.setSubject("Password restoring");
            messageHelper.setText(mailContentBuilder.buildRecoverEmail(user), true);
        };
        try {
            mailSender.send(messagePreparator);
            log.info("Password recover email sent!!");
        } catch (MailException e) {
            log.error("Server couldn't send password recover email", e);
            throw new EmailClientException("Server couldn't send password-recover email", e);

        }
    }
}
