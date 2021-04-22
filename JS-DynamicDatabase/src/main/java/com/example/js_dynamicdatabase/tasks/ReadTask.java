package com.example.js_dynamicdatabase.tasks;

import com.example.js_dynamicdatabase.models.Task;
import com.example.js_dynamicdatabase.models.TaskResult;
import com.example.js_dynamicdatabase.helpers.DBRequest;
import com.example.js_dynamicdatabase.helpers.FilterQuery;
import com.example.jscloud_core.JSCloudApp;
import com.google.gson.Gson;

import io.socket.client.Ack;
import io.socket.client.Socket;

public class ReadTask<T> extends Task<T> {

    Socket mSocket;
    String collectioName;
    FilterQuery query;

    public ReadTask(String collectionName, FilterQuery query) {
        this.collectioName=collectionName;
        this.query=query;
    }

    @Override
    public void run()
    {
        if(mSocket==null)
            mSocket= JSCloudApp.getInstance().getSocket();
        DBRequest<T> request=new DBRequest<>(DBRequest.RequestType.READ_ALL,null,this.collectioName,query);
        String jsonString=new Gson().toJson(request);
        mSocket.emit("js-cloud-dynamic-db-read", jsonString, new Ack() {
            @Override
            public void call(Object... args) {
                fireOnComplete(TaskResult.parseFrom(args));
            }
        });
    }
}