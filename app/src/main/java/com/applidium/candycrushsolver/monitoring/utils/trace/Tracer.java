package com.applidium.candycrushsolver.monitoring.utils.trace;

public interface Tracer {
    void trace(Object target, String message, Object[] parameterValues);
}
