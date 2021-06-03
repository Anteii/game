package com.onlinegame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OutputGlobalMessage {
    private String from;
    private String text;
    private Instant time;
    private String username;
}
