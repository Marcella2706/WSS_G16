package com.wss.csrf.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/**
 * A DevForge team member, persisted in Postgres.
 * The {@code role} column is what a CSRF attack targets: flipping a USER to ADMIN
 * is a durable privilege escalation that survives a server restart.
 */
@Entity
@Table(name = "app_users")
public class AppUser {

    @Id
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String role;

    @Column(nullable = false)
    private String email;

    protected AppUser() { } // for JPA

    public AppUser(String username, String password, String role, String email) {
        this.username = username;
        this.password = password;
        this.role = role;
        this.email = email;
    }

    public String getUsername() { return username; }
    public String getPassword() { return password; }
    public String getRole() { return role; }
    public String getEmail() { return email; }
    public void setRole(String role) { this.role = role; }
    public void setEmail(String email) { this.email = email; }
}
