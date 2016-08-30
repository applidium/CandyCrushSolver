package com.applidium.candycrushsolver.monitoring.utils.trace;

import com.applidium.candycrushsolver.monitoring.utils.logging.Logger;

import javax.inject.Inject;

public class LoggerTracer implements Tracer {

    private final Logger logger;

    @Inject
    LoggerTracer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void trace(Object target, String message, Object[] parameterValues) {
        logger.i(target, message, parameterValues);
    }
}
