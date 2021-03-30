package com.example.js_cloudmessaging;

public interface MessageHandler {
    void onNewMessage(Object... arg);
    void onPush(Object... args);
}
