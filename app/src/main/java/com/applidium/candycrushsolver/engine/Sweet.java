package com.applidium.candycrushsolver.engine;


import android.support.annotation.ColorInt;

import org.opencv.core.Point;
import org.opencv.core.Scalar;

public class Sweet {

    public enum Type {
        GREEN("#00FF00FF"),
        RED("#0000FFFF"),
        ORANGE("#0066CCFF"),
        YELLOW("00CCCCFF"),
        PURPLE("CC0080FF"),
        BLUE("FF0000FF");

        final Scalar color;

        Type(String color) {
            int c = parseRGBA(color);
            this.color = new Scalar(red(c), green(c), blue(c), alpha(c));
        }
    }

    private final Type color;

    Point position;

    public Sweet(Type type, Point position) {
        this.color = type;
        this.position = position;
    }

    public Sweet(int colorIndex, Point position) {
        this(Type.values()[colorIndex], position);
    }

    public Type getType() {
        return color;
    }

    public Point getPosition() { return position; }

    public double getX() {
        return position.x;
    }

    public double getY() {
        return position.y;
    }

    public void setPosition(Point position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return color.toString();
    }

    @ColorInt
    private static int parseRGBA(String color) {
        return Integer.parseInt(color.substring(1), 16);
    }

    private static int red(@ColorInt int color) {
        return (color & 0xFF000000) >> 24;
    }

    private static int green(@ColorInt int color) {
        return (color & 0x00FF0000) >> 16;
    }

    private static int blue(@ColorInt int color) {
        return (color & 0x0000FF00) >> 8;
    }

    private static int alpha(@ColorInt int color) {
        return (color & 0x000000FF) >> 0;
    }
}
