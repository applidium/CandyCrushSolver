package com.applidium.candycrushsolver.monitoring.utils.threading;

import android.os.Handler;
import android.os.Looper;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class UiThread implements PostExecutionThread {

    private final Handler handler;

    @Inject
    UiThread() {
        this.handler = new Handler(Looper.getMainLooper());
    }

    @Override
    public void post(Runnable runnable) {
        handler.post(runnable);
    }
}
