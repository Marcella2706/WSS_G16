package com.wss.csrf.controller;

import com.wss.csrf.model.AppUser;
import com.wss.csrf.model.AuditEntry;
import com.wss.csrf.model.Message;
import com.wss.csrf.repository.AuditRepository;
import com.wss.csrf.repository.MessageRepository;
import com.wss.csrf.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AppController {

    private final UserService userService;
    private final MessageRepository messages;
    private final AuditRepository audit;

    public AppController(UserService userService, MessageRepository messages, AuditRepository audit) {
        this.userService = userService;
        this.messages = messages;
        this.audit = audit;
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> me(@AuthenticationPrincipal UserDetails userDetails) {
        AppUser user = userService.getUser(userDetails.getUsername());
        return ResponseEntity.ok(Map.of(
            "username", user.getUsername(),
            "role", user.getRole(),
            "email", user.getEmail()
        ));
    }

    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        if (token == null) return ResponseEntity.ok(Map.of());
        return ResponseEntity.ok(Map.of("token", token.getToken(), "headerName", token.getHeaderName()));
    }

    @GetMapping("/projects")
    public ResponseEntity<List<Map<String, String>>> projects() {
        return ResponseEntity.ok(List.of(
            Map.of("name", "payments-api",  "status", "production",  "visibility", "private"),
            Map.of("name", "web-frontend",  "status", "production",  "visibility", "private"),
            Map.of("name", "mobile-app",    "status", "beta",        "visibility", "private"),
            Map.of("name", "data-pipeline", "status", "development", "visibility", "internal")
        ));
    }

    @GetMapping("/feed")
    public ResponseEntity<List<Map<String, Object>>> feed() {
        return ResponseEntity.ok(
            messages.findAllByOrderByCreatedAtDesc().stream()
                .map(m -> Map.<String, Object>of(
                    "id", m.getId(),
                    "author", m.getAuthor(),
                    "text", m.getText(),
                    "createdAt", m.getCreatedAt().toString()
                ))
                .toList()
        );
    }

    @PostMapping("/feed")
    public ResponseEntity<Map<String, Object>> post(@RequestParam String text,
                                                    @AuthenticationPrincipal UserDetails me) {
        Message saved = messages.save(new Message(me.getUsername(), text));
        return ResponseEntity.ok(Map.of("id", saved.getId(), "status", "ok"));
    }

    @GetMapping("/admin/users")
    public ResponseEntity<List<Map<String, String>>> listUsers() {
        return ResponseEntity.ok(
            userService.getAllUsers().stream()
                .map(u -> Map.of("username", u.getUsername(), "role", u.getRole(), "email", u.getEmail()))
                .toList()
        );
    }

    @GetMapping("/admin/secrets")
    public ResponseEntity<List<Map<String, String>>> secrets() {
        return ResponseEntity.ok(List.of(
            Map.of("name", "AWS_PROD_ACCESS_KEY", "value", "AKIA4XDEMO7PRODKEY99", "scope", "production"),
            Map.of("name", "STRIPE_LIVE_SECRET",  "value", "sk_live_51Demo9ProdStripeKeyZ",  "scope", "production"),
            Map.of("name", "DEPLOY_TOKEN",        "value", "ghp_demoDeployTokenForProdPipes", "scope", "ci/cd"),
            Map.of("name", "DB_ROOT_PASSWORD",    "value", "Pr0d-Postgre$-R00t-2026",         "scope", "database")
        ));
    }

    @GetMapping("/admin/audit")
    public ResponseEntity<List<Map<String, Object>>> auditLog() {
        return ResponseEntity.ok(
            audit.findAllByOrderByCreatedAtDesc().stream()
                .map(a -> Map.<String, Object>of(
                    "actor", a.getActor(),
                    "action", a.getAction(),
                    "createdAt", a.getCreatedAt().toString()
                ))
                .toList()
        );
    }

    @PostMapping("/admin/update-role")
    public ResponseEntity<Map<String, Object>> updateRole(@RequestParam String username,
                                                          @RequestParam String role,
                                                          Principal actor) {
        userService.updateRole(username, role);
        String who = actor != null ? actor.getName() : "unknown";
        audit.save(new AuditEntry(who, "set role of '" + username + "' to " + role));
        Map<String, Object> out = new HashMap<>();
        out.put("status", "ok");
        out.put("username", username);
        out.put("role", role);
        return ResponseEntity.ok(out);
    }
}
