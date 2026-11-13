package com.wss.csrf.controller;

import com.wss.csrf.model.AppUser;
import com.wss.csrf.service.UserService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class ApiController {

    private final UserService userService;

    @Value("${app.csrf.protection:false}")
    private boolean csrfProtection;

    public ApiController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userService.getUser(userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
            "username", user.getUsername(),
            "role", user.getRole(),
            "email", user.getEmail(),
            "csrfProtection", csrfProtection
        ));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<Map<String, String>>> listUsers() {
        return ResponseEntity.ok(
            userService.getAllUsers().stream()
                .map(u -> Map.of(
                    "username", u.getUsername(),
                    "role", u.getRole(),
                    "email", u.getEmail()
                ))
                .toList()
        );
    }

    @PostMapping("/admin/update-role")
    public ResponseEntity<Map<String, String>> updateRole(@RequestParam String username,
                                                          @RequestParam String role) {
        userService.updateRole(username, role);
        return ResponseEntity.ok(Map.of("status", "ok", "username", username, "role", role));
    }
}
