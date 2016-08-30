package com.applidium.candycrushsolver.monitoring.core;

public class UnexpectedError extends Error {
    @Override
    public int getId() {
        return Errors.GENERIC;
    }
}
