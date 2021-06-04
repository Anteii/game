package com.onlinegame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Collection;

@Entity
@Table(name = "user")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long userId;
    @NotBlank(message = "username is required")
    @Column(unique = true)
    private String username;
    @NotBlank(message = "password is required")
    private String password;
    @NotBlank(message = "full name is required")
    private String name;
    @NotBlank(message = "Ingame name is required")
    private String nickname;
    @NotBlank(message = "email is required")
    @Email(message = "Email should be valid")
    @Column(unique = true)
    private String email;
    @Column
    private String avatarPic;
    @Column
    private Integer score;
    @Column
    private Integer winedGames;
    @Column
    private Integer totalGames;
    @Column
    private Integer position;
    @Column
    @Enumerated(value = EnumType.STRING)
    private Role role;
    @Column
    private Boolean isEnabled;
    @Column
    private java.time.Instant creationDate;
    @Column
    private Boolean isBanned = false;
    @Column
    private Boolean inGame = false;
    @ManyToMany(mappedBy = "users")
    private Collection<Game> games;

}
