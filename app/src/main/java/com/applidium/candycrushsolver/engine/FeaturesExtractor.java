package com.applidium.candycrushsolver.engine;

import android.content.res.Configuration;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import timber.log.Timber;

public class FeaturesExtractor {
    private static final int NB_SWEETS_IN_COL = 16;
    private static final int WALK = 6;
    private static double THRESHOLD;
    private static int STEP = 30;
    private static final int OTHER_PIXEL = 10;
    private static int DIFFERENCE_LIMIT = 20;
    private static final int DIFFERENCE_LIMIT_OTHER = 45;

    public List<List<Sweet>> extractFeaturesFromImage(Mat img, int [] features, int orientation) {
        Timber.v("Running Template Matching");
        List<Sweet> messySweets = new ArrayList<>();
        for (int i = 0; i < features.length; i++) {
            Timber.v("Template matching");
            messySweets.addAll(extractSweetsForFeature(img, features[i], i, orientation));
            Timber.v("Template matching finished");
        }

        deleteCloseOnes(messySweets);

        double colStep = img.height() / NB_SWEETS_IN_COL;
        List<List<Sweet>> grid = sortSweets(messySweets, colStep);
        alignGrid(grid, colStep);
        readjustGridWithSymmetry(grid);

        printFinalGrid(grid);

        return grid;
    }

    /******************************************* OpenCV Version *******************************/

    public List<Sweet> extractSweetsForFeatureWithOpenCV(Mat img, Mat feature, int i) {
        STEP = 10;
        DIFFERENCE_LIMIT = 60;
        Imgproc.pyrDown(feature, feature, new Size(feature.cols() / 2, feature.rows() / 2));
        Mat result = createResultMat(img, feature);
        result = matchFeature(img, feature, result);
        if (i != 5) {
            THRESHOLD = 0.96;
        } else {
            THRESHOLD = 0.85;
        }

        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;
        int refX = (int) matchLoc.x + feature.cols() / 2;
        int refY = (int) matchLoc.y + feature.rows() / 2;
        if (!isFeatureAbsent(img, feature, refX, refY)) {
            Imgproc.threshold(result, result, THRESHOLD, 255, Imgproc.THRESH_BINARY);
            return extractSweetsForFeatureWithOpenCV(result, i);
        }
        return Collections.emptyList();
    }

    private List<Sweet> extractSweetsForFeatureWithOpenCV(Mat result, int colorIndex) {
        List<Sweet> featureSweets = new ArrayList<>();
        result.convertTo(result, CvType.CV_64FC1);
        for (int row = 0; row < result.rows(); row++) {
            for (int col = 0; col < result.cols(); col++) {
                double[] bgr = new double[3];
                result.get(row, col, bgr);
                if (bgr[0] > THRESHOLD) {
                    Sweet sweet = new Sweet(colorIndex, new Point(col, row));
                    featureSweets.add(sweet);
                }
            }
        }
        return featureSweets;
    }

    private static boolean isFeatureAbsent(Mat img, Mat feature, int refX, int refY) {
        return Math.abs(img.get(refY, refX)[0] - feature.get(feature.cols() / 2, feature.rows() / 2)[0]) > DIFFERENCE_LIMIT
                || Math.abs(img.get(refY, refX)[1] - feature.get(feature.cols() / 2, feature.rows() / 2)[1]) > DIFFERENCE_LIMIT
                || Math.abs(img.get(refY, refX)[2] - feature.get(feature.cols() / 2, feature.rows() / 2)[2]) > DIFFERENCE_LIMIT;
    }

    protected Mat createResultMat(Mat img, Mat feature) {
        int result_cols = img.cols() - feature.cols() + 1;
        int result_rows = img.rows() - feature.rows() + 1;
        return new Mat(result_rows, result_cols, CvType.CV_32FC1);
    }

