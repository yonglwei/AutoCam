package com.hunter.camservice;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.SurfaceView;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.Toast;

import com.hunter.camservice.camera.CameraPreview;
import com.hunter.camservice.common.Utilities;
import com.hunter.camservice.video.VideoListActivity;

public class MainActivity extends Activity {
	/**
    * Tag of class.
    */
   private static final String TAG = "MainActivity";

   /**
    * Is recording parameter.
    */
   public boolean isRecording = false;

   /**
    * Actually selected resolution.
    */
   private Integer resolutionValue;

   /**
    * Actually selected effect.
    */
   private Integer selectedEffect;

   /**
    * Recording camera.
    */
   private Camera mCamera;

   /**
    * Preview of camera.
    */
   private SurfaceView mPreview;

   /**
    * Media recorder.
    */
   private MediaRecorder mMediaRecorder;

   /**
    * Button for capturing video.
    */
   private ImageButton captureButton;

   /**
    * Preview layout.
    */
   private FrameLayout preview;

   /**
    * Slider for zooming video.
    */
   private SeekBar zoomSlider;

   /**
    * Scale detector.
    */
   private ScaleGestureDetector mScaleDetector;

   /**
    * Initialization method.
    * @param savedInstanceState
    */
   
   protected void onCreate(Bundle savedInstanceState) {
       super.onCreate(savedInstanceState);
       setContentView(R.layout.activity_main);

       captureButton = (ImageButton) findViewById(R.id.button_capture);
       preview = (FrameLayout) findViewById(R.id.camera_preview);
       zoomSlider = (SeekBar) findViewById(R.id.zoomSlider);

       zoomSlider.setOnSeekBarChangeListener(onZoomSliderChange);

       resolutionValue = Utilities.getSupportedProfiles(Camera.CameraInfo.CAMERA_FACING_BACK).get(0);
   }

   /**
    * When application is resume method.
    */
   
   protected void onResume() {
       super.onResume();

       mScaleDetector  = new ScaleGestureDetector(this, scaleListener);
       mCamera = getCameraInstance();
       mPreview = new CameraPreview(this, mCamera);
       preview.addView(mPreview);

       if (mCamera.getParameters().isZoomSupported()) {
           zoomSlider.setMax(mCamera.getParameters().getMaxZoom());
           zoomSlider.setProgress(mCamera.getParameters().getZoom());
       }
   }

   /**
    * When application is paused method.
    */
   
   protected void onPause() {
       super.onPause();
       if (isRecording) {
           mMediaRecorder.stop();
           isRecording = false;
       }

       captureButton.setImageResource(android.R.drawable.ic_menu_camera);
       releaseMediaRecorder();
       releaseCamera();
   }

