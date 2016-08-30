package com.applidium.candycrushsolver.monitoring.utils.logging;

import timber.log.Timber;

public final class TimberLogger implements Logger {

    private boolean showHash = true;

    public TimberLogger() {
        init();
    }

    public TimberLogger(boolean showHash) {
        this.showHash = showHash;
        init();
    }

    private void init() {
        Timber.v("init()");
    }

    @Override
    protected void finalize() throws Throwable {
        Timber.v("finalize()");
        super.finalize();
    }

    public void setShowHash(boolean showHash) {
        this.showHash = showHash;
    }

    private String getTag(Object instance) {
        if (instance == null) {
            return "";
        }
        String name = instance.getClass().getSimpleName();
        String hash = "@" + System.identityHashCode(instance);
        if (showHash) {
            return name + hash;
        }
        return name;
    }

    /** VERBOSE */
    @Override
    public void v(String message, Object... args) {
        Timber.v(message, args);
    }

    @Override
    public void v(Object instance, String message, Object... args) {
        Timber.tag(getTag(instance)).v(message, args);
    }

    @Override
    public void v(Throwable t, String message, Object... args) {
        Timber.v(t, message, args);
    }

    @Override
    public void v(Object instance, Throwable t, String message, Object... args) {
        Timber.tag(getTag(instance)).v(t, message, args);
    }

    /** DEBUG */
    @Override
    public void d(String message, Object... args) {
        Timber.d(message, args);
    }

    @Override
    public void d(Object instance, String message, Object... args) {
        Timber.tag(getTag(instance)).d(message, args);
    }

    @Override
    public void d(Throwable t, String message, Object... args) {
        Timber.d(t, message, args);
    }

    @Override
    public void d(Object instance, Throwable t, String message, Object... args) {
        Timber.tag(getTag(instance)).d(t, message, args);
    }

    /** INFO */
    @Override
    public void i(String message, Object... args) {
        Timber.i(message, args);
    }

    @Override
    public void i(Object instance, String message, Object... args) {
        Timber.tag(getTag(instance)).i(message, args);
    }

    @Override
    public void i(Throwable t, String message, Object... args) {
        Timber.i(t, message, args);
    }

    @Override
    public void i(Object instance, Throwable t, String message, Object... args) {
        Timber.tag(getTag(instance)).i(t, message, args);
    }

    /** WARNING */
    @Override
    public void w(String message, Object... args) {
        Timber.w(message, args);
    }

    @Override
    public void w(Object instance, String message, Object... args) {
        Timber.tag(getTag(instance)).w(message, args);
    }

    @Override
    public void w(Throwable t, String message, Object... args) {
        Timber.w(t, message, args);
    }

    @Override
    public void w(Object instance, Throwable t, String message, Object... args) {
        Timber.tag(getTag(instance)).w(t, message, args);
    }

    /** ERROR */
    @Override
    public void e(String message, Object... args) {
        Timber.e(message, args);
    }

    @Override
    public void e(Object instance, String message, Object... args) {
        Timber.tag(getTag(instance)).e(message, args);
    }

    @Override
    public void e(Throwable t, String message, Object... args) {
        Timber.e(t, message, args);
    }

    @Override
    public void e(Object instance, Throwable t, String message, Object... args) {
        Timber.tag(getTag(instance)).e(t, message, args);
    }
}
