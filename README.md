# What? When? Where?

## Content of ReadMe

* [Project structure](#project-structure)
* [Features](#features)
* [Technologies](#technologies)
* [Use case example](#use-case-example)
* [Developer note](#developer-note)


## Project Structure
This project was developed during my university web programming course. This repo contains:
* Backed (Java + Spring Boot (MVC, Security, WebSockets) + MySql)
* Frontend (native JS + JQuery)

## Features
The application has following features:

* User Managing
  * Signing up with email confirmation
  * Singing in with checks on active sessions
  * Changing password (again with email confirmation)
  * Managing user preferences (nickname, pfp)
* Social activities
  * Global chat
  * Lobby chat
  * Friends list
* Game activities
  * Search lobby by its name
  * Creating game lobbies (with lobby state managment; e.g. if lobby is empty it is automatically deleted)
  * Change host in lobby
* Ticket system
  * Send ticket with complaint
  * Review ticket (Admin privilege)
  * Ban user (Admin privilege)
* Statistics
  * Individual statistics (win-loose)
 
 ## Technologies
 
 Spring Boot was used with dependencies (Lombok, Data-JPA, Security, Web, Thymeleaf, WebSocket, Validation).
 
 All activities with chat, game events was made with help of websockets.
 
 
 
 ## Use Case Example
 Most primitive way to interract with this game (sequence of action for one primitive actor-player):
 1) Log in
 2) Find lobby
 3) Set yourself ready
 4) Get question
 5) Discuss it with your team
 6) Answer
 7) Repeat until game is over
 
 ## Developer Note
 As it app was developed almost by one student with no practical experiece with Spring and Java, application design contains anti-patterns and code quallity leaves much to be desired (:/).
