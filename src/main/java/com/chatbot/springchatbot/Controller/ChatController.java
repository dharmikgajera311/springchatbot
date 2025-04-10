package com.chatbot.springchatbot.Controller;

import com.chatbot.springchatbot.Service.ChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/chat")
@CrossOrigin("*")
public class ChatController {

    @Autowired
    private ChatService chatService;

    @PostMapping
    public String chat(@RequestBody String userMessage) {
        try {
            return chatService.getChatGPTResponse(userMessage);
        } catch (Exception e) {
            return "Error: " + e.getMessage();
        }
    }
}