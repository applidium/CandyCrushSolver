package com.applidium.candycrushsolver.monitoring.di.common;

import com.applidium.candycrushsolver.monitoring.di.crashes.CrashesModule;
import com.applidium.candycrushsolver.monitoring.utils.logging.Logger;
import com.applidium.candycrushsolver.monitoring.di.logging.LoggingComponent;
import com.applidium.candycrushsolver.monitoring.di.logging.LoggingModule;
import com.applidium.candycrushsolver.monitoring.di.threading.ThreadingComponent;
import com.applidium.candycrushsolver.monitoring.di.threading.ThreadingModule;
import com.applidium.candycrushsolver.monitoring.di.trace.TracerModule;
import com.applidium.candycrushsolver.monitoring.di.crashes.CrashesComponent;

import javax.inject.Singleton;

import dagger.Component;

@Singleton
@Component(modules = {
    LoggingModule.class,
    PreferencesModule.class,
    TracerModule.class
})
public interface ApplicationComponent {
    Logger logger();

    LoggingComponent.Builder loggingComponentBuilder();
    CrashesComponent plus(CrashesModule module);
    ThreadingComponent plus(ThreadingModule module);
}
