package com.applidium.candycrushsolver.monitoring.di;

import android.content.SharedPreferences;
import android.support.annotation.NonNull;

import com.applidium.candycrushsolver.monitoring.di.common.ApplicationComponent;
import com.applidium.candycrushsolver.monitoring.di.common.DaggerApplicationComponent;
import com.applidium.candycrushsolver.monitoring.di.common.PreferencesModule;
import com.applidium.candycrushsolver.monitoring.di.crashes.CrashesComponent;
import com.applidium.candycrushsolver.monitoring.di.crashes.CrashesModule;
import com.applidium.candycrushsolver.monitoring.di.logging.LoggingComponent;
import com.applidium.candycrushsolver.monitoring.di.logging.LoggingModule;
import com.applidium.candycrushsolver.monitoring.di.threading.ThreadingComponent;
import com.applidium.candycrushsolver.monitoring.di.threading.ThreadingModule;
import com.applidium.candycrushsolver.monitoring.di.trace.TracerModule;

import java.io.File;

public class ComponentManager {

    protected static ApplicationComponent applicationComponent;
    protected static LoggingComponent loggingComponent;
    protected static ThreadingComponent threadingComponent;
    protected static CrashesComponent crashesComponent;

    public static void init(SharedPreferences preferences, File cacheDirectory) {
        LoggingModule loggingModule = new LoggingModule();
        PreferencesModule preferencesModule = new PreferencesModule(preferences);
        TracerModule tracerModule = new TracerModule();
        initApplicationComponent(loggingModule, preferencesModule, tracerModule);
        initLoggingComponent();
        ThreadingModule threadingModule = new ThreadingModule();
        initThreadingComponent(threadingModule);
        CrashesModule crashesModule = new CrashesModule();
        initCrashesComponent(crashesModule);
    }

    protected static void initApplicationComponent(
        LoggingModule loggingModule,
        PreferencesModule preferencesModule,
        TracerModule tracerModule
    ) {
        applicationComponent = DaggerApplicationComponent
            .builder()
            .loggingModule(loggingModule)
            .tracerModule(tracerModule)
            .preferencesModule(preferencesModule)
            .build();
    }

    protected static void initLoggingComponent() {
        loggingComponent = applicationComponent.loggingComponentBuilder().build();
    }

    protected static void initThreadingComponent(ThreadingModule threadingModule) {
        threadingComponent = applicationComponent.plus(threadingModule);
    }

    protected static void initCrashesComponent(CrashesModule crashesModule) {
        crashesComponent = applicationComponent.plus(crashesModule);
    }

    public static ApplicationComponent getApplicationComponent() {
        return safeReturn(applicationComponent);
    }

    public static LoggingComponent getLoggingComponent() {
        return safeReturn(loggingComponent);
    }

    public static ThreadingComponent getThreadingComponent() {
        return safeReturn(threadingComponent);
    }

    public static CrashesComponent getCrashesComponent() {
        return safeReturn(crashesComponent);
    }

    @NonNull
    private static <C> C safeReturn(C component) {
        if (component == null) {
            fail();
        }
        return component;
    }

    private static void fail() {
        String message = "ComponentManager.init() was not called on Application#onCreate()";
        throw new RuntimeException(message);
    }
}
