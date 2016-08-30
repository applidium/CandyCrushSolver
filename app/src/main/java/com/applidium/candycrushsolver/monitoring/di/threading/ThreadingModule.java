package com.applidium.candycrushsolver.monitoring.di.threading;

import com.applidium.candycrushsolver.monitoring.utils.threading.UiThread;
import com.applidium.candycrushsolver.monitoring.utils.threading.JobExecutor;
import com.applidium.candycrushsolver.monitoring.utils.threading.PostExecutionThread;
import com.applidium.candycrushsolver.monitoring.utils.threading.ThreadExecutor;

import javax.inject.Singleton;

import dagger.Module;
import dagger.Provides;

@Module
public class ThreadingModule {

    @Provides
    @Singleton
    ThreadExecutor provideExecutor(JobExecutor instance) {
        return instance;
    }

    @Provides
    @Singleton
    PostExecutionThread providePostThread(UiThread instance) {
        return instance;
    }
}
