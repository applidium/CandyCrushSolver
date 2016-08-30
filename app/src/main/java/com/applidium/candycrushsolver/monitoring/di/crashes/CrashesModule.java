package com.applidium.candycrushsolver.monitoring.di.crashes;

import android.app.Application;
import android.content.ComponentCallbacks2;
import android.support.annotation.NonNull;

import com.applidium.candycrushsolver.monitoring.utils.crashes.HockeyAppCrashManagerListener;

import net.hockeyapp.android.CrashManagerListener;

import javax.inject.Named;

import dagger.Module;
import dagger.Provides;

@Module
public class CrashesModule {

    private static final String RETHROWN_MESSAGE = "Error has been rethrown. App did not crash.\n\n";

    @Provides @Named("rethrown")
    CrashManagerListener provideRethrownListener(
        final HockeyAppCrashManagerListener hockeyAppListener
    ) {
        return getRethrownCrashManagerListener(hockeyAppListener);
    }

    @NonNull
    private CrashManagerListener getRethrownCrashManagerListener(
        final HockeyAppCrashManagerListener hockeyAppListener
    ) {
        return new CrashManagerListener() {
            @Override
            public String getDescription() {
                return RETHROWN_MESSAGE + hockeyAppListener.getDescription();
            }
        };
    }

    @Provides @Named("crash")
    CrashManagerListener provideCrashListener(HockeyAppCrashManagerListener hockeyAppListener) {
        return hockeyAppListener;
    }

    @Provides
    Application.ActivityLifecycleCallbacks provideActivityListener(
        HockeyAppCrashManagerListener hockeyAppListener
    ) {
        return hockeyAppListener;
    }

    @Provides
    ComponentCallbacks2 provideComponentListener(HockeyAppCrashManagerListener hockeyAppListener) {
        return hockeyAppListener;
    }
}
