package com.onlinegame.game.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.*;

@Entity
@Table(name = "friendship")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Friendship {
  @Id
  @GeneratedValue(strategy = GenerationType.SEQUENCE)
  private long friendshipId;
  @Column
  private String status;
  @Column
  private java.sql.Timestamp date;

  @ManyToOne(
    cascade = CascadeType.ALL, fetch = FetchType.EAGER
  )
  @JoinColumn(
    name = "userOneID", referencedColumnName = "userID"
  )
  private User userOne;
  @ManyToOne(
          cascade = CascadeType.ALL, fetch = FetchType.EAGER
  )
  @JoinColumn(
          name = "userTwoID", referencedColumnName = "userID"
  )
  private User userTwo;
}
