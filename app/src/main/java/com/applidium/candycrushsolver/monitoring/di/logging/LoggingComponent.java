package com.applidium.candycrushsolver.monitoring.di.logging;

import com.applidium.candycrushsolver.android.BusinessService;
import com.applidium.candycrushsolver.android.HeadService;
import com.applidium.candycrushsolver.android.MainActivity;
import com.applidium.candycrushsolver.android.SettingsFragment;
import com.applidium.candycrushsolver.android.TutoActivity;
import com.applidium.candycrushsolver.monitoring.utils.aspect.TracerAspect;

import javax.inject.Singleton;

import dagger.Subcomponent;

@Singleton
@Subcomponent
public interface LoggingComponent {
    void inject(MainActivity injected);
    void inject(SettingsFragment injected);
    void inject(TutoActivity injected);
    void inject(BusinessService injected);
    void inject(HeadService injected);

    void inject(TracerAspect tracerAspect);

    @Subcomponent.Builder
    interface Builder {
        LoggingComponent build();
    }
}
