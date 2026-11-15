package com.wss.csrf.controller;

import com.wss.csrf.model.AppUser;
import com.wss.csrf.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.csrf.CsrfToken;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
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

    /** Current session's identity — drives the frontend UI. */
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

    /**
     * Hands the anti-CSRF token to the legitimate SPA. When protection is OFF
     * there is no token, so this returns an empty object. A forged cross-site
     * page cannot call this and read the response (same-origin policy / CORS).
     */
    @GetMapping("/csrf")
    public ResponseEntity<Map<String, String>> csrf(HttpServletRequest request) {
        CsrfToken token = (CsrfToken) request.getAttribute("_csrf");
        if (token == null) return ResponseEntity.ok(Map.of());
        return ResponseEntity.ok(Map.of(
            "token", token.getToken(),
            "headerName", token.getHeaderName()
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

    /**
     * === THE VULNERABLE CODE PATH ===
     * A state-changing action (privilege escalation) exposed over POST.
     * When app.csrf.protection=false, Spring requires NO proof of intent:
     * the only thing gating it is the session cookie, which the browser
     * attaches automatically to any request — including a forged one.
     */
    @PostMapping("/admin/update-role")
    public ResponseEntity<Map<String, Object>> updateRole(@RequestParam String username,
                                                          @RequestParam String role) {
        userService.updateRole(username, role);
        Map<String, Object> out = new HashMap<>();
        out.put("status", "ok");
        out.put("username", username);
        out.put("role", role);
        return ResponseEntity.ok(out);
    }
}
