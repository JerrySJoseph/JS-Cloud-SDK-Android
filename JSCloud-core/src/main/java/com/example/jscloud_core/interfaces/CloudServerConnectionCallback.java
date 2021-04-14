package com.example.jscloud_core.interfaces;

public interface CloudServerConnectionCallback {
    void onConnected();
    void onDisconnected();
    void onConnectionFailed(String reason);
}
