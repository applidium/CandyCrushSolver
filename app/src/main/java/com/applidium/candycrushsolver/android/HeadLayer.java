package com.applidium.candycrushsolver.android;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.Toast;

import com.applidium.candycrushsolver.R;
import com.applidium.candycrushsolver.engine.Move;
import com.applidium.candycrushsolver.engine.Sweet;

import java.util.List;

import timber.log.Timber;

/**
 * Creates the head layer view which is displayed directly on window manager.
 * It means that the view is above every application's view on your phone -
 * until another application does the same.
 */
public class HeadLayer extends View {

    private static final int CANDY_SIZE = 45;
    private static final int REDUCTION_SIZE = 10;
    private final Context context;
    private final FrameLayout frameLayout;
    private WindowManager windowManager;
    private ImageView image;

   private final WindowManager.LayoutParams params = new WindowManager.LayoutParams(
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.WRAP_CONTENT,
            WindowManager.LayoutParams.TYPE_PHONE,
           WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_WATCH_OUTSIDE_TOUCH,
            PixelFormat.TRANSLUCENT);

    public HeadLayer(Context context) {
        super(context);
        this.context = context;
        frameLayout = new FrameLayout(this.context);
        addToWindowManager();
    }

    private void addToWindowManager() {
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        windowManager.addView(frameLayout, params);

        LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        layoutInflater.inflate(R.layout.head, frameLayout);
        image = (ImageView) frameLayout.findViewById(R.id.solution);
    }

    public void destroy() {
        if (frameLayout.isAttachedToWindow()) {
            windowManager.removeView(frameLayout);
        }
    }

    public void showBestMoveOnScreen(Move best, float factorDiminutionSize) {
        Bitmap bm = createBitmap();
        Timber.v("App cycle : just before display");
        //we divide sizes by REDUCTION_SIZE to make the bitmap creation faster
        int sweetSize = Math.round(CANDY_SIZE * factorDiminutionSize) / REDUCTION_SIZE;

        boolean success = drawMove(best, factorDiminutionSize, bm, sweetSize);
        if (!success) {
            return;
        }
        showFinalImage(bm);
    }

    public void showMovesOnScreen(List<Move> moves, float factorDiminutionSize) {
        Timber.v("App cycle : just before display");
        Bitmap bm = createBitmap();
        //we divide sizes by REDUCTION_SIZE to make the bitmap creation faster
        int sweetSize = Math.round(CANDY_SIZE * factorDiminutionSize) / REDUCTION_SIZE;

        for (Move move : moves) {
            boolean success = drawMove(move, factorDiminutionSize, bm, sweetSize);
            if (!success) {
                return;
            }
        }
        drawBorders(moves, bm, factorDiminutionSize, sweetSize);
        showFinalImage(bm);
    }

    private boolean drawMove(Move move, float factorDiminutionSize, Bitmap bm, int sweetSize) {
        int x = (int) move.getSweet1().getX();
        int y = (int) move.getSweet1().getY();
        Timber.v("x is %d and y is %d", x, y);
        if (move.getSweet1() == null || move.getSweet2() == null) {
            Toast.makeText(context, getResources().getString(R.string.err_move), Toast.LENGTH_SHORT).show();
            return false;
        }
        Sweet.Type color = move.getSweet1().getType();
        Sweet.Type color2 = move.getSweet2().getType();

        //we divide sizes by REDUCTION_SIZE to make the bitmap creation faster
        int xReal = Math.round(x * factorDiminutionSize) / REDUCTION_SIZE;
        int yReal = Math.round(y * factorDiminutionSize) / REDUCTION_SIZE;

        Timber.v("factor is %f", factorDiminutionSize);
        Timber.v("xReal is %d and yReal is %d", xReal, yReal);
        drawMoveOnBitmap(move, color, color2, xReal, yReal, bm, sweetSize);
        return true;
    }

    private void showFinalImage(Bitmap bm) {
        windowManager.updateViewLayout(frameLayout, params);
        if (image == null) {
            return;
        }
        image.setVisibility(VISIBLE);
        image.setImageBitmap(bm);
        image.setImageAlpha(140);
        image.setEnabled(false);
        Timber.v("App cycle : just after display");
    }

    private Bitmap createBitmap() {
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        Display display = wm.getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        params.width = size.x;
        params.height = size.y;

        int statusBarHeight = getStatusBarHeight();
        int actionBarHeight = getActionBarHeight();

        //we divide REDUCTION_SIZE by ten to make the bitmap creation faster
        int bitmapWidth = size.x / REDUCTION_SIZE;
        int bitmapHeight = (size.y - statusBarHeight - actionBarHeight) / REDUCTION_SIZE;

        Bitmap bm = Bitmap.createBitmap(bitmapWidth, bitmapHeight, Bitmap.Config.ARGB_8888);
        bm = colorBitmapInBlack(bm);
        return bm;
    }

