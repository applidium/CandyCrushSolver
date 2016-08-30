package org.opencv.android;

import java.util.Arrays;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.SurfaceTexture;
import android.hardware.camera2.CameraAccessException;
import android.hardware.camera2.CameraCaptureSession;
import android.hardware.camera2.CameraCharacteristics;
import android.hardware.camera2.CameraDevice;
import android.hardware.camera2.CameraManager;
import android.hardware.camera2.CaptureRequest;
import android.hardware.camera2.params.StreamConfigurationMap;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Size;
import android.view.Surface;

import timber.log.Timber;

@TargetApi(21)
public class Camera2Renderer extends CameraGLRendererBase {

    private CameraDevice mCameraDevice;
    private CameraCaptureSession mCaptureSession;
    private CaptureRequest.Builder mPreviewRequestBuilder;
    private String mCameraID;
    private Size mPreviewSize = new Size(-1, -1);

    private HandlerThread mBackgroundThread;
    private Handler mBackgroundHandler;
    private Semaphore mCameraOpenCloseLock = new Semaphore(1);

    Camera2Renderer(CameraGLSurfaceView view) {
        super(view);
    }

    @Override
    protected void doStart() {
        Timber.d("doStart");
        startBackgroundThread();
        super.doStart();
    }


    @Override
    protected void doStop() {
        Timber.d("doStop");
        super.doStop();
        stopBackgroundThread();
    }

    boolean cacPreviewSize(final int width, final int height) {
        Timber.i("cacPreviewSize: "+width+"x"+height);
        if(mCameraID == null) {
            Timber.e("Camera isn't initialized!");
            return false;
        }
        CameraManager manager = (CameraManager) mView.getContext()
                .getSystemService(Context.CAMERA_SERVICE);
        try {
            CameraCharacteristics characteristics = manager
                    .getCameraCharacteristics(mCameraID);
            StreamConfigurationMap map = characteristics
                    .get(CameraCharacteristics.SCALER_STREAM_CONFIGURATION_MAP);
            int bestWidth = 0, bestHeight = 0;
            float aspect = (float)width / height;
            for (Size psize : map.getOutputSizes(SurfaceTexture.class)) {
                int w = psize.getWidth(), h = psize.getHeight();
                Timber.d("trying size: "+w+"x"+h);
                if ( width >= w && height >= h &&
                     bestWidth <= w && bestHeight <= h &&
                     Math.abs(aspect - (float)w/h) < 0.2 ) {
                    bestWidth = w;
                    bestHeight = h;
                }
            }
            Timber.i("best size: "+bestWidth+"x"+bestHeight);
            if( bestWidth == 0 || bestHeight == 0 ||
                mPreviewSize.getWidth() == bestWidth &&
                mPreviewSize.getHeight() == bestHeight )
                return false;
            else {
                mPreviewSize = new Size(bestWidth, bestHeight);
                return true;
            }
        } catch (CameraAccessException e) {
            Timber.e("cacPreviewSize - Camera Access Exception");
        } catch (IllegalArgumentException e) {
            Timber.e("cacPreviewSize - Illegal Argument Exception");
        } catch (SecurityException e) {
            Timber.e("cacPreviewSize - Security Exception");
        }
        return false;
    }

    @Override
    protected void openCamera(int id) {
        Timber.i("openCamera");
        CameraManager manager = (CameraManager) mView.getContext().getSystemService(Context.CAMERA_SERVICE);
        try {
            String camList[] = manager.getCameraIdList();
            if(camList.length == 0) {
                Timber.e("Error: camera isn't detected.");
                return;
            }
            if(id == CameraBridgeViewBase.CAMERA_ID_ANY) {
                mCameraID = camList[0];
            } else {
                for (String cameraID : camList) {
                    CameraCharacteristics characteristics = manager.getCameraCharacteristics(cameraID);
                    if( id == CameraBridgeViewBase.CAMERA_ID_BACK &&
                        characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_BACK ||
                        id == CameraBridgeViewBase.CAMERA_ID_FRONT &&
                        characteristics.get(CameraCharacteristics.LENS_FACING) == CameraCharacteristics.LENS_FACING_FRONT) {
                        mCameraID = cameraID;
                        break;
                    }
                }
            }
            if(mCameraID != null) {
                if (!mCameraOpenCloseLock.tryAcquire(2500, TimeUnit.MILLISECONDS)) {
                    throw new RuntimeException(
                            "Time out waiting to lock camera opening.");
                }
                Timber.i("Opening camera: " + mCameraID);
                manager.openCamera(mCameraID, mStateCallback, mBackgroundHandler);
            }
        } catch (CameraAccessException e) {
            Timber.e("OpenCamera - Camera Access Exception");
        } catch (IllegalArgumentException e) {
            Timber.e("OpenCamera - Illegal Argument Exception");
        } catch (SecurityException e) {
            Timber.e("OpenCamera - Security Exception");
        } catch (InterruptedException e) {
            Timber.e("OpenCamera - Interrupted Exception");
        }
    }

    @Override
    protected void closeCamera() {
        Timber.i("closeCamera");
        try {
            mCameraOpenCloseLock.acquire();
            if (null != mCaptureSession) {
                mCaptureSession.close();
                mCaptureSession = null;
            }
            if (null != mCameraDevice) {
                mCameraDevice.close();
                mCameraDevice = null;
            }
        } catch (InterruptedException e) {
            throw new RuntimeException("Interrupted while trying to lock camera closing.", e);
        } finally {
            mCameraOpenCloseLock.release();
        }
    }

