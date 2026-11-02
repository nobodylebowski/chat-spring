package com.example.chat;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;


    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String loginPage() {
        return "login";
    }

    @PostMapping("/register")
    public String register(@RequestParam String username,
                           @RequestParam String password,
                           Model model) {
        try {
            userService.register(username, password);
            model.addAttribute("info", "Registered! You can login now");
        } catch(IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
        }
        return "login";
    }

    @PostMapping("/login")
    public String login(@RequestParam String username,
                        @RequestParam String password,
                        HttpServletRequest request,
                        Model model) {
        try {
            User user = userService.login(username, password);
            request.getSession();
            request.changeSessionId();
            request.getSession().setAttribute("user", user);
            return "redirect:/chat";
        } catch(IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            return "login";
        }
    }

    @PostMapping("/logout")
    public String logout(HttpSession session) {
            session.invalidate();
            return "redirect:/";
    }
}
