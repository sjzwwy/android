package com.example.cameratest.camera;

import android.graphics.Point;
import android.hardware.Camera;
import android.view.SurfaceHolder;

import java.io.IOException;
import java.util.List;

/**
 * Created by yp-tc-m-2781 on 16/5/23.
 */
public interface CameraController {

    void startPreview();

    void stopPreview();

    void setPreviewDisplay(SurfaceHolder surfaceHolder) throws IOException;

    void release();

    void autoFocus(Camera.AutoFocusCallback autoFocusCallback);

    void takePicture(Camera.ShutterCallback shutter, Camera.PictureCallback raw,
                     Camera.PictureCallback jpeg);

    void cancelFocus();

    Camera getCamera();

    void setParameters(Camera.Parameters parameters);

    void setPreviewSize(int width, int height);

    List<Camera.Size> getSupportedPreviewSize();

    void setCreateListenr(CameraControllerImpl.CreateListenr createListenr);

    void setOneShotPreviewCallback(Camera.PreviewCallback previewCallback);

    Point getCameraResolution();

    interface CreateListenr{
        void onCreateFinish();
    }



}
