package com.wss.csrf.config;

import com.wss.csrf.model.AppUser;
import com.wss.csrf.model.AuditEntry;
import com.wss.csrf.model.Message;
import com.wss.csrf.repository.AuditRepository;
import com.wss.csrf.repository.MessageRepository;
import com.wss.csrf.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * Seeds the DevForge team and content into Postgres on startup (only if empty).
 *
 *   alice   / admin123  -> ADMIN     (the victim: a logged-in admin)
 *   bob     / user123   -> USER       (an ordinary developer)
 *   mallory / hack123   -> USER       (the attacker, wants ADMIN)
 */
@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner seed(UserRepository users, MessageRepository messages,
                           AuditRepository audit, PasswordEncoder encoder) {
        return args -> {
            if (users.count() == 0) {
                users.save(new AppUser("alice",   encoder.encode("admin123"), "ADMIN", "alice@devforge.io"));
                users.save(new AppUser("bob",     encoder.encode("user123"),  "USER",  "bob@devforge.io"));
                users.save(new AppUser("mallory", encoder.encode("hack123"),  "USER",  "mallory@devforge.io"));
            }
            if (messages.count() == 0) {
                messages.save(new Message("bob",
                        "Pushed the new OAuth flow to staging — reviews welcome before Friday's freeze."));
                messages.save(new Message("alice",
                        "Reminder: production deploy freeze starts Friday 5pm. Ping me for exceptions."));
                // The attacker plants the bait link in the team feed. An admin
                // browsing the feed will click it — no out-of-band URL needed.
                messages.save(new Message("mallory",
                        "Perk alert! Free JetBrains All-Products licences for the whole team — "
                        + "claim yours before EOD today: http://localhost:5500/evil.html"));
            }
            if (audit.count() == 0) {
                audit.save(new AuditEntry("system", "DevForge workspace initialised"));
            }
        };
    }
}
