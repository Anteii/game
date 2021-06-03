package com.onlinegame.game.restController;

import com.onlinegame.game.dto.TicketDto;
import com.onlinegame.game.model.Ticket;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.UserRepository;
import com.onlinegame.game.service.TicketService;
import com.onlinegame.game.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;


@RestController
@RequestMapping("/social")
public class SocialController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final TicketService ticketService;

    public SocialController(UserRepository userRepository, UserService userService, TicketService ticketService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.ticketService = ticketService;
    }

    @DeleteMapping("/delete-friend")
    public ResponseEntity<String> deleteFriend(String username){
        User user = getCurrentUser();

        if (user.getUsername().equals(username))
            return new ResponseEntity<>("{}", HttpStatus.FORBIDDEN);

        userService.deleteFriend(user, username);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
    @PatchMapping("/add-friend")
    public ResponseEntity<String> addFriend(String username){
        User user = getCurrentUser();
        if (user.getUsername().equals(username))
            return new ResponseEntity<>("{}", HttpStatus.FORBIDDEN);
        userService.addFriend(user, username);
        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
    @PostMapping("/send-ticket")
    public ResponseEntity<String> sendTicket(TicketDto ticketDto){
        Ticket ticket = new Ticket();
        User sender = getCurrentUser();
        User suspect = userRepository.findByUsername(ticketDto.getUsername()).orElseThrow();
        if (sender.equals(suspect))
            return new ResponseEntity<>("{}", HttpStatus.FORBIDDEN);

        ticket.setDescription(ticketDto.getText());
        ticket.setSuspect(suspect);
        ticket.setSender(sender);
        ticket.setTheme(ticketDto.getTheme());
        ticket.setCreationDate(Instant.now());
        ticket.setStatus(false);
        ticketService.createTicket(ticket);

        return new ResponseEntity<>("{}", HttpStatus.OK);
    }
    private User getCurrentUser(){
        UserDetails principle = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principle.getUsername()).orElseThrow();
    }
}
