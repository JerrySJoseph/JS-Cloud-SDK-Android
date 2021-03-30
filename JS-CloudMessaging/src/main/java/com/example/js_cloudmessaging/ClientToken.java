package com.example.js_cloudmessaging;

public class ClientToken {
    String token;
    long expiresAt;
    String refreshToken;

    public ClientToken() {
    }

    public ClientToken(String token, long expiresAt, String refreshToken) {
        this.token = token;
        this.expiresAt = expiresAt;
        this.refreshToken = refreshToken;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpiresAt() {
        return expiresAt;
    }

    public void setExpiresAt(long expiresAt) {
        this.expiresAt = expiresAt;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public void setRefreshToken(String refreshToken) {
        this.refreshToken = refreshToken;
    }
}
