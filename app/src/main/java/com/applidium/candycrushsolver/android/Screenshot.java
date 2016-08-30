package com.applidium.candycrushsolver.android;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.hardware.display.DisplayManager;
import android.hardware.display.VirtualDisplay;
import android.media.Image;
import android.media.ImageReader;
import android.media.projection.MediaProjection;
import android.media.projection.MediaProjectionManager;
import android.os.Handler;
import android.os.HandlerThread;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.OrientationEventListener;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;

import timber.log.Timber;

public class Screenshot {

    private static final String SCREENCAP_NAME = "screencap";
    private static final String THREAD_SCREENSHOT = "Thread screenshot";
    private static final int VIRTUAL_DISPLAY_FLAGS = DisplayManager.VIRTUAL_DISPLAY_FLAG_OWN_CONTENT_ONLY | DisplayManager.VIRTUAL_DISPLAY_FLAG_PUBLIC;
    private static final int DELAY = 1000;
    private static String STORE_DIRECTORY;
    private static MediaProjection mediaProjection;
    private MediaProjectionManager projectionManager;
    private ImageReader imageReader;
    private Handler handler;
    private Display display;
    private VirtualDisplay virtualDisplay;
    private DisplayMetrics metrics;
    private int density;
    private int width;
    private int height;
    private int rotation;
    private OrientationChangeCallback orientationChangeCallback;

    /******************************************* Lifecycle *******************************/

    public Screenshot(String storeDirectory) {
        STORE_DIRECTORY = storeDirectory;
    }

    public void callForTheProjectionManager(Activity activity) {
        projectionManager = (MediaProjectionManager) activity.getSystemService(Context.MEDIA_PROJECTION_SERVICE);
        Timber.v("Manager");
    }

    public void afterActivityResult(Activity activity, int resultCode, Intent data) {
        Timber.v("Activity result");
        mediaProjection = projectionManager.getMediaProjection(resultCode, data);

        if (mediaProjection != null) {
            if(!isDirectoryCreationSuccessful()) {
                Timber.e("failed to create file storage directory.");
                return;
            }

            displayMetrics(activity);

            // create virtual display depending on device width / height
            createVirtualDisplay();

            registerOrientationChangeCallback(activity);

            mediaProjection.registerCallback(new MediaProjectionStopCallback(), handler);
        }
    }

    private boolean isDirectoryCreationSuccessful(){
        File storeDirectory = new File(STORE_DIRECTORY);
        if (!storeDirectory.exists()) {
            return storeDirectory.mkdirs();
        }
        return true;
    }

    private void registerOrientationChangeCallback(Activity activity) {
        orientationChangeCallback = new OrientationChangeCallback(activity);
        if (orientationChangeCallback.canDetectOrientation()) {
            orientationChangeCallback.enable();
        }
    }

    private void displayMetrics(Activity activity) {
        metrics = activity.getResources().getDisplayMetrics();
        density = metrics.densityDpi;
        display = activity.getWindowManager().getDefaultDisplay();
        Timber.v("Metrics displayed");
    }

    public void startCaptureHandlingThread() {
        HandlerThread thread = new HandlerThread(THREAD_SCREENSHOT);
        thread.start();
        handler = new Handler(thread.getLooper());
    }

    public Intent createCaptureIntent() {
        return projectionManager.createScreenCaptureIntent();
    }


    public void stopHandler() {
        handler.post(new Runnable() {
            @Override
            public void run() {
                if (mediaProjection != null) {
                    mediaProjection.stop();
                }
            }
        });
    }

    /******************************************* Factoring Virtual Display creation ****************/

    private void createVirtualDisplay() {
        // get width and height
        Point size = new Point();
        display.getSize(size);
        width = size.x;
        height = size.y;

        // start capture reader
        imageReader = ImageReader.newInstance(width, height, PixelFormat.RGBA_8888, 2);
        virtualDisplay = mediaProjection.createVirtualDisplay(SCREENCAP_NAME, width, height, density, VIRTUAL_DISPLAY_FLAGS, imageReader.getSurface(), null, handler);

        handler.postDelayed(getRunnableThatTakesScreenshot(), 2);
    }

    private Runnable getRunnableThatTakesScreenshot() {
        return new Runnable() {
            @Override
            public void run() {
                Image image = null;
                FileOutputStream fos = null;
                Bitmap bitmap = null;

                try {
                    image = imageReader.acquireLatestImage();
                    if (image != null) {
                        Image.Plane[] planes = image.getPlanes();
                        ByteBuffer buffer = planes[0].getBuffer();
                        int pixelStride = planes[0].getPixelStride();
                        int rowStride = planes[0].getRowStride();
                        int rowPadding = rowStride - pixelStride * width;

                        bitmap = createBitmap(buffer, pixelStride, rowPadding);
                        Bitmap resizedBitmap = resizeBitmap(bitmap);
                        fos = writeBitmapToFile(resizedBitmap);
                    }

                } catch (Exception e) {
                    e.printStackTrace();
                    mediaProjection.stop();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException ioe) {
                            ioe.printStackTrace();
                        }
                    }
                    if (bitmap != null) {
                        bitmap.recycle();
                    }
                    if (image != null) {
                        image.close();
                    }
                    handler.postDelayed(getRunnableThatTakesScreenshot(), DELAY);
                }
            }
        };
    }

    @NonNull
    private Bitmap resizeBitmap(Bitmap bitmap) {
        float d = metrics.density;
        int newHeight = Math.round(bitmap.getHeight() / d);
        int newWidth = Math.round(bitmap.getWidth() / d);
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, false);
    }

    @NonNull
    private FileOutputStream writeBitmapToFile(Bitmap bitmap) throws FileNotFoundException {
        FileOutputStream fos = new FileOutputStream(STORE_DIRECTORY + "/myscreen.png");
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
        return fos;
    }

    @NonNull
    private Bitmap createBitmap(ByteBuffer buffer, int pixelStride, int rowPadding) {
        Bitmap bitmap;
        bitmap = Bitmap.createBitmap(width + rowPadding / pixelStride, height, Bitmap.Config.ARGB_8888);
        bitmap.copyPixelsFromBuffer(buffer);
        return bitmap;
    }

    private class OrientationChangeCallback extends OrientationEventListener {
        public OrientationChangeCallback(Context context) {
            super(context);
        }

        @Override
        public void onOrientationChanged(int orientation) {
            synchronized (this) {
                final int rotation = display.getRotation();
                if (rotation != Screenshot.this.rotation) {
                    Screenshot.this.rotation = rotation;
                    try {
                        cleanUpVirtualDisplay();
                        cleanUpImageReader();

                        // re-create virtual display depending on device width / height
                        createVirtualDisplay();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    private void cleanUpVirtualDisplay() {
        if (virtualDisplay != null) {
            virtualDisplay.release();
        }
    }

    private void cleanUpImageReader() {
        if (imageReader != null) {
            imageReader.setOnImageAvailableListener(null, null);
        }
    }

    private void disableOrientationChangeCallback() {
        if (orientationChangeCallback != null) {
            orientationChangeCallback.disable();
        }
    }

    private class MediaProjectionStopCallback extends MediaProjection.Callback {
        @Override
        public void onStop() {
            Timber.e("stopping projection");
            handler.post(new Runnable() {
                @Override
                public void run() {
                    cleanUpVirtualDisplay();
                    cleanUpImageReader();
                    disableOrientationChangeCallback();
                    mediaProjection.unregisterCallback(MediaProjectionStopCallback.this);
                }
            });
        }
    }
}
