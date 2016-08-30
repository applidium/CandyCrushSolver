package com.applidium.candycrushsolver.engine;

import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class FeaturesPainter {

    private final static int FEATURE_SIZE = 35;

    public static void drawRectanglesFromMessySweets(List<Sweet> messySweets, Mat img) {
        for (Sweet sweet : messySweets) {
            drawRectangleForSweet(img, sweet);
        }
    }

    public static void drawRectanglesFromSweetsGrid(List<List<Sweet>> grid, Mat img) {
        if (grid == null) {
            return;
        }
        for (List<Sweet> row : grid) {
            for (Sweet sweet : row) {
                drawRectangleForSweet(img, sweet);
            }
        }
    }

    private static void drawRectangleForSweet(Mat img, Sweet sweet) {
        if (sweet == null) {
            return;
        }
        double col = sweet.getX();
        double row = sweet.getY();
        Scalar colorant = sweet.getType().color;
        Imgproc.rectangle(img, new Point(col, row), new Point(col + FEATURE_SIZE, row + FEATURE_SIZE), colorant);
    }
}
