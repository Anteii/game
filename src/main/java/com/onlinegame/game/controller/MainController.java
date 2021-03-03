package com.onlinegame.game.controller;

import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.UserRepository;
import com.onlinegame.game.security.SecurityUser;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Date;
import java.util.Optional;

@Controller
public class MainController {

    private final UserRepository userRepository;

    public MainController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping("/games")
    public String games(Model model){
        model.addAttribute("user", getCurrentUser());
        return "games";
    }

    @GetMapping("/moderation")
    @PreAuthorize("hasAuthority('moderation')")
    public String moderation(){
        return "moderation";
    }

    @GetMapping("/profile/{username}")
    public String profile(Model model, @PathVariable String username){
        User user1 = userRepository.findByUsername(username).orElseThrow();

        model.addAttribute("user", user1);
        model.addAttribute("d", new Date());
        return "profile";
    }
    @GetMapping("/profile/{username}/settings")
    public String settings(Model model, @PathVariable String username){
        User currentUser = getCurrentUser();
        User user1 = userRepository.findByUsername(username).orElseThrow();
        if (!currentUser.getUsername().equals(user1.getUsername())){
            throw new AccessDeniedException("sosi");
        }
        model.addAttribute("user", currentUser);
        return "settings";
    }

    public User getCurrentUser(){
        UserDetails principle = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principle.getUsername()).orElseThrow();
    }
}
