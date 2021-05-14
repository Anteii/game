package com.onlinegame.game.controller;

import com.onlinegame.game.dto.TicketProcessDto;
import com.onlinegame.game.model.Ticket;
import com.onlinegame.game.service.TicketService;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

@Controller
@RequestMapping("/moderation")
@PreAuthorize("hasAuthority('moderation')")
public class ModerationController {

    private TicketService ticketService;

    public ModerationController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @GetMapping("/tickets")
    public String moderation(Model model) {
        List<Ticket> ticketList = ticketService.getAllTickets();
        model.addAttribute("ticketList", ticketList);
        return "moderation";
    }

    @PostMapping("/processTicket")
    @ResponseBody
    public void processTicket(TicketProcessDto dto){

        if (dto.getAction().equals("ACCEPT")){
            ticketService.acceptTicket(dto.getTicketId());
        }
        else if (dto.getAction().equals("REJECT")){
            ticketService.rejectTicket(dto.getTicketId());
        }
    }
}
