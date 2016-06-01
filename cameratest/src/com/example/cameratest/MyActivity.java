package com.example.cameratest;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;
import com.example.cameratest.camera.*;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.PlanarYUVLuminanceSource;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.Hashtable;

public class MyActivity extends Activity implements View.OnClickListener, CameraController.CreateListenr {
    /**
     * Called when the activity is first created.
     */
    final String tag = "log_cameratest";
    private LinearLayout parent_preview;
    private CameraController mCameraController;
    MySurfaceView mySurfaceView;
    ImageButton btn_capture;
    View scan_view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        parent_preview = (LinearLayout) findViewById(R.id.parent_preview);
        btn_capture = (ImageButton) findViewById(R.id.btn_capture);
        scan_view = findViewById(R.id.scan_view);
        btn_capture.setOnClickListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        getWindow().getDecorView().postDelayed(new Runnable() {
            @Override
            public void run() {
                showCamera();
            }
        }, 100);
    }

    private void showCamera() {
        mCameraController = new CameraControllerImpl(CameraManager.getCamera(), MyActivity.this);
        mySurfaceView = new MySurfaceView(MyActivity.this, mCameraController);
        LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT
                , ViewGroup.LayoutParams.MATCH_PARENT);
        parent_preview.addView(mySurfaceView, layoutParams);

    }

    @Override
    public void onClick(View v) {

        Toast.makeText(this, "getX : " + scan_view.getX() + " getY : " + scan_view.getY()
                        + "\n" + " getLeft : " + scan_view.getLeft() + " getTop : " + scan_view.getTop()
                , Toast.LENGTH_LONG).show();
        Log.d(tag, "getX : " + scan_view.getX() + " getY : " + scan_view.getY()
                + "\n" + " getLeft : " + scan_view.getLeft() + " getTop : " + scan_view.getTop());
        Rect rect = new Rect();
        scan_view.getDrawingRect(rect);
        Log.d(tag,"rect left : "+rect.left+" top : "+rect.top
        +" right : "+rect.right+" bottom : "+rect.bottom);
//        mCameraController.takePicture(new Camera.ShutterCallback() {
//            @Override
//            public void onShutter() {
//                Log.d(tag, "onShutter");
//            }
//        }, new Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(byte[] data, Camera camera) {
//                if (data != null)
//                    Log.d(tag, "data 1 : " + data.length);
//                else
//                    Log.d(tag, "data 1 is null");
//                if (camera == null) Log.d(tag, "camera 1 is null");
//                else
//                    Log.d(tag, "camera 1 is not null");
//            }
//        }, new Camera.PictureCallback() {
//            @Override
//            public void onPictureTaken(byte[] data, Camera camera) {
//                if (data != null)
//                    toFile(data);
//                if (camera == null) Log.d(tag, "camera 1 is null");
//                else {
//                    Log.d(tag, "camera 2 is not null");
//                    camera.startPreview();
//                }
//            }
//        });
//        mCameraController.autoFocus(new Camera.AutoFocusCallback() {
//            @Override
//            public void onAutoFocus(boolean success, Camera camera) {
//                if (success) {
//
//                }
//            }
//        });
    }

    @Override
    public void onCreateFinish() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void toFile(byte[] data) {
        Log.d(tag, "thread name :" + Thread.currentThread().getName());
        FileOutputStream fos = null;
        try {
            fos = new FileOutputStream("/sdcard/yeepay/camera.jpeg");
            fos.write(data);
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private Bitmap createQRImage(String content, int qr_width) {
        try {
            // 判断URL合法性
            if (content == null || "".equals(content) || content.length() < 1) {
                return null;
            }
            Hashtable<EncodeHintType, String> hints = new Hashtable<EncodeHintType, String>();
            hints.put(EncodeHintType.CHARACTER_SET, "utf-8");
            // 图像数据转换，使用了矩阵转换
            BitMatrix bitMatrix = new QRCodeWriter().encode(content,
                    BarcodeFormat.QR_CODE, qr_width, qr_width, hints);


            int[] pixels = new int[qr_width * qr_width];
            // 下面这里按照二维码的算法，逐个生成二维码的图片，
            // 两个for循环是图片横列扫描的结果
            for (int y = 0; y < qr_width; y++) {
                for (int x = 0; x < qr_width; x++) {
                    if (bitMatrix.get(x, y)) {


                        pixels[y * qr_width + x] = 0xff000000;
                    } else {
                        pixels[y * qr_width + x] = 0xffffffff;
                    }
                }
            }
            // 生成二维码图片的格式，使用ARGB_8888
            Bitmap bitmap = Bitmap.createBitmap(qr_width, qr_width,
                    Bitmap.Config.ARGB_8888);
            bitmap.setPixels(pixels, 0, qr_width, 0, 0, qr_width, qr_width);
            // 显示到一个ImageView上面
            return bitmap;
        } catch (WriterException e) {
            e.printStackTrace();
            return null;
        }
    }

}
