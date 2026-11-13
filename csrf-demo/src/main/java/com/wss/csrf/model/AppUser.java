package com.wss.csrf.model;

public class AppUser {
    private String username;
    private String password;
    private String role;
    private String email;

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
