package com.example.js_dynamicdatabase.exceptions;

public class DocumentsOutOfLimitException extends OutOfMemoryError {
    public DocumentsOutOfLimitException() {
        super();
    }

    public DocumentsOutOfLimitException(String message) {
        super(message);
    }


}
