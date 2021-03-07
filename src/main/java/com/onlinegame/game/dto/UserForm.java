package com.onlinegame.game.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserForm {
    @NotBlank(message = "username is required")
    private String username;
    @NotBlank(message = "password is required")
    private String password;
    @NotBlank(message = "full name is required")
    private String name;
    @NotBlank(message = "nick")
    private String nickname;
    @NotBlank(message = "email is required")
    @Email
    private String email;
}