    protected Mat matchFeature(Mat img, Mat feature, Mat result) {
        Imgproc.matchTemplate(img, feature, result, Imgproc.TM_CCOEFF_NORMED);
        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());
        return result;
    }

    /******************************************* Without OpenCV Version *******************************/

    private List<Sweet> extractSweetsForFeature(Mat img, int feature, int i, int orientation) {
        List<Sweet> featureSweets = new ArrayList<>();
        int rowLimit = adjustLimitAccordingToOrientation(orientation, img.rows());
        for (int k = 0; k < rowLimit; k += WALK) {
            for (int l = 0; l < img.cols(); l += WALK) {
                lookForFeatureInPixel(img, feature, i, featureSweets, k, l);
            }
        }
        return featureSweets;
    }

    private int adjustLimitAccordingToOrientation(int orientation, int rows) {
        if (orientation == Configuration.ORIENTATION_LANDSCAPE) {
            return rows;
        }
        return (int) (rows * 0.8);
    }

    private void lookForFeatureInPixel(Mat img, int feature, int i, List<Sweet> featureSweets, int k, int l) {
        if (isTheSameColor(img, feature , k, l, DIFFERENCE_LIMIT)) {
            if (k + OTHER_PIXEL < img.rows() && l + OTHER_PIXEL < img.cols()) {
                //second check a little further to be more precise
                if (isTheSameColor(img, feature, k + OTHER_PIXEL, l + OTHER_PIXEL, DIFFERENCE_LIMIT_OTHER)) {
                    Sweet sweet = new Sweet(i, new Point(l, k));
                    featureSweets.add(sweet);
                }
            }
        }
    }

    private static boolean isTheSameColor(Mat img, int reference, int k, int l, int difference) {
        int redReference = (reference >> 16) & 0xFF;
        int greenReference = (reference >> 8) & 0xFF;
        int blueReference = (reference >> 0) & 0xFF;
        return Math.abs(img.get(k, l)[0] - blueReference) < difference
                && Math.abs(img.get(k, l)[1] - greenReference) < difference
                && Math.abs(img.get(k, l)[2] - redReference) < difference;
    }

    /******************************************* Shared functions *******************************/

    private List<List<Sweet>> sortSweets(List<Sweet> messySweets, double colStep) {
        List<List<Sweet>> grid = new ArrayList<>();

        // Get minHeight and minWidth
        int minHeight = Integer.MAX_VALUE;
        int minWidth = Integer.MAX_VALUE;
        for (Sweet sweet : messySweets) {
            if (sweet.getY() < minHeight) {
                minHeight = (int) sweet.getY();
            }
            if (sweet.getX() < minWidth) {
                minWidth = (int) sweet.getX();
            }
        }

        fillGrid(messySweets, colStep, grid, minHeight, minWidth);
        return grid;
    }

    private void fillGrid(List<Sweet> messySweets, double colStep, List<List<Sweet>> grid, int minHeight, int minWidth) {
        for (Sweet sweet : messySweets) {
            addSweetToGrid(grid, sweet, minHeight - 10, minWidth - 10, colStep);
        }
    }

    private void readjustGridWithSymmetry(List<List<Sweet>> grid) {
        int highestLineSize = findHighestLineSize(grid);
        rearrangeRows(grid, highestLineSize);
    }

    private void rearrangeRows(List<List<Sweet>> grid, int highestLineSize) {
        for (int i = 0; i < grid.size(); i++) {
            while (highestLineSize > grid.get(i).size() && grid.get(i).size() != 0) {
                grid.get(i).add(null);
            }
        }
    }

    private int findHighestLineSize(List<List<Sweet>> grid) {
        int highestLineSize = 0;
        for (int i = 0; i < grid.size(); i++) {
            if (highestLineSize < grid.get(i).size()) {
                highestLineSize = grid.get(i).size();
            }
        }
        return highestLineSize;
    }

    private void alignGrid(List<List<Sweet>> grid, double colStep) {
        for (int i = 0; i < grid.size(); i++) {
            for (int j = 0; j < grid.get(i).size() - 1; j++) {
                if (grid.get(i).get(j) != null && grid.get(i).get(j + 1) != null && Math.abs(grid.get(i).get(j).getX() - grid.get(i).get(j + 1).getX()) > colStep * 1.5) {
                    grid.get(i).add(j + 1, null);
                }
            }
        }
    }

    private void printFinalGrid(List<List<Sweet>> grid) {
        for (int i = 0; i < grid.size(); i++) {
            Timber.v("%s : %s", i, grid.get(i).toString());
        }
    }


    private static void deleteCloseOnes(List<Sweet> featureSweets) {
        for (int i = 0; i < featureSweets.size() - 1; i++) {
            for (int j = i + 1; j < featureSweets.size(); j++) {
                boolean xClose = Math.abs(featureSweets.get(i).getX() - featureSweets.get(j).getX()) < STEP;
                boolean yClose = Math.abs(featureSweets.get(i).getY() - featureSweets.get(j).getY()) < STEP;
                if (xClose && yClose) {
                    featureSweets.remove(featureSweets.get(j));
                    j--;
                }
            }
        }
    }

    private void addSweetToGrid(List<List<Sweet>> grid, Sweet sweet, int firstPositionRow, int firstPositionCol, double colStep) {
        int row = (int) (Math.floor(sweet.getY() - firstPositionRow) / colStep) + 1;
        int col = (int) (Math.floor(sweet.getX() - firstPositionCol) / (colStep - 1));
        while (row >= grid.size()) {
            grid.add(new ArrayList<Sweet>());
        }
        while (col >= grid.get(row).size()) {
            grid.get(row).add(null);
        }
        grid.get(row).set(col, sweet);
    }
}
