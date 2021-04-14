package com.example.js_auth.Helpers;

import android.util.Log;

import com.example.js_auth.JSCloudAuth;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.socket.engineio.client.Socket;

public class TokenUpdater {

    long defaultInterval;

    ScheduledThreadPoolExecutor threadPoolExecutor;
    private  final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger mCount = new AtomicInteger(1);
        public Thread newThread(Runnable r) {
            Log.e("THREAD","created new thread");
            return new Thread(r, "token-update-task #" + mCount.getAndIncrement());
        }
    };
    public TokenUpdater(long interval)
    {
        this.defaultInterval=interval;
        threadPoolExecutor=(ScheduledThreadPoolExecutor)Executors.newScheduledThreadPool(1,sThreadFactory);
    }
    public  void setDefaultInterval(long interval)
    {
        defaultInterval=interval;
    }
    public  void startUpdate()
    {
        threadPoolExecutor.scheduleWithFixedDelay(updateTask,10,20, TimeUnit.SECONDS);
    }
    public  void stopUpdate()
    {
        threadPoolExecutor.shutdown();
    }

    Runnable updateTask= new Runnable() {
        @Override
        public void run() {
            JSCloudAuth.getInstance().refreshMyToken();
        }
    };


}
