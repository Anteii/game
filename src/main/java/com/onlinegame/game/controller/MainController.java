package com.onlinegame.game.controller;

import com.onlinegame.game.dto.UserForm;
import com.onlinegame.game.model.Game;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.UserRepository;
import com.onlinegame.game.service.GameService;
import com.onlinegame.game.service.UserService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;

@Controller
public class MainController {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final GameService gameService;

    public MainController(UserRepository userRepository, UserService userService, PasswordEncoder passwordEncoder, GameService gameService) {
        this.userRepository = userRepository;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
        this.gameService = gameService;
    }

    @GetMapping("/games")
    public String games(Model model){
        List<User> friendsList = userService.getUserFriends(getCurrentUser());
        model.addAttribute("friendsList", friendsList);
        model.addAttribute("user", getCurrentUser());
        return "games";
    }

    @GetMapping("/profile/{username}")
    public String profile(Model model, @PathVariable String username){
        User currentUser = getCurrentUser();
        User user1 = userRepository.findByUsername(username).orElseThrow();
        boolean isMe = username.equals(currentUser.getUsername());
        model.addAttribute("user", user1);
        model.addAttribute("isMe", isMe);
        System.out.println(userService.getUserFriends(currentUser));
        model.addAttribute("friendsList", userService.getUserFriends(currentUser));
        return "profile";
    }

    @GetMapping("/profile/{username}/settings")
    public String settings(Model model, @PathVariable String username){
        User currentUser = getCurrentUser();
        User user1 = userRepository.findByUsername(username).orElseThrow();
        if (!currentUser.getUsername().equals(user1.getUsername())){
            throw new AccessDeniedException("Access to settings page denied");
        }
        model.addAttribute("user", currentUser);
        return "settings";
    }

    @PostMapping("/profile/{username}/settings")
    public String applySettings(Model model, @PathVariable String username,
                                @ModelAttribute("user") UserForm userForm, BindingResult bindingResult){
        if(bindingResult.hasErrors()){
            return "settings";
        }
        User user = userRepository.findByUsername(username).orElseThrow();
        user.setNickname(userForm.getNickname());
        user.setName(userForm.getName());
        if(!userForm.getPassword().isBlank()){
            user.setPassword(passwordEncoder.encode(userForm.getPassword()));
        }
        userRepository.save(user);
        return "redirect:settings";
    }

    @GetMapping("/game/{id}")
    public String game(@PathVariable Long id){
        User currentUser = getCurrentUser();
        Game game = gameService.getGameById(id);
        int N = 2; // максимальное допустимое количество игроков
        if (gameService.isActive(game.getGameName())){
            if (game.getUsers().size() == N && game.getUsers().stream().noneMatch(x->x.equals(currentUser)))
                return "redirect:/games";
            if (!game.getHost().equals(currentUser)){
                gameService.connectToTheGame(game.getGameId(), currentUser);
            }
            return "game";
        }
        return "redirect:/games";
    }

    @PostMapping("/game/leave-game")
    public String leaveGame(){
        return "redirect:/games";
    }

    private User getCurrentUser(){
        UserDetails principle = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principle.getUsername()).orElseThrow();
    }
}
