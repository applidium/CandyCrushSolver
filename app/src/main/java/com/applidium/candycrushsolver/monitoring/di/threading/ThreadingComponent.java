package com.applidium.candycrushsolver.monitoring.di.threading;

import com.applidium.candycrushsolver.monitoring.utils.aspect.ThreadingAspect;

import javax.inject.Singleton;

import dagger.Subcomponent;

@Singleton
@Subcomponent(modules = ThreadingModule.class)
public interface ThreadingComponent {
    void inject(ThreadingAspect aspect);
}
