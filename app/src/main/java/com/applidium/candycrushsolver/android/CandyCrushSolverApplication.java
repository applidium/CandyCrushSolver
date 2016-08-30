package com.applidium.candycrushsolver.android;

import android.app.Application;
import android.content.SharedPreferences;

import com.applidium.candycrushsolver.BuildConfig;
import com.applidium.candycrushsolver.Settings;
import com.applidium.candycrushsolver.monitoring.di.ComponentManager;
import com.applidium.candycrushsolver.monitoring.di.crashes.CrashesComponent;
import com.applidium.candycrushsolver.monitoring.utils.aspect.RethrowUnexpectedAspect;
import com.applidium.candycrushsolver.monitoring.utils.aspect.ThreadingAspect;
import com.applidium.candycrushsolver.monitoring.utils.aspect.TracerAspect;

import net.hockeyapp.android.CrashManager;

import org.aspectj.lang.Aspects;

import java.io.File;

import timber.log.Timber;

public class CandyCrushSolverApplication extends Application {

    protected static final String PREFS_NAME = "CANDYCRUSHSOLVER_PROJECT";
    protected static final boolean HOCKEY_APP_ENABLED = Settings.crashes.enabled;
    protected static final String HOCKEY_APP_KEY = BuildConfig.APP_KEY;

    @Override
    public void onCreate() {
        super.onCreate();
        setupLogging();
        if (HOCKEY_APP_KEY.equals("")) {
            return;
        }
        setupGraph();
        setupHockeyApp();
        setupAspects();
    }

    private void setupHockeyApp() {
        if (!HOCKEY_APP_ENABLED) {
            return;
        }
        CrashesComponent component = ComponentManager.getCrashesComponent();
        CrashManager.register(this, HOCKEY_APP_KEY, component.crashesListener());
        registerActivityLifecycleCallbacks(component.activityListener());
        registerComponentCallbacks(component.componentListener());
    }

    private void setupAspects() {
        ThreadingAspect threadingAspect = Aspects.aspectOf(ThreadingAspect.class);
        threadingAspect.init();
        TracerAspect loggerAspect = Aspects.aspectOf(TracerAspect.class);
        loggerAspect.init();
        RethrowUnexpectedAspect rethrowAspect = Aspects.aspectOf(RethrowUnexpectedAspect.class);
        rethrowAspect.init();
    }


    protected void setupGraph() {
        SharedPreferences preferences = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        File cacheDirectory = getCacheDir();
        ComponentManager.init(preferences, cacheDirectory);
    }

    private void setupLogging() {
        if (BuildConfig.DEBUG) {
            Timber.plant(new Timber.DebugTree());
        }
    }
}
