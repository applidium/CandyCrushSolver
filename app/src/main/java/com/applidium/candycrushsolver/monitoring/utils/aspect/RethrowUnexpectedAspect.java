package com.applidium.candycrushsolver.monitoring.utils.aspect;

import android.support.annotation.NonNull;
import com.applidium.candycrushsolver.Settings;
import com.applidium.candycrushsolver.monitoring.core.UnexpectedError;
import com.applidium.candycrushsolver.monitoring.di.ComponentManager;
import com.applidium.candycrushsolver.monitoring.utils.logging.Logger;

import net.hockeyapp.android.CrashManagerListener;
import net.hockeyapp.android.ExceptionHandler;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.CodeSignature;

import javax.inject.Inject;
import javax.inject.Named;

@Aspect
public class RethrowUnexpectedAspect {

    private static final boolean ENABLED = Settings.errors.rethrow;

    private static final String ANNOTATION_PACKAGE = "com.applidium.candycrushsolver.utils.rethrowunexpected.";
    private static final String RETHROW_UNEXPECTED_POINTCUT = "execution(@" + ANNOTATION_PACKAGE + "RethrowUnexpected * *(..))";

    private static final boolean CRASHES_ENABLED = Settings.crashes.enabled;

    @Inject
    Logger logger;
    @Inject @Named("rethrown")
    CrashManagerListener crashListener;

    public void init() {
        ComponentManager.getCrashesComponent().inject(this);
    }

    @Pointcut(RETHROW_UNEXPECTED_POINTCUT)
    public void rethrowUnexpected() {}

    @Around("rethrowUnexpected()")
    public Object rethrow(ProceedingJoinPoint joinPoint) throws Throwable {
        if (!ENABLED) {
            return joinPoint.proceed();
        }
        checkInit();
        log(joinPoint);
        return doFailSafe(joinPoint);
    }

    private void log(ProceedingJoinPoint joinPoint) {
        Object target = joinPoint.getTarget();
        CodeSignature codeSignature = (CodeSignature) joinPoint.getSignature();
        Object[] parameterValues = joinPoint.getArgs();
        String message = makeMessage(codeSignature);
        logger.w(target, message, parameterValues);
    }

    private Object doFailSafe(ProceedingJoinPoint joinPoint) {
        try {
            return joinPoint.proceed();
        } catch (Throwable throwable) {
            if (CRASHES_ENABLED) {
                ExceptionHandler.saveException(throwable, null, crashListener);
            }
            logger.e(throwable, null);
            throw new UnexpectedError();
        }
    }

    @NonNull
    private String makeMessage(CodeSignature codeSignature) {
        String methodName = codeSignature.getName();
        String[] parameterNames = codeSignature.getParameterNames();

        String params = makeParamsFormat(parameterNames);
        return "rethrow unexpected " + methodName + "(" + params + ")";
    }

    @NonNull
    private String makeParamsFormat(String[] parameterNames) {
        StringBuilder builder = new StringBuilder();
        for (String parameterName : parameterNames) {
            builder.append(parameterName);
            builder.append(": %s,");
        }
        int index = builder.lastIndexOf(",");
        if (index >= 0) {
            builder.deleteCharAt(index);
        }
        return builder.toString();
    }

    private void checkInit() {
        if (logger == null) {
            fail();
        }
    }

    private static void fail() {
        String message = "RethrowUnexpectedAspect#init() was not called on Application#onCreate()";
        throw new RuntimeException(message);
    }
}
