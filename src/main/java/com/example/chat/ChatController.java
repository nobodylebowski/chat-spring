package com.example.chat;

import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class ChatController {

    @GetMapping("/chat")
    public String chat(HttpSession session, Model model) {
        User me = (User) session.getAttribute("user");
        if (me == null) {
            return "redirect:/";
        }
        model.addAttribute("name", me.getName());
        return "chat";
    }
}
