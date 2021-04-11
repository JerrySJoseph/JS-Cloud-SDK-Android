package com.example.js_auth.interfaces;

public interface SignOutResponse {
    void onSignOutSuccess();
    void onSignOutFailed(String reason);
}
