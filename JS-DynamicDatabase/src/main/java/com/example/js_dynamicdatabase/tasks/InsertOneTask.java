package com.example.js_dynamicdatabase.tasks;

import com.example.js_dynamicdatabase.models.Task;
import com.example.js_dynamicdatabase.models.TaskResult;
import com.example.js_dynamicdatabase.helpers.DBRequest;
import com.example.jscloud_core.JSCloudApp;
import com.google.gson.Gson;

import io.socket.client.Ack;
import io.socket.client.Socket;

public class InsertOneTask<T> extends Task<T> {

    Socket mSocket;
    T object;
    String collectioName;

    public InsertOneTask(String collectionName,T object) {
        this.object = object;
        this.collectioName=collectionName;
    }


    @Override
    public void run()
    {
        if(mSocket==null)
            mSocket= JSCloudApp.getInstance().getSocket();
        DBRequest<T> request=new DBRequest<>(DBRequest.RequestType.INSERT_ONE,object,this.collectioName);
        String jsonString=new Gson().toJson(request);
        mSocket.emit("js-cloud-dynamic-db-insert-one", jsonString, new Ack() {
            @Override
            public void call(Object... args) {
                fireOnComplete(TaskResult.parseFrom(args));

            }
        });
    }
}
