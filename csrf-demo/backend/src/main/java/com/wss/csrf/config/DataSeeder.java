package com.wss.csrf.config;

import com.wss.csrf.model.AppUser;
import com.wss.csrf.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds the DevForge team into Postgres on startup (only if empty).
 *
 *   alice   / admin123  -> ADMIN     (the victim: a logged-in admin)
 *   bob     / user123   -> USER       (an ordinary developer)
 *   mallory / hack123   -> USER       (the attacker, wants ADMIN)
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository repo, PasswordEncoder encoder) {
        return args -> {
            if (repo.count() > 0) return;
            repo.save(new AppUser("alice",   encoder.encode("admin123"), "ADMIN", "alice@devforge.io"));
            repo.save(new AppUser("bob",     encoder.encode("user123"),  "USER",  "bob@devforge.io"));
            repo.save(new AppUser("mallory", encoder.encode("hack123"),  "USER",  "mallory@evil.com"));
        };
    }
}
