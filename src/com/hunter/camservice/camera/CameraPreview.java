package com.hunter.camservice.camera;

import java.io.IOException;

import android.content.Context;
import android.hardware.Camera;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

public class CameraPreview extends SurfaceView implements SurfaceHolder.Callback {
    private static final String TAG = "CameraPreview";
    private Camera camera;

    /**
     * Constructor.
     * @param context Application context.
     * @param camera Camera instance.
     */
    public CameraPreview(Context context, Camera camera) {
        super(context);

        this.camera = camera;
        getHolder().addCallback(this);
    }

    /**
     * On surface created method.
     * @param holder Holder.
     */
    public void surfaceCreated(SurfaceHolder holder) {
	    try {
	        camera.setDisplayOrientation(90);
            camera.setPreviewDisplay(holder);
            camera.startPreview();
        } catch (IOException e) {
            Log.d(TAG, "Error setting camera preview: " + e.getMessage());
        }
    }

    /**
     * When surface changed method
     * @param holder Holder.
     * @param format Format.
     * @param width Width.
     * @param height Height.
     */
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
    }

    /**
     * When surface is destroyed method.
     * @param holder Holder
     */
    public void surfaceDestroyed(SurfaceHolder holder) {
    	getHolder().removeCallback(this);
    }
}
