package com.example.js_dynamicdatabase.tasks;

import com.example.js_dynamicdatabase.models.Task;
import com.example.js_dynamicdatabase.models.TaskResult;
import com.example.js_dynamicdatabase.exceptions.DocumentsOutOfLimitException;
import com.example.js_dynamicdatabase.helpers.DBRequest;
import com.example.jscloud_core.JSCloudApp;
import com.google.gson.Gson;

import io.socket.client.Ack;
import io.socket.client.Socket;

public class InserAllTask<T> extends Task<T> {

    Socket mSocket;
    T[] object;
    String collectioName;

    public InserAllTask(String collectionName,T[] object) {
        this.object = object;
        this.collectioName=collectionName;
        if(object.length>999)
            throw new DocumentsOutOfLimitException("No. of documents exceeded the limit of 999. Please break your document list into multiple batches of size less than 999");
    }


    @Override
    public void run()
    {
        if(mSocket==null)
            mSocket= JSCloudApp.getInstance().getSocket();
        DBRequest<T[]> request=new DBRequest<>(DBRequest.RequestType.INSERT_ALL,object,this.collectioName);
        String jsonString=new Gson().toJson(request);
        mSocket.emit("js-cloud-dynamic-db-insert-all", jsonString, new Ack() {
            @Override
            public void call(Object... args) {
                fireOnComplete(TaskResult.parseFrom(args));
            }
        });
    }
}