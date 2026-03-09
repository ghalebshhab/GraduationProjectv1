package com.start.demo.DTOs.Auth.Login;

public class LoginResponse {

    private String token;
    private String type = "Bearer";

    private Long id;
    private String email;
    private String username;
    private String role;

    public LoginResponse() {
    }

    public LoginResponse(String token, Long id, String email, String username, String role) {
        this.token = token;
        this.id = id;
        this.email = email;
        this.username = username;
        this.role = role;
    }

    public String getToken() {
        return token;
    }

    public String getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getUsername() {
        return username;
    }

    public String getRole() {
        return role;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setRole(String role) {
        this.role = role;
    }
}