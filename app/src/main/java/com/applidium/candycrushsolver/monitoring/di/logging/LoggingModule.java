package com.applidium.candycrushsolver.monitoring.di.logging;

import com.applidium.candycrushsolver.BuildConfig;
import com.applidium.candycrushsolver.Settings;
import com.applidium.candycrushsolver.monitoring.utils.logging.NoOpLogger;
import com.applidium.candycrushsolver.monitoring.utils.logging.TimberLogger;
import com.applidium.candycrushsolver.monitoring.utils.logging.Logger;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class LoggingModule {

    protected static final boolean SHOW_HASH = Settings.logging.show_hashs;
    protected static final boolean ENABLED = Settings.logging.enabled;

    @Provides @Singleton
    Logger provideLogger() {
        return getLogger();
    }

    protected Logger getLogger() {
        if (BuildConfig.DEBUG && ENABLED) {
            return new TimberLogger(SHOW_HASH);
        }
        return new NoOpLogger();
    }
}
