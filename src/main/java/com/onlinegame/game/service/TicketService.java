package com.onlinegame.game.service;

import com.onlinegame.game.model.Ticket;
import com.onlinegame.game.repository.TicketRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TicketService {
    private TicketRepository ticketRepository;
    private UserService userService;
    public TicketService(TicketRepository ticketRepository, UserService userService) {
        this.ticketRepository = ticketRepository;
        this.userService = userService;
    }

    public List<Ticket> getAllTickets(){
        return ticketRepository.findAll();
    }

    public void acceptTicket(Long ticketId){
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        System.out.println(ticketId + " " + ticket.getSuspect().getUsername());
        userService.banUser(ticket.getSuspect().getUserId());
        ticket.setStatus(true);
        ticketRepository.save(ticket);
    }
    public void rejectTicket(Long ticketId){
        Ticket ticket = ticketRepository.findById(ticketId).orElseThrow();
        ticket.setStatus(true);
        ticketRepository.save(ticket);
    }
}
