package com.wss.csrf.controller;

import com.wss.csrf.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AppController {

    private final UserService userService;

    @Value("${app.csrf.protection:false}")
    private boolean csrfProtection;

    public AppController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/")
    public String home() {
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        model.addAttribute("user", userService.getUser(userDetails.getUsername()));
        model.addAttribute("csrfProtection", csrfProtection);
        return "dashboard";
    }

    @GetMapping("/admin/users")
    public String adminPanel(Model model) {
        model.addAttribute("users", userService.getAllUsers());
        model.addAttribute("csrfProtection", csrfProtection);
        return "admin";
    }

    @PostMapping("/admin/update-role")
    public String updateRole(@RequestParam String username,
                             @RequestParam String role,
                             RedirectAttributes redirectAttributes) {
        userService.updateRole(username, role);
        redirectAttributes.addFlashAttribute("message", username + " → " + role);
        return "redirect:/admin/users";
    }
}
