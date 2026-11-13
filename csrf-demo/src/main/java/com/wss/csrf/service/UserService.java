package com.wss.csrf.service;

import com.wss.csrf.model.AppUser;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class UserService implements UserDetailsService {

    private final Map<String, AppUser> users = new ConcurrentHashMap<>();

    public UserService(PasswordEncoder encoder) {
        users.put("alice", new AppUser("alice", encoder.encode("admin123"), "ADMIN", "alice@devforge.io"));
        users.put("bob", new AppUser("bob", encoder.encode("user123"), "USER", "bob@devforge.io"));
        users.put("mallory", new AppUser("mallory", encoder.encode("hack123"), "USER", "mallory@evil.com"));
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        AppUser user = users.get(username);
        if (user == null) throw new UsernameNotFoundException(username);
        return User.withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }

    public List<AppUser> getAllUsers() {
        return new ArrayList<>(users.values());
    }

    public AppUser getUser(String username) {
        return users.get(username);
    }

    public void updateRole(String username, String role) {
        AppUser user = users.get(username);
        if (user != null) user.setRole(role);
    }
}
