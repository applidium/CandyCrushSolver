package com.applidium.candycrushsolver.monitoring.di.crashes;

import android.app.Application;
import android.content.ComponentCallbacks2;

import com.applidium.candycrushsolver.monitoring.utils.aspect.RethrowUnexpectedAspect;

import net.hockeyapp.android.CrashManagerListener;

import javax.inject.Named;

import dagger.Subcomponent;

@Subcomponent(modules = {
    CrashesModule.class
})
public interface CrashesComponent {
    @Named("crash")
    CrashManagerListener crashesListener();
    Application.ActivityLifecycleCallbacks activityListener();
    ComponentCallbacks2 componentListener();

    void inject(RethrowUnexpectedAspect rethrowUnexpectedAspect);
}
