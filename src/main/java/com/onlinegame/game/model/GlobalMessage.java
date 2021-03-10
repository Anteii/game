package com.onlinegame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@AllArgsConstructor
@NoArgsConstructor
@Data
@Entity
@Table(name = "global_message")
public class GlobalMessage {
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ManyToOne
    @JoinColumn(
            name = "userId",
            referencedColumnName = "userId"
    )
    private User user;
    @Column
    private java.time.Instant date;
    @Column
    private String text;
}
