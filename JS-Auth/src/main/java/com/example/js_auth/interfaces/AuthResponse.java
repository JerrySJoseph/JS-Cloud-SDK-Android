package com.example.js_auth.interfaces;

import com.example.js_auth.Models.JSCloudUser;

public interface AuthResponse {
    void onAuthResponse(boolean isAuthorized, JSCloudUser user);
    void onAuthError(Exception exception);
}
