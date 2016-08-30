package com.applidium.candycrushsolver;

import com.applidium.candycrushsolver.engine.FeaturesExtractor;

import org.opencv.core.Mat;
import org.opencv.imgcodecs.Imgcodecs;

import java.io.IOException;

public class BaseTest {

    private static final int NB_FEATURES = 6;
    private static final String STORE_DIRECTORY_TEST = "./build/intermediates/classes/test/debug/assets/";

    protected final FeaturesExtractor extractor = new FeaturesExtractor();
    protected int [] features;
    protected Mat[] featuresOpenCV;

    @org.junit.Before
    public void setUp() throws Exception {
        System.loadLibrary("opencv_java310");

        features = loadFeatures();
        featuresOpenCV = loadFeaturesForOpenCV(-1);
    }

    private int[] loadFeatures() {
        return new int[]{
            0x42C60A,
            0xF80001,
            0xF77D08,
            0xFFC301,
            0xAD18F8,
            0x076DFE
        };
    }

    private Mat[] loadFeaturesForOpenCV(int flags) throws IOException {
        Mat[] features = new Mat[NB_FEATURES];
        features[0] = readPng("green", flags);
        features[1] = readPng("red", flags);
        features[2] = readPng("orange", flags);
        features[3] = readPng("yellow", flags);
        features[4] = readPng("purple", flags);
        features[5] = readPng("blue", flags);
        return features;
    }

    protected Mat readPng(String fileName, int flags) {
        return Imgcodecs.imread((STORE_DIRECTORY_TEST + fileName + ".png"), flags);
    }
}
