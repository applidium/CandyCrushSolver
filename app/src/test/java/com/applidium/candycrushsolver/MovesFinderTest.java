package com.applidium.candycrushsolver;

import android.content.res.Configuration;

import com.applidium.candycrushsolver.android.BusinessService;
import com.applidium.candycrushsolver.engine.FeaturesPainter;
import com.applidium.candycrushsolver.engine.Move;
import com.applidium.candycrushsolver.engine.MoveFinder;
import com.applidium.candycrushsolver.engine.Sweet;

import org.junit.Assert;
import org.junit.Test;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.List;

public class MovesFinderTest extends BaseTest {

    @Test
    public void testFindMoves() throws Exception {
        Mat image = readPng("screenshot", -1);
        Imgproc.pyrDown(image, image, new Size(image.cols() / 2, image.rows() / 2));
        Mat movesPossible = new Mat(image.rows(), image.cols(), CvType.CV_32FC3);
        image.copyTo(movesPossible);

        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(image, features, Configuration.ORIENTATION_PORTRAIT);

        FeaturesPainter.drawRectanglesFromSweetsGrid(grid, image);
        Imgcodecs.imwrite("out_other.png", image);

        MoveFinder mv = new MoveFinder(grid);
        Move best = mv.findMove();
        Assert.assertEquals(best.findDirection(), Move.Direction.UP);
        List<Move> moves = mv.getMoves();
        recenterMoves(image, moves);
        printMoves(movesPossible, moves, best, "out4.png");
    }

    @Test
    public void testFindSpecial5Moves() throws Exception {
        Mat image = readPng("screenshot_5special", -1);
        Imgproc.pyrDown(image, image, new Size(image.cols() / 2, image.rows() / 2));
        Mat movesPossible = new Mat(image.rows(), image.cols(), CvType.CV_32FC3);
        image.copyTo(movesPossible);

        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(image, features, Configuration.ORIENTATION_PORTRAIT);
        FeaturesPainter.drawRectanglesFromSweetsGrid(grid, image);

        MoveFinder mv = new MoveFinder(grid);
        Move best = mv.findMove();
        Assert.assertEquals(best.findDirection(), Move.Direction.LEFT);
        List<Move> moves = mv.getMoves();
        printMoves(movesPossible, moves, best, "out5.png");
    }

    @Test
    public void testFindSpecialBombMoves() throws Exception {
        Mat image = readPng("screenshot_bomb", -1);
        Imgproc.pyrDown(image, image, new Size(image.cols() / 2, image.rows() / 2));
        Mat movesPossible = new Mat(image.rows(), image.cols(), CvType.CV_32FC3);
        image.copyTo(movesPossible);

        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(image, features, Configuration.ORIENTATION_PORTRAIT);

        MoveFinder mv = new MoveFinder(grid);
        Move best = mv.findMove();
        Assert.assertEquals(best.findDirection(), Move.Direction.RIGHT);
        List<Move> moves = mv.getMoves();
        printMoves(movesPossible, moves, best, "out6.png");
    }

    @Test
    public void testFindSpecialBombMovesLeft() throws Exception {
        Mat image = readPng("screenshot11", -1);
        Imgproc.pyrDown(image, image, new Size(image.cols() / 2, image.rows() / 2));
        Mat movesPossible = new Mat(image.rows(), image.cols(), CvType.CV_32FC3);
        image.copyTo(movesPossible);

        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(image, features, Configuration.ORIENTATION_PORTRAIT);

        MoveFinder mv = new MoveFinder(grid);
        Move best = mv.findMove();
        Assert.assertEquals(best.findDirection(), Move.Direction.LEFT);
        List<Move> moves = mv.getMoves();
        printMoves(movesPossible, moves, best, "out10.png");
    }

    @Test
    public void test4SweetsVsBombMove() throws Exception {
        Mat image = readPng("screenshot12", -1);
        Mat resizeImage = new Mat();
        int newCol = (int) Math.round(image.cols() / 2.6);
        int newRow = (int) Math.round(image.rows() / 2.6);
        Size sz = new Size(newCol, newRow);
        Imgproc.resize(image, resizeImage, sz);
        Mat movesPossible = new Mat(resizeImage.rows(), resizeImage.cols(), CvType.CV_32FC3);
        resizeImage.copyTo(movesPossible);

        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(resizeImage, features, Configuration.ORIENTATION_PORTRAIT);

        MoveFinder mv = new MoveFinder(grid);
        Move best = mv.findMove();
        List<Move> moves = mv.getMoves();
        printMoves(movesPossible, moves, best, "out12.png");
    }

    @Test
    public void testFindSpecialSymetricMoves() throws Exception {
        Mat image = readPng("screenshot2", -1);
        Imgproc.pyrDown(image, image, new Size(image.cols() / 2, image.rows() / 2));
        Mat movesPossible = new Mat(image.rows(), image.cols(), CvType.CV_32FC3);
        image.copyTo(movesPossible);

        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(image, features, Configuration.ORIENTATION_PORTRAIT);

        MoveFinder mv = new MoveFinder(grid);
        Move best = mv.findMove();
        Assert.assertEquals(best.getScore(), 2);
        List<Move> moves = mv.getMoves();
        printMoves(movesPossible, moves, best, "out8.png");
    }

    @Test
    public void testWithJelly() throws Exception {
        Mat image = readPng("screenshot10", -1);
        Mat resizeImage = new Mat();
        int newCol = (int) Math.round(image.cols() / 2.6);
        int newRow = (int) Math.round(image.rows() / 2.6);
        Size sz = new Size(newCol, newRow);
        Imgproc.resize(image, resizeImage, sz);
        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(resizeImage, features, Configuration.ORIENTATION_PORTRAIT);

        MoveFinder mv = new MoveFinder(grid);
        Move best = mv.findMove();
        List<Move> moves = mv.getMoves();
        recenterMoves(resizeImage, moves);
        printMoves(resizeImage, moves, best, "out11.png");
    }

    private void recenterMoves(Mat resizeImage, List<Move> moves) {
        for (Move move : moves) {
            //test : to see if sweets are centered
            BusinessService bs = new BusinessService();
            bs.recenterSweet(move, resizeImage, featuresOpenCV);
        }
    }

    public void printMoves(Mat image, List<Move> moves, Move best, String outFile) {
        for (int i = 0; i < moves.size(); i++) {
            Scalar scalar;
            if (moves.get(i) == best) {
                scalar = new Scalar(0, 0, 0, 255); //black is best
            } else if (moves.get(i).getScore() == 1) {
                scalar = new Scalar(255, 255, 255, 255); //white is plain
            } else if (moves.get(i).getScore() == 2) {
                scalar = new Scalar(0, 0, 255, 255); //red is better than white
            } else if (moves.get(i).getScore() == 3) {
                scalar = new Scalar(0, 255, 0, 255); //green is better than red
            } else {
                scalar = new Scalar(255, 0, 0, 0); //blue is better than green
            }
            Imgproc.rectangle(
                image,
                moves.get(i).getSweet1().getPosition(),
                new Point(moves.get(i).getSweet1().getPosition().x + 35, moves.get(i).getSweet1().getPosition().y + 35),
                scalar);
        }
        Imgcodecs.imwrite(outFile, image);
    }
}
