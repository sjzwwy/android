package com.example.cameratest.camera;

import android.app.Activity;
import android.content.Context;
import android.graphics.*;
import android.hardware.Camera;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.Toast;
import com.google.zxing.*;
import com.google.zxing.common.HybridBinarizer;

import java.io.IOException;

/**
 * Created by yp-tc-m-2781 on 16/5/20.
 */
public class MySurfaceView extends SurfaceView implements SurfaceHolder.Callback {

    private CameraController mCameraController;
    private SurfaceHolder mSurfaceHolder;
    private final static String tag = "log_cameratest";
    private Context context;
    private float touch_x;
    private float touch_y;
    private Paint paint;
    private DisplayMetrics dm = new DisplayMetrics();

    private final int FOCUS_DONE = 0;
    private final int FOCUS_WAITING = 1;
    private final int FOCUS_SUCCESS = 2;
    private final int FOCUS_FAIL = 3;

    private int focus_status = FOCUS_DONE;

    public MySurfaceView(Context context, CameraController cameraController) {
        super(context);
        this.context = context;
        ((Activity)context).getWindowManager().getDefaultDisplay().getMetrics(dm);
        mCameraController = cameraController;
        mSurfaceHolder = getHolder();
        mSurfaceHolder.addCallback(this);
        setCameraDisplayOrientation((Activity) context, 0, mCameraController.getCamera());

        Camera.Size csize = mCameraController.getCamera().getParameters().getPreviewSize();
        Log.d(tag, "previewSize : width : " + csize.width + " height : " + csize.height);

        Point point = new Point();
        ((Activity) context).getWindowManager().getDefaultDisplay().getSize(point);
        point = CameraConfigurationUtils.findBestPreviewSizeValue(mCameraController.getCamera().getParameters(),
                point);

        mCameraController.setPreviewSize(point.x, point.y);

        Camera.Size nsize = mCameraController.getCamera().getParameters().getPreviewSize();
        Log.d(tag, "previewSize after set : width : " + nsize.width + " height : " + nsize.height);

        paint = new Paint();
        paint.setStyle(Paint.Style.STROKE);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        try {
            Log.d(tag, "surfaceCreated thread :" + Thread.currentThread().getName() + " isActived : " + isActivated());
            mCameraController.setPreviewDisplay(holder);
            mCameraController.startPreview();
            postDelayed(new Runnable() {
                @Override
                public void run() {
                    startScan();
                }
            }, 500);
        } catch (IOException e) {
            Log.i(tag, "Error setting camera preview: " + e.getMessage());
        }

    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
        Log.d(tag, "surfaceChanged thread : " + Thread.currentThread().getName() + " isActived : " + isActivated());
        if (mSurfaceHolder.getSurface() == null) {
            // preview surface does not exist
            Log.i(tag, "mHolder.getSurface() == null");
            return;
        }
        try {
            mCameraController.stopPreview();

        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
            Log.i(tag, "Error stopping camera preview: " + e.getMessage());
        }

        try {
            mCameraController.setPreviewDisplay(mSurfaceHolder);
            mCameraController.startPreview();

        } catch (Exception e) {
            // ignore: tried to stop a non-existent preview
            Log.i(tag, "Error starting camera preview: " + e.getMessage());
        }
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.i(tag, "surfaceDestroyed");
        mCameraController.stopPreview();
        mCameraController.release();
    }

    private Camera.AutoFocusCallback autoFocusCallback = new Camera.AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.i(tag, "onAutoFocus status : " + success);
            focus_status = success ? FOCUS_SUCCESS : FOCUS_FAIL;
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_UP) {
            Log.i(tag, "onTouchEvent : ACTION_UP");
//            focusOnTouch(event);
        }
        return true;
    }

    private void tryAutoFocus(Camera.AutoFocusCallback autoFocusCallback) {
        mCameraController.autoFocus(autoFocusCallback);
    }

    private void cancelAutoFocus() {
        mCameraController.cancelFocus();
    }

    public static void setCameraDisplayOrientation(Activity activity,
                                                   int cameraId, Camera camera) {
        Camera.CameraInfo info =
                new Camera.CameraInfo();
        Camera.getCameraInfo(cameraId, info);
        int rotation = activity.getWindowManager().getDefaultDisplay()
                .getRotation();
        int degrees = 0;
        switch (rotation) {
            case Surface.ROTATION_0:
                degrees = 0;
                break;
            case Surface.ROTATION_90:
                degrees = 90;
                break;
            case Surface.ROTATION_180:
                degrees = 180;
                break;
            case Surface.ROTATION_270:
                degrees = 270;
                break;
        }

        int result;
        if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
            result = (info.orientation + degrees) % 360;
            result = (360 - result) % 360;  // compensate the mirror
        } else {  // back-facing
            result = (info.orientation - degrees + 360) % 360;
        }

        Log.i(tag, "setDisplayOrientation result : " + result);
        camera.setDisplayOrientation(result);
    }


    private void startScan() {
        tryAutoFocus(new Camera.AutoFocusCallback() {
            @Override
            public void onAutoFocus(boolean b, Camera camera) {
                mCameraController.setOneShotPreviewCallback(new Camera.PreviewCallback() {
                    @Override
                    public void onPreviewFrame(byte[] bytes, Camera camera) {
                        Log.d(tag, "onPreviewFrame" + bytes.length);
                        Point point = mCameraController.getCameraResolution();
                        decode(bytes,point.x,point.y);
                    }
                });
            }
        });
    }

    Result rawResult = null;
    private void decode(byte[] data, int width, int height) {
        MultiFormatReader multiFormatReader = new MultiFormatReader();

        Rect rect = getFramingRectInPreview();
        PlanarYUVLuminanceSource source = new PlanarYUVLuminanceSource(data, width, height, rect.left, rect.top,
                rect.width(), rect.height(), false);
        if (source != null) {
            BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
            try {
                rawResult = multiFormatReader.decodeWithState(bitmap);
            } catch (ReaderException re) {
            } finally {
                multiFormatReader.reset();
            }

            if (rawResult != null) {
                Log.d(tag, "解析完毕" + rawResult.getBarcodeFormat().toString());
                post(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(context,"解析成功"+rawResult.getText(),Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                startScan();
            }
        } else {
            startScan();
        }
    }

    private Rect getFramingRectInPreview() {

        int left = (int)(getY()+(getHeight()-180*dm.density)/2);
        int top = (int)(getX()+(getWidth()-180*dm.density)/2);
        final Rect rect = new Rect(left,top,left+(int)(180*dm.density),top+(int)(180*dm.density));
        return rect;
    }

}
