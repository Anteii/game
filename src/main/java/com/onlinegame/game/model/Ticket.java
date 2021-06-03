package com.onlinegame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;
import java.time.Instant;

@Entity
@Table(name= "Ticket")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Ticket {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long ticketId;

    public String theme;
    public String description;
    public Boolean status;
    public Instant creationDate;
    public Instant closingDate;
    @ManyToOne
    @JoinColumn(
            name = "senderId",
            referencedColumnName = "userId"
    )
    public User sender;
    @ManyToOne
    @JoinColumn(
            name = "suspectId",
            referencedColumnName = "userId"
    )
    public User suspect;
}
