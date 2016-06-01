package com.example.cameratest.camera;


import android.graphics.Point;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

/**
 * Created by yp-tc-m-2781 on 16/5/23.
 */
public class CameraControllerImpl implements CameraController {

    private Camera mCamera;
    private Point cameraResolution;


    private final String tag = "log_cameratest";

    private CameraControllerImpl() {

    }

    public CameraControllerImpl(Camera mCamera,CreateListenr createListenr) {
        this.mCamera = mCamera;
        createListenr.onCreateFinish();
    }

    @Override
    public void startPreview() {
        mCamera.startPreview();
    }

    @Override
    public void stopPreview() {
        mCamera.stopPreview();
    }

    @Override
    public void setPreviewDisplay(SurfaceHolder surfaceHolder) throws IOException {
        mCamera.setPreviewDisplay(surfaceHolder);
    }

    @Override
    public void release() {
        mCamera.release();
    }

    @Override
    public void autoFocus(Camera.AutoFocusCallback autoFocusCallback) {
        mCamera.autoFocus(autoFocusCallback);
    }

    @Override
    public void takePicture(Camera.ShutterCallback shutter, Camera.PictureCallback raw, Camera.PictureCallback jpeg) {
        mCamera.takePicture(shutter,raw,jpeg);
    }

    @Override
    public void cancelFocus() {
        mCamera.cancelAutoFocus();
    }

    @Override
    public Camera getCamera() {
        return mCamera;
    }

    @Override
    public void setParameters(Camera.Parameters parameters) {
        mCamera.setParameters(parameters);
    }

    @Override
    public void setPreviewSize(int width, int height) {
        cameraResolution = new Point();
        cameraResolution.x = width;
        cameraResolution.y = height;
        Camera.Parameters parameters = mCamera.getParameters();
        parameters.setPreviewSize(width,height);
        mCamera.setParameters(parameters);
    }

    @Override
    public List<Camera.Size> getSupportedPreviewSize() {
        return mCamera.getParameters().getSupportedPreviewSizes();
    }

    private CreateListenr mCreateListenr;

    public void setCreateListenr(CreateListenr createListenr)
    {
        mCreateListenr = createListenr;
    }

    @Override
    public void setOneShotPreviewCallback(Camera.PreviewCallback previewCallback) {
        mCamera.setOneShotPreviewCallback(previewCallback);
    }

    @Override
    public Point getCameraResolution() {
        if(cameraResolution == null)
        {
            cameraResolution = new Point();
            Camera.Size size = mCamera.getParameters().getPictureSize();
            cameraResolution.x = size.width;
            cameraResolution.y = size.height;
        }
        return cameraResolution;
    }

}
