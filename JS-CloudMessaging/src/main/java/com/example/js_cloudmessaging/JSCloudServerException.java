package com.example.js_cloudmessaging;

public class JSCloudServerException extends IllegalArgumentException {

    public JSCloudServerException() {
        super();
    }

    public JSCloudServerException(String s) {
        super(s);
    }

    public JSCloudServerException(String message, Throwable cause) {
        super(message, cause);
    }

    public JSCloudServerException(Throwable cause) {
        super(cause);
    }
}
