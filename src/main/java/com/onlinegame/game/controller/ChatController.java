package com.onlinegame.game.controller;

import com.onlinegame.game.model.GlobalMessage;
import com.onlinegame.game.model.Message;
import com.onlinegame.game.model.OutputGlobalMessage;
import com.onlinegame.game.model.User;
import com.onlinegame.game.repository.GlobalMessageRepository;
import com.onlinegame.game.repository.UserRepository;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;

import java.time.Instant;
import java.util.ArrayDeque;
import java.util.Queue;

@Controller
public class ChatController {
    private final UserRepository userRepository;
    private final GlobalMessageRepository globalMessageRepository;
    private Queue<OutputGlobalMessage> testList;
    private static final int GLOBAL_MESSAGE_CACHE_CAPACITY = 20;


    public ChatController(UserRepository userRepository, GlobalMessageRepository globalMessageRepository) {
        this.userRepository = userRepository;
        this.globalMessageRepository = globalMessageRepository;
        testList = new ArrayDeque<>(GLOBAL_MESSAGE_CACHE_CAPACITY);
    }

    @MessageMapping("/hello")
    @SendTo("/topic/greetings")
    public OutputGlobalMessage greeting(Message message) throws Exception {
        System.out.println(testList.size());
        User user = getCurrentUser();
        GlobalMessage globalMessage = new GlobalMessage();
        globalMessage.setUser(user);
        globalMessage.setDate(Instant.now());
        globalMessage.setText(message.getMessage());

        globalMessageRepository.save(globalMessage);
        OutputGlobalMessage outputGlobalMessage =
                new OutputGlobalMessage(user.getNickname(), message.getMessage(), Instant.now(), user.getUsername());
        cacheGlobalChatMessage(outputGlobalMessage);
        return outputGlobalMessage;
    }

    @MessageMapping("/history-load-request")
    @SendTo("/topic/history-load")
    public Queue<OutputGlobalMessage> loadHistory(Object obj) throws Exception {
        return testList;
    }
    private User getCurrentUser(){
        UserDetails principle = (UserDetails) SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        return userRepository.findByUsername(principle.getUsername()).orElseThrow();
    }
    private void cacheGlobalChatMessage(OutputGlobalMessage outputGlobalMessage){
        if (testList.size() == GLOBAL_MESSAGE_CACHE_CAPACITY){
            testList.poll();
        }
        testList.add(outputGlobalMessage);
    }
}
