package com.onlinegame.game.controller;

import com.onlinegame.game.dto.UserLoginDto;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/auth")
public class AuthController {

    @GetMapping("/login")
    public String login(){
        System.out.println(1);
        return "login";
    }

    @GetMapping("signup")
    public String signUp(){
        return "signup";
    }

}
