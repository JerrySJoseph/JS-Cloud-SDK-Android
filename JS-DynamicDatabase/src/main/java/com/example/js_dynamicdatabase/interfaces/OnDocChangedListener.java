package com.example.js_dynamicdatabase.interfaces;

import com.example.js_dynamicdatabase.helpers.ObserveEvent;
import com.example.js_dynamicdatabase.models.TaskResult;

public interface OnDocChangedListener {
    void onDocChanged(ObserveEvent event);
    void onDocAdded(ObserveEvent event);
    void onDocRemoved(ObserveEvent event);
}
