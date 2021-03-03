package com.onlinegame.game.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.annotation.security.RolesAllowed;

@Controller
public class MainController {

    @GetMapping("/games")
    public String games(){
        return "games";
    }

    @GetMapping("/moderation")
    @PreAuthorize("hasAuthority('moderation')")
    public String moderation(){
        return "moderation";
    }
}
