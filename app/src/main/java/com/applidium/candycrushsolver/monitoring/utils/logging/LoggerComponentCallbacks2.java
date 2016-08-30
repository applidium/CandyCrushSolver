package com.applidium.candycrushsolver.monitoring.utils.logging;

import android.content.ComponentCallbacks2;
import android.content.res.Configuration;

import javax.inject.Singleton;

@Singleton
public class LoggerComponentCallbacks2 implements ComponentCallbacks2 {

    private final Logger logger;

    public LoggerComponentCallbacks2(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void onTrimMemory(int level) {
        logger.w("onTrimMemory(level: %s)", level);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        logger.i("onConfigurationChanged(newConfig: %s)", newConfig);
    }

    @Override
    public void onLowMemory() {
        logger.w("onLowMemory()");
    }
}
