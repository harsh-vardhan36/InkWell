package com.inkWell.auth.dto;

public class AuthResponse {
    private String token;
    private String message;

    public AuthResponse() {}
    public AuthResponse(String token, String message) {
        this.token = token;
        this.message = message;
    }

    public static AuthResponseBuilder builder() {
        return new AuthResponseBuilder();
    }

    public static class AuthResponseBuilder {
        private String token;
        private String message;
        public AuthResponseBuilder token(String token) { this.token = token; return this; }
        public AuthResponseBuilder message(String message) { this.message = message; return this; }
        public AuthResponse build() { return new AuthResponse(token, message); }
    }

    public String getToken() { return token; }
    public void setToken(String token) { this.token = token; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}
