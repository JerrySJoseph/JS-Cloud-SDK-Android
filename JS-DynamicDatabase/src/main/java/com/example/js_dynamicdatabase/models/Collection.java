package com.example.js_dynamicdatabase.models;


import android.os.Handler;
import android.os.Looper;
import android.util.Log;


import com.example.js_dynamicdatabase.helpers.ObserveEvent;
import com.example.js_dynamicdatabase.interfaces.OnCompleteListener;
import com.example.js_dynamicdatabase.tasks.InserAllTask;
import com.example.js_dynamicdatabase.tasks.InsertOneTask;
import com.example.js_dynamicdatabase.tasks.ReadTask;
import com.example.js_dynamicdatabase.helpers.FilterQuery;
import com.example.js_dynamicdatabase.interfaces.OnDocChangedListener;
import com.example.js_dynamicdatabase.tasks.RegisterEventTask;
import com.example.jscloud_core.JSCloudApp;

import java.util.List;

import io.socket.client.Socket;
import io.socket.emitter.Emitter;

public class Collection<T> {
    String name;
    OnDocChangedListener onDocChangedListener;

    public Collection(String name) {
        this.name = name;
    }

    public Task<T> insert(T dataObject)
    {
        return new InsertOneTask<T>(this.name,dataObject);
    }

    public void insert(T dataObject,InsertOptions options)
    {

    }

    public Task<T> insert(T[] dataObject)
    {
        return new InserAllTask<T>(this.name,dataObject);
    }
    public Task<T> insert(List<T> dataObject)
    {
        return new InserAllTask<T>(this.name, (T[]) dataObject.toArray());
    }
    public void insert(List<T> dataObject,InsertOptions options)
    {

    }
    public Task<T> read(FilterQuery filterQuery)
    {
        String query=filterQuery.getFilterQuery().toString();
        return new ReadTask<T>(this.name,filterQuery);
    }
    public Collection observe(OnDocChangedListener onDocChangedListener) {
        this.onDocChangedListener=onDocChangedListener;
        new RegisterEventTask<>(this.name).addOnCompleteListener(onregisteredEvent).execute();
        return this;
    }

    OnCompleteListener onregisteredEvent= new OnCompleteListener() {
        @Override
        public void onTaskComplete(TaskResult result) {
            Socket mSocket=JSCloudApp.getInstance().getSocket();
            if(result.isSuccess())
            {
                List<Emitter.Listener> listeners=mSocket.listeners(result.getResult(String.class));
                if(mSocket.listeners(result.getResult(String.class)).size()<1)
                mSocket.on(result.getResult(String.class),(args)->{
                    ObserveEvent event=ObserveEvent.parseFromArgs(args);
                    if(event==null)
                        return;
                    post(()->{
                        if(onDocChangedListener!=null)
                        {
                            if(event.getObserveType()== ObserveEvent.EventType.delete)
                                onDocChangedListener.onDocRemoved(event);
                            if(event.getObserveType()== ObserveEvent.EventType.update)
                                onDocChangedListener.onDocChanged(event);
                            if(event.getObserveType()== ObserveEvent.EventType.insert)
                                onDocChangedListener.onDocAdded(event);

                        }
                    });

                });
            }
            else
                Log.e("Collection",result.message);
        }
    };
    void post(Runnable r)
    {
        new Handler(Looper.getMainLooper()).post(r);
    }

}
