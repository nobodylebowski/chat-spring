package com.example.chat;

import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class ChatController {

    private final UserService userService;
    private final MessageService messageService;

    public ChatController(UserService userService,
                          MessageService messageService) {
        this.userService = userService;
        this.messageService = messageService;
    }

    @GetMapping("/chat")
    public String chat(@RequestParam(required = false) Integer with,
                       HttpSession session, Model model) {
        User me = (User) session.getAttribute("user");
        if (me == null) return "redirect:/";

        model.addAttribute("me", me);
        model.addAttribute("users", userService.findAllExcept(me.getId()));

        if (with != null) {
            User other = userService.findById(with);
            if (other == null) {
                model.addAttribute("error", "User not found");
            } else {
                model.addAttribute("other", other);
                model.addAttribute("messages", messageService.getConversation(me, other));
            }
        }
        return "chat";
    }

    @PostMapping("/send")
    public ResponseEntity<Void> send(@RequestParam int to,
                                     @RequestParam String content,
                                     HttpSession session) {
        User me = (User) session.getAttribute("user");
        if (me == null) return ResponseEntity.status(401).build();
        User other = userService.findById(to);
        if (other == null) return ResponseEntity.badRequest().build();

        try {
            messageService.send(me, other, content);
            return ResponseEntity.noContent().build();
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        }
    }
}
