package com.applidium.candycrushsolver.monitoring.utils.logging;

public interface Logger {

    void v(String message, Object... args);
    void v(Object instance, String message, Object... args);
    void v(Throwable t, String message, Object... args);
    void v(Object instance, Throwable t, String message, Object... args);

    void d(String message, Object... args);
    void d(Object instance, String message, Object... args);
    void d(Throwable t, String message, Object... args);
    void d(Object instance, Throwable t, String message, Object... args);

    void i(String message, Object... args);
    void i(Object instance, String message, Object... args);
    void i(Throwable t, String message, Object... args);
    void i(Object instance, Throwable t, String message, Object... args);

    void w(String message, Object... args);
    void w(Object instance, String message, Object... args);
    void w(Throwable t, String message, Object... args);
    void w(Object instance, Throwable t, String message, Object... args);

    void e(String message, Object... args);
    void e(Object instance, String message, Object... args);
    void e(Throwable t, String message, Object... args);
    void e(Object instance, Throwable t, String message, Object... args);
}
