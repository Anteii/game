package com.onlinegame.game.service;

import com.onlinegame.game.exceptions.EmailClientException;
import com.onlinegame.game.exceptions.InvalidTokenException;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {

    private final EmailService emailService;
    private final UserService userService;
    private final UserRepository userRepository;

    public AuthService(EmailService emailService, UserService userService, UserRepository userRepository) {
        this.emailService = emailService;
        this.userService = userService;
        this.userRepository = userRepository;
    }

    public void sendVerificationEmail(User user) throws EmailClientException {
       emailService.sendVerificationMail(user);
    }
    public void sendRecoverEmail(User user) throws EmailClientException{
       emailService.sendRecoverEmail(user);
    }

    public void verifyUser(String token) throws InvalidTokenException {
        String username = emailService.getUsernameFromToken(token);
        if (emailService.verifyToken(token))
            userService.activateUser(username);
        else
            throw new InvalidTokenException("Invalid verifying token");
    }

    public Optional<User> getUserByToken(String token) throws InvalidTokenException {
        String username = emailService.getUsernameFromToken(token);
        if (emailService.verifyToken(token))
            return userRepository.findByUsername(username);
        else
            throw new InvalidTokenException("Invalid recovery token");
    }
}
