package com.onlinegame.game.model;

import lombok.*;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import java.util.Collection;
import java.util.Date;

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
    private java.sql.Timestamp creationDate;

    @ManyToMany(mappedBy = "users")
    private Collection<Game> games;

    public String getJavaDate(){
        return creationDate.toString();
    }
}