    private Bitmap colorBitmapInBlack(Bitmap bm) {
        for (int i = 0; i < bm.getWidth(); i++) {
            for (int j = 0; j < bm.getHeight(); j++) {
                bm.setPixel(i, j, Color.rgb(0, 0, 0));
            }
        }
        return bm;
    }

    private int getActionBarHeight() {
        int actionBarHeight = 0;
        int resource2 = getResources().getIdentifier("action_bar_height", "dimen", "android");
        if (resource2 > 0) {
            actionBarHeight = context.getResources().getDimensionPixelSize(resource2);
        }
        return actionBarHeight;
    }

    private int getStatusBarHeight() {
        int statusBarHeight = 0;
        int resource = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resource > 0) {
            statusBarHeight = context.getResources().getDimensionPixelSize(resource);
        }
        return statusBarHeight;
    }

    private void drawMoveOnBitmap(Move best, Sweet.Type color, Sweet.Type color2, int xReal, int yReal, Bitmap bm, int sweetSize) {
        switch (best.findDirection()) {
            case UP:
                setWhitePixelsOnSolution(xReal, xReal + sweetSize, yReal - sweetSize, yReal + sweetSize, bm);
                Timber.v("UP : %1$d %2$d, %3$s %4$s", xReal, yReal, color, color2);
                break;
            case DOWN:
                setWhitePixelsOnSolution(xReal, xReal + sweetSize, yReal, yReal + sweetSize * 2, bm);
                Timber.v("DOWN : %1$d %2$d, %3$s %4$s", xReal, yReal, color, color2);
                break;
            case LEFT:
                setWhitePixelsOnSolution(xReal - sweetSize, xReal + sweetSize, yReal, yReal + sweetSize, bm);
                Timber.v("LEFT : %1$d %2$d, %3$s %4$s", xReal, yReal, color, color2);
                break;
            case RIGHT:
                setWhitePixelsOnSolution(xReal, xReal + sweetSize * 2, yReal, yReal + sweetSize, bm);
                Timber.v("RIGHT : %1$d %2$d, %3$s %4$s", xReal, yReal, color, color2);
                break;
            default:
                Toast.makeText(context, getResources().getString(R.string.err_move), Toast.LENGTH_LONG).show();
        }
    }

    private void setWhitePixelsOnSolution(int xStart, int xStop, int yStart, int yStop, Bitmap bm) {
        for (int i = xStart; i < xStop; i++) {
            for (int j = yStart; j < yStop; j++) {
                if (i > 0 && j > 0 && i < bm.getWidth() && j < bm.getHeight()) {
                    bm.setPixel(i, j, Color.rgb(255, 255, 255));
                }
            }
        }
    }

    private void drawBorders(List<Move> moves, Bitmap bm, float factorDiminutionSize, int sweetSize) {
        for (Move move : moves) {
            int x = (int) move.getSweet1().getX();
            int y = (int) move.getSweet1().getY();

            //we divide sizes by REDUCTION_SIZE to make the bitmap creation faster
            int xReal = Math.round(x * factorDiminutionSize) / REDUCTION_SIZE;
            int yReal = Math.round(y * factorDiminutionSize) / REDUCTION_SIZE;

            switch (move.findDirection()) {
                case UP:
                    setBorderPixel(xReal, xReal + sweetSize, yReal - sweetSize, yReal + sweetSize, bm);
                    break;
                case DOWN:
                    setBorderPixel(xReal, xReal + sweetSize, yReal, yReal + sweetSize * 2, bm);
                    break;
                case LEFT:
                    setBorderPixel(xReal - sweetSize, xReal + sweetSize, yReal, yReal + sweetSize, bm);
                    break;
                case RIGHT:
                    setBorderPixel(xReal, xReal + sweetSize * 2, yReal, yReal + sweetSize, bm);
                    break;
            }
        }
    }

    private void setBorderPixel(int xStart, int xStop, int yStart, int yStop, Bitmap bm) {
        for (int i = xStart; i < xStop; i++) {
            bm.setPixel(i, yStart, ContextCompat.getColor(getContext(), R.color.colorBackground));
            bm.setPixel(i, yStop - 1, ContextCompat.getColor(getContext(), R.color.colorBackground));
        }
        for (int j = yStart; j < yStop; j++) {
            bm.setPixel(xStart, j, ContextCompat.getColor(getContext(), R.color.colorBackground));
            bm.setPixel(xStop - 1, j, ContextCompat.getColor(getContext(), R.color.colorBackground));
        }
    }
}
