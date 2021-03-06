package com.example.js_dynamicdatabase.models;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.example.js_dynamicdatabase.interfaces.OnCompleteListener;

import java.util.concurrent.ThreadFactory;


public abstract class Task<T> implements Runnable {

    private String TAG="Dynamic DB Task";
    private OnCompleteListener onCompleteListener;

    private ThreadFactory mThreadFactory=new ThreadFactory() {
        @Override
        public Thread newThread(Runnable r) {
            Log.e(TAG,"New Thread Created for Task");
            return new Thread(r);
        }
    };
    public void execute()
    {
        mThreadFactory.newThread(this).start();
    }

    public Task<T> addOnCompleteListener(OnCompleteListener onCompleteListener){
        this.onCompleteListener=onCompleteListener;
        return this;
    }
    protected void fireOnComplete(TaskResult result)
    {
        post(()->{
            if(onCompleteListener!=null)
                onCompleteListener.onTaskComplete(result);
        });

    }
    private void post(Runnable runnable)
    {
        new Handler(Looper.getMainLooper()).post(runnable);
    }
}
