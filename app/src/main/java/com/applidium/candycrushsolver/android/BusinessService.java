package com.applidium.candycrushsolver.android;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.ContextCompat;
import android.widget.Toast;

import com.applidium.candycrushsolver.R;
import com.applidium.candycrushsolver.engine.FeaturesExtractor;
import com.applidium.candycrushsolver.engine.Move;
import com.applidium.candycrushsolver.engine.MoveFinder;
import com.applidium.candycrushsolver.engine.Sweet;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import timber.log.Timber;

public class BusinessService extends Service {

    private static final int NB_FEATURES = 6;
    private static final int SWEET_SIZE = 45;
    private static final int FEATURE_SIZE = 35;
    private String STORE_DIRECTORY;
    private int DELAY = 1000;
    private static final String KEY_STRING_CHOICE = "KEY_STRING_CHOICE";

    private HeadLayer headLayer;
    private boolean choseBestMove;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) { //your attention space rangers, this is starCommand !
        Timber.v("Life cycle : service just started");
        initHeadLayer();
        STORE_DIRECTORY = getFilesDir().getAbsolutePath();
        choseBestMove = intent.getExtras().getBoolean(KEY_STRING_CHOICE);
        Timber.v("The choice is %s", choseBestMove);
        findAndDisplaySolution();

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        destroyHeadLayer();
        stopForeground(true);
    }

    private void initHeadLayer() {
        Timber.v("App cycle : service inside initialisation");
        headLayer = new HeadLayer(this);
    }

    private void destroyHeadLayer() {
        if (headLayer == null) {
            return;
        }
        headLayer.destroy();
        headLayer = null;
    }

    private void findAndDisplaySolution() {
        Timber.v("App cycle : begin work");
        float density = getResources().getDisplayMetrics().density;
        int[] features = loadFeatures();
        Mat image = Imgcodecs.imread(STORE_DIRECTORY + "/myscreen.png");
        if (image == null || image.empty()) {
            Toast.makeText(this, getResources().getString(R.string.err_image), Toast.LENGTH_LONG).show();
            return;
        }

        FeaturesExtractor extractor = new FeaturesExtractor();
        int orientation = getResources().getConfiguration().orientation;
        Timber.v("App cycle : just before extractor");
        List<List<Sweet>> grid = extractor.extractFeaturesFromImage(image, features, orientation);
        Timber.v("App cycle : just after extractor");

        if (grid == null) {
            Toast.makeText(this, getResources().getString(R.string.err_grid), Toast.LENGTH_LONG).show();
            return;
        }

        int [] numberOfColorsInGrid = getNumberOfColorsInGrid(grid);
        int percentageOfOrangeSweets = numberOfColorsInGrid[2] / 70;  //2 is the number for orange color and 70 is approximately a grid size

        int totalNumberOfSweets =
                numberOfColorsInGrid[0]
                + numberOfColorsInGrid[1]
                + numberOfColorsInGrid[2]
                + numberOfColorsInGrid[3]
                + numberOfColorsInGrid[4]
                + numberOfColorsInGrid[5];
        //try to find if the screen is a level or not
        if (percentageOfOrangeSweets > 0.5 || getNumberOfDifferentColorsInGrid(grid) < 3) {
            //we don't show result for grid with too much orange sweets
            //because it often means that we're on the splash screen
            //(or when there are not a lot of colors - meaning we're on a level selection or menu screen)
            // -- no error message on a toast because the player doesn't care yet what our services does (as he is not on the level)
            return;
        }

        if (totalNumberOfSweets < 30) {
            //we don't show results for grid with less than 20 sweets :
            //because it often means that we're not on the level yet (level selection for example)
            //or the screenshot is not perfectly loaded and solution will be flawed
            Toast.makeText(this, getResources().getString(R.string.err_grid), Toast.LENGTH_LONG).show();
            return;
        }

        MoveFinder mv = new MoveFinder(grid);
        if (choseBestMove) {
            Timber.v("answer yes");
            findAndDisplayBestMove(density, image, mv);
        } else {
            Timber.v("answer no");
            findAndDisplayEveryMove(density, image, mv);
        }
    }

    private void findAndDisplayBestMove(float density, Mat resizeImage, MoveFinder mv) {
        Move best = mv.findMove();
        if (best == null) {
            Toast.makeText(this, getResources().getString(R.string.err_move), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Mat[] featuresOpenCV = loadFeaturesForOpenCvVersion();

            recenterSweet(best, resizeImage, featuresOpenCV);
            Move opposedMove = new Move(best.getSweet2(), best.getSweet1(), best.getScore());
            recenterSweet(opposedMove, resizeImage, featuresOpenCV);

            Timber.v("App cycle : move just found");
            headLayer.showBestMoveOnScreen(best, density);
            Timber.v("App cycle : move on screen");
            clearScreenAfterAMoment();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void findAndDisplayEveryMove(float density, Mat resizeImage, MoveFinder mv) {
        mv.findAllMoves();
        List<Move> moves = mv.getMoves();
        if (moves == null || moves.size() == 0) {
            Toast.makeText(this, getResources().getString(R.string.err_move), Toast.LENGTH_SHORT).show();
            return;
        }
        try {
            Mat[] featuresOpenCV = loadFeaturesForOpenCvVersion();
            for (Move move : moves) {
                recenterSweet(move, resizeImage, featuresOpenCV);
            }
            Timber.v("App cycle : moves just found");
            headLayer.showMovesOnScreen(moves, density);
            Timber.v("App cycle : moves on screen");
            DELAY = 6000;
            clearScreenAfterAMoment();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void clearScreenAfterAMoment() {
        new Handler().postDelayed(new Runnable() {
            public void run() {
                Timber.v("App cycle : time to put display off");
                if (headLayer != null) {
                    headLayer.destroy();
                }
            }
        }, DELAY);
    }

    private int [] getNumberOfColorsInGrid(List<List<Sweet>> grid) {
        int [] colorsPresent = new int [6];
        for (int i = 0; i < grid.size(); i++) {
            if (grid.get(i) != null) {
                for (int j = 0; j < grid.get(i).size(); j++) {
                    if (grid.get(i).get(j) != null) {
                        colorsPresent[grid.get(i).get(j).getType().ordinal()] += 1;
                    }
                }
            }
        }
        Timber.v("Color counts are : %s", Arrays.toString(colorsPresent));
        return colorsPresent;
    }

    private int getNumberOfDifferentColorsInGrid(List<List<Sweet>> grid) {
        int [] colorsPresent = new int [6];
        for (int i = 0; i < grid.size(); i++) {
            if (grid.get(i) != null) {
                for (int j = 0; j < grid.get(i).size(); j++) {
                    if (grid.get(i).get(j) != null) {
                        colorsPresent[grid.get(i).get(j).getType().ordinal()] = 1;
                     }
                }
            }
        }
        int sum = 0;
        for( int i : colorsPresent) {
            sum += i;
        }
        return sum;
    }

    public void recenterSweet(Move best, Mat image, Mat[] features) {
        int x = (int) best.getSweet1().getX();
        int y = (int) best.getSweet1().getY();

        Mat cropped = cropImage(image, x, y);
        Mat result = cropped.clone();
        Mat feature = resizeFeature(best, features);

        Imgproc.matchTemplate(cropped, feature, result, Imgproc.TM_CCOEFF_NORMED);
        Core.MinMaxLocResult mmr = Core.minMaxLoc(result);
        Point matchLoc = mmr.maxLoc;
        int xInCroppedImage = (int) matchLoc.x;
        int yInCroppedImage = (int) matchLoc.y;

        //convert for big image
        int refX = Math.min(xInCroppedImage + Math.max(0, x - SWEET_SIZE), image.cols());
        int refY = Math.min(yInCroppedImage + Math.max(0, y - SWEET_SIZE), image.rows());

        best.getSweet1().setPosition(new Point(refX, refY));
    }

    private Mat resizeFeature(Move best, Mat[] features) {
        int color = best.getSweet1().getType().ordinal();
        Size sz = new Size(FEATURE_SIZE, FEATURE_SIZE);
        Imgproc.resize(features[color], features[color], sz);
        return features[color];
    }

    private Mat cropImage(Mat image, int x, int y) {
        return image.submat(
                    Math.max(0, y - SWEET_SIZE),
                    Math.min(image.rows(), y + SWEET_SIZE),
                    Math.max(0, x - SWEET_SIZE),
                    Math.min(image.cols(), x + SWEET_SIZE)
            );
    }

    protected int[] loadFeatures() {
        return new int[]{
            ContextCompat.getColor(this, R.color.colorGreen),
            ContextCompat.getColor(this, R.color.colorRed),
            ContextCompat.getColor(this, R.color.colorOrange),
            ContextCompat.getColor(this, R.color.colorYellow),
            ContextCompat.getColor(this, R.color.colorPurple),
            ContextCompat.getColor(this, R.color.colorBlue)
        };
    }

    protected Mat[] loadFeaturesForOpenCvVersion() throws IOException {
        Mat[] features = new Mat[NB_FEATURES];
        if (!OpenCVLoader.initDebug()) {
            Toast.makeText(this, getResources().getString(R.string.err_opencv), Toast.LENGTH_LONG).show();
        } else {
            features[0] = Utils.loadResource(this, R.raw.green);
            features[1] = Utils.loadResource(this, R.raw.red);
            features[2] = Utils.loadResource(this, R.raw.orange);
            features[3] = Utils.loadResource(this, R.raw.yellow);
            features[4] = Utils.loadResource(this, R.raw.purple);
            features[5] = Utils.loadResource(this, R.raw.blue);
        }
        return features;
    }


    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