    private final CameraDevice.StateCallback mStateCallback = new CameraDevice.StateCallback() {

        @Override
        public void onOpened(CameraDevice cameraDevice) {
            mCameraDevice = cameraDevice;
            mCameraOpenCloseLock.release();
            createCameraPreviewSession();
        }

        @Override
        public void onDisconnected(CameraDevice cameraDevice) {
            cameraDevice.close();
            mCameraDevice = null;
            mCameraOpenCloseLock.release();
        }

        @Override
        public void onError(CameraDevice cameraDevice, int error) {
            cameraDevice.close();
            mCameraDevice = null;
            mCameraOpenCloseLock.release();
        }

    };

    private void createCameraPreviewSession() {
        int w=mPreviewSize.getWidth(), h=mPreviewSize.getHeight();
        Timber.i("createCameraPreviewSession("+w+"x"+h+")");
        if(w<0 || h<0)
            return;
        try {
            mCameraOpenCloseLock.acquire();
            if (null == mCameraDevice) {
                mCameraOpenCloseLock.release();
                Timber.e("createCameraPreviewSession: camera isn't opened");
                return;
            }
            if (null != mCaptureSession) {
                mCameraOpenCloseLock.release();
                Timber.e("createCameraPreviewSession: mCaptureSession is already started");
                return;
            }
            if(null == mSTexture) {
                mCameraOpenCloseLock.release();
                Timber.e("createCameraPreviewSession: preview SurfaceTexture is null");
                return;
            }
            mSTexture.setDefaultBufferSize(w, h);

            Surface surface = new Surface(mSTexture);

            mPreviewRequestBuilder = mCameraDevice
                    .createCaptureRequest(CameraDevice.TEMPLATE_PREVIEW);
            mPreviewRequestBuilder.addTarget(surface);

            mCameraDevice.createCaptureSession(Arrays.asList(surface),
                    new CameraCaptureSession.StateCallback() {
                        @Override
                        public void onConfigured( CameraCaptureSession cameraCaptureSession) {
                            mCaptureSession = cameraCaptureSession;
                            try {
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AF_MODE, CaptureRequest.CONTROL_AF_MODE_CONTINUOUS_PICTURE);
                                mPreviewRequestBuilder.set(CaptureRequest.CONTROL_AE_MODE, CaptureRequest.CONTROL_AE_MODE_ON_AUTO_FLASH);

                                mCaptureSession.setRepeatingRequest(mPreviewRequestBuilder.build(), null, mBackgroundHandler);
                                Timber.i("CameraPreviewSession has been started");
                            } catch (CameraAccessException e) {
                                Timber.e("createCaptureSession failed");
                            }
                            mCameraOpenCloseLock.release();
                        }

                        @Override
                        public void onConfigureFailed(
                                CameraCaptureSession cameraCaptureSession) {
                            Timber.e("createCameraPreviewSession failed");
                            mCameraOpenCloseLock.release();
                        }
                    }, mBackgroundHandler);
        } catch (CameraAccessException e) {
            Timber.e("createCameraPreviewSession");
        } catch (InterruptedException e) {
            throw new RuntimeException(
                    "Interrupted while createCameraPreviewSession", e);
        }
        finally {
            //mCameraOpenCloseLock.release();
        }
    }

    private void startBackgroundThread() {
        Timber.i("startBackgroundThread");
        stopBackgroundThread();
        mBackgroundThread = new HandlerThread("CameraBackground");
        mBackgroundThread.start();
        mBackgroundHandler = new Handler(mBackgroundThread.getLooper());
    }

    private void stopBackgroundThread() {
        Timber.i("stopBackgroundThread");
        if(mBackgroundThread == null)
            return;
        mBackgroundThread.quitSafely();
        try {
            mBackgroundThread.join();
            mBackgroundThread = null;
            mBackgroundHandler = null;
        } catch (InterruptedException e) {
            Timber.e("stopBackgroundThread");
        }
    }

    @Override
    protected void setCameraPreviewSize(int width, int height) {
        Timber.i("setCameraPreviewSize("+width+"x"+height+")");
        if(mMaxCameraWidth  > 0 && mMaxCameraWidth  < width)  width  = mMaxCameraWidth;
        if(mMaxCameraHeight > 0 && mMaxCameraHeight < height) height = mMaxCameraHeight;
        try {
            mCameraOpenCloseLock.acquire();

            boolean needReconfig = cacPreviewSize(width, height);
            mCameraWidth  = mPreviewSize.getWidth();
            mCameraHeight = mPreviewSize.getHeight();

            if( !needReconfig ) {
                mCameraOpenCloseLock.release();
                return;
            }
            if (null != mCaptureSession) {
                Timber.d("closing existing previewSession");
                mCaptureSession.close();
                mCaptureSession = null;
            }
            mCameraOpenCloseLock.release();
            createCameraPreviewSession();
        } catch (InterruptedException e) {
            mCameraOpenCloseLock.release();
            throw new RuntimeException("Interrupted while setCameraPreviewSize.", e);
        }
    }
}
