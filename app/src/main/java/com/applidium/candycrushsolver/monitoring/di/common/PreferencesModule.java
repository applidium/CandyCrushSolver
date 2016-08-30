package com.applidium.candycrushsolver.monitoring.di.common;

import android.content.SharedPreferences;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class PreferencesModule {

    private final SharedPreferences preferences;

    public PreferencesModule(SharedPreferences preferences) {
        this.preferences = preferences;
    }

    @Provides @Singleton
    SharedPreferences preferences() {
        return preferences;
    }
}
