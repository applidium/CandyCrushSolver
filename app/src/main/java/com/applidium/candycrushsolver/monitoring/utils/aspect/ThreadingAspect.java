package com.applidium.candycrushsolver.monitoring.utils.aspect;

import com.applidium.candycrushsolver.monitoring.core.UnexpectedError;
import com.applidium.candycrushsolver.monitoring.di.ComponentManager;
import com.applidium.candycrushsolver.monitoring.utils.logging.Logger;
import com.applidium.candycrushsolver.monitoring.utils.threading.PostExecutionThread;
import com.applidium.candycrushsolver.monitoring.utils.threading.ThreadExecutor;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;

import javax.inject.Inject;

@Aspect
public class ThreadingAspect {

    private static final String ANNOTATION_PACKAGE = "com.applidium.candycrushsolver.utils.threading.";
    public static final String EXECUTION_PREFIX = "execution(@" + ANNOTATION_PACKAGE;
    public static final String EXECUTION_SUFFIX = " * *(..))";

    private static final String EXECUTION_THREAD_POINTCUT =
        EXECUTION_PREFIX + "RunOnExecutionThread" + EXECUTION_SUFFIX;

    private static final String POST_EXECUTION_THREAD_POINTCUT =
        EXECUTION_PREFIX + "RunOnPostExecutionThread" + EXECUTION_SUFFIX;

    @Inject
    ThreadExecutor threadExecutor;
    @Inject
    PostExecutionThread postExecutionThread;
    @Inject
    Logger logger;

    public void init() {
        ComponentManager.getThreadingComponent().inject(this);
    }

    @Pointcut(EXECUTION_THREAD_POINTCUT)
    public void executionThreadAnnotated() {}

    @Pointcut(POST_EXECUTION_THREAD_POINTCUT)
    public void postExecutionThreadAnnotated() {}

    @Around("executionThreadAnnotated()")
    public void runOnExecutionThread(ProceedingJoinPoint joinPoint) {
        checkInit();
        logger.v(this, "Running execution joint point " + joinPoint);
        threadExecutor.execute(makeExecutorRunnable(joinPoint));
    }

    private Runnable makeExecutorRunnable(final ProceedingJoinPoint joinPoint) {
        return new Runnable() {
            @Override
            public void run() {
                proceed(joinPoint);
            }
        };
    }

    @Around("postExecutionThreadAnnotated()")
    public void runOnPostExecutionThread(ProceedingJoinPoint joinPoint) {
        checkInit();
        logger.v(this, "Running post execution joint point " + joinPoint);
        postExecutionThread.post(makePostRunnable(joinPoint));
    }

    private Runnable makePostRunnable(final ProceedingJoinPoint joinPoint) {
        return new Runnable() {
            @Override
            public void run() {
                proceed(joinPoint);
            }
        };
    }

    private void proceed(ProceedingJoinPoint joinPoint) {
        try {
            joinPoint.proceed();
        } catch (Throwable throwable) {
            logger.e(this, throwable, "Error while changing threads.");
            throw new UnexpectedError();
        }
    }

    private void checkInit() {
        if (logger == null || threadExecutor == null || postExecutionThread == null) {
            fail();
        }
    }

    private static void fail() {
        String message = "ThreadingAspect#init() was not called on Application#onCreate()";
        throw new RuntimeException(message);
    }
}
