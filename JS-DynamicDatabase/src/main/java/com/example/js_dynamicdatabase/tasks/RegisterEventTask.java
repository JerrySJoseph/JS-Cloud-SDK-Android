package com.example.js_dynamicdatabase.tasks;

import android.util.Log;


import com.example.js_dynamicdatabase.models.Task;
import com.example.js_dynamicdatabase.models.TaskResult;
import com.example.jscloud_core.JSCloudApp;


import io.socket.client.Ack;
import io.socket.client.Socket;


public class RegisterEventTask<T> extends Task<T> {
    Socket mSocket;
    String collectioName;


    public RegisterEventTask(String collectioName) {
        this.collectioName = collectioName;
    }

    @Override
    public void run() {
        if(mSocket==null)
            mSocket= JSCloudApp.getInstance().getSocket();

        mSocket.emit("js-cloud-dynamic-db-observe", collectioName, new Ack() {
            @Override
            public void call(Object... args) {
                Log.e("ack","ack received");
                fireOnComplete(TaskResult.parseFrom(args));
            }
        });
    }

}