   /**
    * Listener for change of zoom slider.
    */
   private SeekBar.OnSeekBarChangeListener onZoomSliderChange = new SeekBar.OnSeekBarChangeListener() {

       public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
           Camera.Parameters p = mCamera.getParameters();

           if (p.isZoomSupported()) {

               p.setZoom(progress);
               p.setFocusMode(p.FOCUS_MODE_CONTINUOUS_VIDEO);
               mCamera.setParameters(p);
           }
       }

       
       public void onStartTrackingTouch(SeekBar seekBar) {

       }

       
       public void onStopTrackingTouch(SeekBar seekBar) {

       }
   };

   /**
    * Returns camera instance.
    * @return Camera instance.
    */
   public static Camera getCameraInstance() {

       Camera camera = null;

       try {
           camera = Camera.open();
       } catch (Exception e) {
           Log.d("MainActivity", "Camera is not available: " + e.getMessage());
       }

       return camera;
   }

   /**
    * Prepares application for video recording
    * @return True if everything is prepared.
    */
   private boolean prepareVideoRecorder() {
       mCamera.stopPreview();

       //mCamera = getCameraInstance();
       mMediaRecorder = new MediaRecorder();

       // Step 1: Unlock and set camera to MediaRecorder
       mCamera.unlock();
       mMediaRecorder.setCamera(mCamera);

       // Step 2: Set sources
       mMediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
       mMediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

       // Step 3: Set a CamcorderProfile (requires API Level 8 or higher)
       mMediaRecorder.setProfile(
    		   CamcorderProfile.get(Camera.CameraInfo.CAMERA_FACING_BACK, 
    		   resolutionValue));

       // Step 4: Set output file
       mMediaRecorder.setOutputFile(getOutputMediaFile().toString());

       // Step 5: Set the preview output
       mMediaRecorder.setPreviewDisplay(mPreview.getHolder().getSurface());

       // Step 6: Prepare configured MediaRecorder
       try {
           mMediaRecorder.prepare();
       } catch (IllegalStateException e) {
           Log.d(TAG, "IllegalStateException preparing MediaRecorder: " + e.getMessage());
           releaseMediaRecorder();
           return false;
       } catch (IOException e) {
           Log.d(TAG, "IOException preparing MediaRecorder: " + e.getMessage());
           releaseMediaRecorder();
           return false;
       }
       return true;
   }

   /**
    * When button for recording or stop recording is clicked.
    * @param v
    */
   public void onClick(View v) {
       captureButton.setEnabled(false);
       if (isRecording) {
           mMediaRecorder.stop();
           releaseMediaRecorder();
           mCamera.lock();
           captureButton.setImageResource(android.R.drawable.ic_menu_camera);
           isRecording = false;
       } else {
           if (prepareVideoRecorder()) {
               mMediaRecorder.start();
               captureButton.setImageResource(android.R.drawable.ic_menu_save);
               isRecording = true;
           } else {
               releaseMediaRecorder();
           }
       }

       try {
           Thread.sleep(1000); // video should have at least 1s
       } catch (InterruptedException e) {
           e.printStackTrace();
       }
       captureButton.setEnabled(true);
   }

   /**
    * Method for release media recorder.
    */
   private void releaseMediaRecorder() {
       if (mMediaRecorder != null) {
           mMediaRecorder.reset();
           mMediaRecorder.release();
           mMediaRecorder = null;
           mCamera.lock();
       }
   }

   /**
    * Method for release camera.
    */
   private void releaseCamera() {
       if (mCamera != null) {
           mCamera.release();
           mCamera = null;
       }
   }

   /**
    * Returns file for write video.
    * @return New video file.
    */
   private File getOutputMediaFile() {

       if (Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {

           String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
           return new File(Utilities.getAppDirPath(getApplicationContext()) + File.separator + "VIDEO_" + timeStamp + ".mp4");
       } else {

           Toast.makeText(MainActivity.this, "Not enough space or SD card is not mounted.", Toast.LENGTH_SHORT).show();
           return null;
       }
   }

   /**
    * When edit button is clicked method.
    * @param view
    */
   public void onClickEdit(View view) {
       Intent intent = new Intent(this, VideoListActivity.class);
       startActivity(intent);
   }

   /**
    * When info button is clicked method.
    * @param view
    */
   public void onInfoClick(View view) {
       final AlertDialog alertDialog = new AlertDialog.Builder(this).create();
       alertDialog.setTitle("About");
       alertDialog.setMessage("Auto Camera\n\nFrom: Hunter Studio\nLocation: Hubei Wuhan\nYear: 2015");
       alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "OK", new DialogInterface.OnClickListener() {
           public void onClick(DialogInterface dialog, int which) {
               alertDialog.cancel();
           }
       });

       alertDialog.show();
   }

   /**
    * When settings button is clicked method.
    * @param view
    */
   public void onSettingsClick(View view) {

       new ShowSetting(this).show();
   }

   /**
    * 设置对话框
    */
   private class ShowSetting extends Dialog {

       /**
        * Spinner for effects.
        */
       private Spinner effectSpinner;

       /**
        * Spinner for resolutions.
        */
       private Spinner resolutionSpinner;

       /**
        * Application context.
        */
       private Context context;

       /**
        * Constructor.
        * @param context Application context.
        */
       public ShowSetting(Context context) {
           super(context);
           this.context = context;
       }

       /**
        * Initialization method.
        * @param savedInstanceState
        */
       
       protected void onCreate(Bundle savedInstanceState) {
           super.onCreate(savedInstanceState);
           requestWindowFeature(Window.FEATURE_NO_TITLE);
           setContentView(R.layout.activity_settings_dialog);

           resolutionSpinner = (Spinner) findViewById(R.id.resolution_spinner);
           effectSpinner = (Spinner) findViewById(R.id.effect_spinner);

           ArrayAdapter<String> effectAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, mCamera.getParameters().getSupportedColorEffects());
           effectSpinner.setAdapter(effectAdapter);

           ArrayAdapter<String> resolutionAdapter = new ArrayAdapter<String>(context, android.R.layout.simple_spinner_dropdown_item, Utilities.getSupportedResolutions(Camera.CameraInfo.CAMERA_FACING_BACK));
           resolutionSpinner.setAdapter(resolutionAdapter);

           effectSpinner.setOnItemSelectedListener(onEffectSelected);
           resolutionSpinner.setOnItemSelectedListener(onResolutionSelected);

           if (isRecording) {
               resolutionSpinner.setEnabled(false);
           }

           resolutionSpinner.setSelection(Utilities.getSelectedResolution(resolutionValue));

           if(selectedEffect != null) {
               effectSpinner.setSelection(selectedEffect);
           }
       }

       /**
        * On effect selected listener.
        */
       AdapterView.OnItemSelectedListener onEffectSelected = new AdapterView.OnItemSelectedListener() {

           
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                   Camera.Parameters p = mCamera.getParameters();
                   p.setColorEffect(p.getSupportedColorEffects().get(position));
                   mCamera.setParameters(p);
                   selectedEffect = position;
           }

           
           public void onNothingSelected(AdapterView<?> parent) {

           }
       };

       /**
        * On resolution selected listener.
        */
       AdapterView.OnItemSelectedListener onResolutionSelected = new AdapterView.OnItemSelectedListener() {

           
           public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

               resolutionValue = Utilities.getSupportedProfiles(Camera.CameraInfo.CAMERA_FACING_BACK).get(position);
           }

           
           public void onNothingSelected(AdapterView<?> parent) {

           }
       };
   }

   /**
    * 通过音量键缩放图像
    */
   public boolean onKeyDown(int keyCode, KeyEvent event) {

       Camera.Parameters p = mCamera.getParameters();

       if ((keyCode == KeyEvent.KEYCODE_VOLUME_DOWN)) {
           if (p.isZoomSupported()) {
               int change = p.getZoom() - p.getMaxZoom() / 10;
               p.setZoom(change  < 0 ? 0 : change);
               mCamera.setParameters(p);
           }
       } else if (keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
           if (p.isZoomSupported()) {
               int change = p.getZoom()  + p.getMaxZoom() / 10;
               p.setZoom(change  > p.getMaxZoom() ? p.getMaxZoom() : change);
               mCamera.setParameters(p);
           }
       } else {
           super.onKeyDown(keyCode, event);
       }

       return true;
   }

   /**
    * On gesture zoom event.
    * @param event Event.
    * @return true
    */
   
   public boolean onTouchEvent(MotionEvent event) {
       mScaleDetector.onTouchEvent(event);
       return true;
   }

   /**
    * Gesture zoom scale listener.
    */
   ScaleGestureDetector.OnScaleGestureListener scaleListener = new ScaleGestureDetector.OnScaleGestureListener() {

       private int zoom;

       private float step;

       private Camera.Parameters p;

       
       public void onScaleEnd(ScaleGestureDetector detector) {
       }

       
       public boolean onScaleBegin(ScaleGestureDetector detector) {
           p = mCamera.getParameters();
           zoom = p.getZoom();
           step = p.getMaxZoom() / 10;
           return true;
       }

       /**
        * 通过手势缩放
        */
       public boolean onScale(ScaleGestureDetector detector) {

           float scale = detector.getScaleFactor();

           if (scale < 1.0f) {

               int change = (int)(zoom - p.getMaxZoom() * (1 - scale));
               p.setZoom(change < 0 ? 0 : change);
               mCamera.setParameters(p);

           } else {
               int change = (int)(zoom + step * scale);
               p.setZoom(change > p.getMaxZoom() ? p.getMaxZoom() : change);
               mCamera.setParameters(p);
           }

           return false;
       }
   };

}
