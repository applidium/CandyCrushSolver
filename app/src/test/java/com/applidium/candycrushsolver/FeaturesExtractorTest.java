package com.applidium.candycrushsolver;

import android.content.res.Configuration;

import com.applidium.candycrushsolver.engine.FeaturesPainter;
import com.applidium.candycrushsolver.engine.Sweet;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.Mat;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class FeaturesExtractorTest extends BaseTest {

    @Test
    public void test() throws Exception {
        Mat image = readPng("screenshot", -1);
        Imgproc.pyrDown(image, image, new Size(image.cols() / 2, image.rows() / 2));
        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(image, features, Configuration.ORIENTATION_PORTRAIT);
        FeaturesPainter.drawRectanglesFromSweetsGrid(grid, image);
        Imgcodecs.imwrite("out.png", image);

        List<List<Sweet.Type>> fixture1 = GridFixture.fixture1();
        assertGridMatchesFixture(grid, fixture1);
    }

    @Test
    public void testSmallerGrid() throws Exception {
        Mat image = readPng("screenshot2", -1);
        Imgproc.pyrDown(image, image, new Size(image.cols() / 2, image.rows() / 2));
        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(image, features, Configuration.ORIENTATION_PORTRAIT);
        FeaturesPainter.drawRectanglesFromSweetsGrid(grid, image);
        Imgcodecs.imwrite("out2.png", image);


        List<List<Sweet.Type>> fixture2 = GridFixture.fixture2();
        assertGridMatchesFixture(grid, fixture2);
    }

    @Test
    public void testWithoutYellow() throws Exception {
        Mat image = readPng("screenshot5", -1);
        Imgproc.pyrDown(image, image, new Size(image.cols() / 2, image.rows() / 2));
        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(image, features, Configuration.ORIENTATION_PORTRAIT);
        FeaturesPainter.drawRectanglesFromSweetsGrid(grid, image);
        Imgcodecs.imwrite("out3.png", image);

        List<List<Sweet.Type>> model = GridFixture.fixture3();
        assertGridMatchesFixture(grid, model);
    }

    @Test
    public void testWithHoles() throws Exception {
        Mat image = readPng("screenshot6", -1);
        Imgproc.pyrDown(image, image, new Size(image.cols() / 2, image.rows() / 2));
        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(image, features, Configuration.ORIENTATION_PORTRAIT);
        FeaturesPainter.drawRectanglesFromSweetsGrid(grid, image);
        Imgcodecs.imwrite("out7.png", image);

        List<List<Sweet.Type>> model = GridFixture.fixture4();
        assertGridMatchesFixture(grid, model);
    }

    @Test
    public void testWithMoreHoles() throws Exception {
        Mat image = readPng("screenshot10", -1);
        Mat resizeImage = new Mat();
        Size sz = new Size(image.cols() / 3, image.rows() / 3);
        Imgproc.resize(image, resizeImage, sz);
        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(resizeImage, features, Configuration.ORIENTATION_PORTRAIT);
        FeaturesPainter.drawRectanglesFromSweetsGrid(grid, resizeImage);
        Imgcodecs.imwrite("out9.png", resizeImage);

        List<List<Sweet.Type>> model = GridFixture.fixture5();
        assertGridMatchesFixture(grid, model);
    }

    @Test
    public void testOpenCvVersion() throws Exception {
        Mat image = readPng("screenshot", -1);
        Imgproc.pyrDown(image, image, new Size(image.cols() / 2, image.rows() / 2));
        List<Sweet> grid = extractor.extractSweetsForFeatureWithOpenCV(image, featuresOpenCV[0], 0);
        FeaturesPainter.drawRectanglesFromMessySweets(grid, image);
        Imgcodecs.imwrite("out13.png", image);
    }

    private static void assertGridMatchesFixture(List<List<Sweet>> grid, List<List<Sweet.Type>> fixture) {
        Assert.assertEquals(fixture.size(), grid.size());
        for (int i = 0; i < fixture.size(); i++) {
            Assert.assertEquals(fixture.get(i).size(), grid.get(i).size());
            for (int j = 0; j < fixture.get(i).size(); j++) {
                Assert.assertFalse(
                    "no match on (" + i + "," + j + ")",
                    oneNullOtherNot(grid, fixture, i, j)
                );
                Sweet sweet = gridGet(grid, i, j);
                if (sweet != null) {
                    Assert.assertEquals(gridGet(fixture, i, j), sweet.getType());
                }
            }
        }
    }

    private static boolean oneNullOtherNot(List<List<Sweet>> grid, List<List<Sweet.Type>> reference, int i, int j) {
        return (gridGet(grid, i, j) == null && gridGet(reference, i, j) != null)
            || (gridGet(grid, i, j) != null && gridGet(reference, i, j) == null);
    }

    private static <T> T gridGet(List<List<T>> grid, int i, int j) {
        if (grid.size() <= i || grid.get(i).size() <= j) {
            return null;
        }
        return grid.get(i).get(j);
    }

}
