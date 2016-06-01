package com.example.cameratest.camera;


import android.hardware.Camera;

/**
 * Created by yp-tc-m-2781 on 16/5/20.
 */
public class CameraManager {
    private static final String tag = "log_cameratest";

    public static Camera getCamera() {
        Camera c = null;
        try {
            c = Camera.open(0); // attempt to get a Camera instance
        } catch (Exception e) {
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }
}
