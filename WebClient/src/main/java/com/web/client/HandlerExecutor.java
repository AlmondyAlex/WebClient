package com.web.client;

import android.os.Handler;
import android.os.Looper;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HandlerExecutor
{
    protected Handler handler;
    protected ExecutorService executor;

    public HandlerExecutor()
    {
        handler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();
    }

    public HandlerExecutor(ExecutorService exec, Handler handler)
    {
        this.handler = handler;
        this.executor = exec;
    }

    public void execute(Runnable task)
    {
        executor.execute(task);
    }

    public void post(Runnable message)
    {
        handler.post(message);
    }

    public Handler getHandler() { return handler; }
    public ExecutorService getExecutor() { return executor; }
    public void setExecutor(ExecutorService executor) { this.executor = executor; }
    public void setHandler(Handler handler) { this.handler = handler; }
}
