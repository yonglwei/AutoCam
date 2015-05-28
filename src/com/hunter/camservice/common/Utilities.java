package com.hunter.camservice.common;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.os.Environment;
import android.util.Log;

import com.hunter.camservice.R;

public class Utilities {

    /**
     * Tag of this class.
     */
    private static final String TAG = "Utilities";

    /**
     * List of supported profiles.
     */
    private static final int[] profiles = {
            CamcorderProfile.QUALITY_1080P,
            CamcorderProfile.QUALITY_720P,
            CamcorderProfile.QUALITY_480P,
            CamcorderProfile.QUALITY_CIF,
            CamcorderProfile.QUALITY_QCIF
    };

    /**
     * List of supported resolutions.
     */
    private static final String[] resolutions = {
            "1920 x 1080",
            "1280 x 720",
            "720 x 480",
            "352 x 288",
            "176 x 144"
    };

    /**
     * Getting application directory or creating it.
     * @param context Application context.
     * @return Application directory.
     */
    public static File getAppDir(Context context) {
        File appDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES),
                context.getString(R.string.app_dir));

        if (!appDir.exists()) {

            if (!appDir.mkdirs()) {
                Log.e(TAG, "Application folder cannot be created.");
                return null;
            }
        }

        return appDir;
    }

    /**
     * Returns application directory path.
     * @param context Application context.
     * @return Directory path.
     */
    public static String getAppDirPath(Context context) {
        return getAppDir(context).getPath();
    }


    /**
     * Returns list of supported resolutions of device.
     * @param cameraId Camera ID.
     * @return List of supported resolutions.
     */
    public static ArrayList<String> getSupportedResolutions(int cameraId) {

        ArrayList<String> list = new ArrayList<String>();

        for (int i = 0; i < profiles.length; i++) {
            if (CamcorderProfile.hasProfile(cameraId, profiles[i])) {
                list.add(resolutions[i]);
            }
        }

        return list;
    }

    /**
     * Returns list of supported profiles of device.
     * @param cameraId Camera ID.
     * @return List of supported profiles.
     */
    public static ArrayList<Integer> getSupportedProfiles(int cameraId) {

        ArrayList<Integer> list = new ArrayList<Integer>();

        for (int i = 0; i < profiles.length; i++) {
            if (CamcorderProfile.hasProfile(cameraId, profiles[i])) {
                list.add(profiles[i]);
            }
        }

        return list;
    }

    /**
     * Returns list of supported camera effects.
     * @param camera Camera instance.
     * @return List of supported camera effects.
     */
    public static List<String> getSuportedCameraEffects(Camera camera) {
        return camera.getParameters().getSupportedColorEffects();
    }

    /**
     * Get selected efect ID.
     * @param camera Camera instance.
     * @param name Name of effect.
     * @return Id of effect.
     */
    public static int getSelectedEffect(Camera camera, String name) {

        List<String> list = getSuportedCameraEffects(camera);

        for (int i = 0; i < list.size(); i++) {

            if (list.get(i).equals(name)) {
                return i;
            }
        }

        return -1;
    }

    /**
     * Get selected id of resolution.
     * @param resolutionValue Number of selected id from list of resolution.
     * @return Id of resolution.
     */
    public static int getSelectedResolution(int resolutionValue) {
        ArrayList<Integer> list = getSupportedProfiles(Camera.CameraInfo.CAMERA_FACING_BACK);
        for (int i = 0; i < list.size(); i++) {
            if (resolutionValue == list.get(i)) {
                return i;
            }
        }
        return -1;
    }
}

