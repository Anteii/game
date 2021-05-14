package com.onlinegame.game.controller;

import com.onlinegame.game.dto.UserForm;
import com.onlinegame.game.exceptions.EmailClientException;
import com.onlinegame.game.exceptions.InvalidTokenException;
import com.onlinegame.game.exceptions.SessionException;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.UserRepository;
import com.onlinegame.game.service.AuthService;
import com.onlinegame.game.service.SessionService;
import com.onlinegame.game.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.util.Optional;

@Controller
@RequestMapping("/auth")
public class AuthController {

    private final SessionService sessionService;
    private final UserService userService;
    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(SessionService sessionService, UserService userService, AuthService authService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.sessionService = sessionService;
        this.userService = userService;
        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping("/login")
    public String login(){
        if (isUserAuthenticated())
            return "redirect:/games";
        return "login";
    }

    @GetMapping("/verify")
    public String verifyAccount(@RequestParam("token") String token) {
        try {
            authService.verifyUser(token);
        } catch (InvalidTokenException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token");
        }
        return "forward:login";
    }

    @GetMapping("/login-error")
    public String loginError(Model model){
        if (isUserAuthenticated()){
            return "redirect:/games";
        }
        model.addAttribute("loginError", true);
        return "login";
    }

    @GetMapping("/signup")
    public String signUp(Model model){
        if (isUserAuthenticated()){
            return "redirect:/games";
        }
        UserForm user = new UserForm();
        model.addAttribute("user", user);
        return "signup";
    }

    @PostMapping("/signup")
    public String signUpPost(@ModelAttribute("user") @Valid UserForm userFrom, BindingResult bindingResult,
                             @RequestParam("image") MultipartFile multipartFile, Model model){
        if (isUserAuthenticated()){
            return "redirect:/games";
        }
        if (!userService.isFileSuitable(multipartFile)){
                model.addAttribute("imageSizeError", true);
                return "signup";
        }
        else if (bindingResult.hasErrors())
            return "signup";
        else if (!(userService.isUsernameFree(userFrom.getUsername()) && userService.isEmailFree(userFrom.getEmail()) )){
            model.addAttribute("usernameIsTaken", !userService.isUsernameFree(userFrom.getUsername()));
            model.addAttribute("emailIsTaken", !userService.isEmailFree(userFrom.getEmail()));
            return "signup";
        }

        User user = userService.createNewUser(userFrom, multipartFile);
        try {
            authService.sendVerificationEmail(user);
        }catch (EmailClientException e){
            user = userRepository.findByUsername(user.getUsername()).orElseThrow();
            userRepository.delete(user);
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Server couldn't send an activation email, try to signUp later", e
            );
        }
        return "redirect:/games";
    }

    @RequestMapping("/double_login_warning")
    public String doubleLogin(HttpServletRequest request, HttpServletResponse response, Model model) {
        if (isUserAuthenticated())
            return "redirect:/games";
        try {
            sessionService.closeUserSessions(request.getParameter("username"));
        } catch (SessionException e){
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "You already have a opened session, which server can't close",
                    e
            );
        }
        model.addAttribute("doubleSessionError", true);
        return "login";
    }

    @GetMapping("/forget")
    public String forget(Model model){
        if (isUserAuthenticated())
            return "redirect:/games";
        model.addAttribute("user", new UserForm());
        return "forget-password";
    }

    @PostMapping("/forget")
    public String forget(@ModelAttribute("user") UserForm userForm, Model model){
        if (isUserAuthenticated())
            return "redirect:/games";
        Optional<User> user = userRepository.findByEmail(userForm.getEmail());
        if (user.isEmpty()){
            model.addAttribute("emailIsFree", true);
            return "forget-password";
        }
        try {
            authService.sendRecoverEmail(user.get());
        } catch (EmailClientException e){
            throw new ResponseStatusException(
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "Server can't send password-recover email",
                    e
            );
        }
        return "redirect:/login";
    }

    @GetMapping("/recover")
    public String recoverAccount(@RequestParam("token") String token, Model model) {
        if (isUserAuthenticated())
            return "redirect:/games";
        try {
            User user = authService.getUserByToken(token).orElseThrow();
            UserForm userForm = new UserForm();
            userForm.setUsername(user.getUsername());
            model.addAttribute("user", userForm);
            model.addAttribute("token", token);
            return "recover-password";
        } catch (InvalidTokenException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token", e);
        }
    }

    @PostMapping("/recover")
    public String recoverAccountPost(@ModelAttribute("user")  @Valid UserForm userForm,
                                     BindingResult bindingResult,
                                     @ModelAttribute("token") String token) {
        if (isUserAuthenticated())
            return "redirect:/games";
        if (bindingResult.hasFieldErrors("password")){
            return "recover-password";
        }
        try {
            User user = authService.getUserByToken(token).orElseThrow();
            user.setPassword(passwordEncoder.encode(userForm.getPassword()));
            userRepository.save(user);
            return "redirect:/login";
        } catch (InvalidTokenException e){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid token", e);
        }
    }

    @RequestMapping("/you-are-banned")
    private String youAreBanned(){
        return "ban-page";
    }

    private boolean isUserAuthenticated(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && !(authentication instanceof AnonymousAuthenticationToken);
    }
}
