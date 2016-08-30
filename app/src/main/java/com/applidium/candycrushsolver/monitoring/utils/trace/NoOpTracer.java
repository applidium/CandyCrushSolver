package com.applidium.candycrushsolver.monitoring.utils.trace;

import javax.inject.Inject;

public class NoOpTracer implements Tracer {

    @Inject
    NoOpTracer() {}

    @Override
    public void trace(Object target, String message, Object[] parameterValues) {
    }
}
