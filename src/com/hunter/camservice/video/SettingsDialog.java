package com.hunter.camservice.video;

import java.util.ArrayList;

import android.app.Activity;
import android.app.Dialog;
import android.hardware.Camera;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import com.hunter.camservice.R;
import com.hunter.camservice.common.Utilities;

/**
 * Setting dialog class.
 */
public class SettingsDialog extends Dialog {

    /**
     * Main activity.
     */
    private Activity activity;

    /**
     * Spinner for effects.
     */
    private Spinner effectSpinner;

    /**
     * Spinner for resolutions.
     */
    private Spinner resolutionSpinner;

    /**
     * List of available effects.
     */
    private ArrayList<String> effects;

    /**
     * List of available resolution ids.
     */
    private ArrayList<Integer> resolutions;

    /**
     * List of available resolutions.
     */
    private ArrayList<String> resolutionNames;

    /**
     * Actually selected resolution.
     */
    private int selectedResolution;

    /**
     * Actually selected effect
     */
    private String selectedEffect;

    /**
     * Actually selected effect number.
     */
    private int selectedEfectNumber;

    /**
     * Is recording parameter.
     */
    private boolean isRecording;

    /**
     * Constructor.
     * @param activity Main activity.
     * @param camera Camera instance.
     * @param cameraId Camera id.
     * @param selectedResolution Actually selected resolution.
     * @param selectedEfect Actually selected effect.
     * @param isRecording Is recording parameter.
     */
    public SettingsDialog(Activity activity, Camera camera, int cameraId, int selectedResolution, String selectedEfect, boolean isRecording) {
        super(activity);

        this.activity = activity;
        this.effects = (ArrayList<String>) camera.getParameters().getSupportedColorEffects();
        this.resolutions = Utilities.getSupportedProfiles(cameraId);
        this.resolutionNames = Utilities.getSupportedResolutions(cameraId);
        this.selectedResolution = selectedResolution;
        this.selectedEffect = selectedEfect;
        this.selectedEfectNumber = Utilities.getSelectedEffect(camera, selectedEfect);
        this.isRecording = isRecording;
    }

    /**
     * Initialization method.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_settings_dialog);

        resolutionSpinner = (Spinner) findViewById(R.id.resolution_spinner);
        effectSpinner = (Spinner) findViewById(R.id.effect_spinner);

        ArrayAdapter<String> resolutionAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_dropdown_item, resolutionNames);
        resolutionSpinner.setAdapter(resolutionAdapter);

        if (isRecording) {
            resolutionSpinner.setEnabled(false);
        }

        ArrayAdapter<String> effectAdapter = new ArrayAdapter<String>(activity, android.R.layout.simple_spinner_dropdown_item, effects);
        effectSpinner.setAdapter(effectAdapter);

        effectSpinner.setSelection(selectedEfectNumber);
        resolutionSpinner.setSelection(selectedResolution);

        effectSpinner.setOnItemSelectedListener(onEfectSelected);
    }

    /**
     * Listener for select effect.
     */
    AdapterView.OnItemSelectedListener onEfectSelected = new AdapterView.OnItemSelectedListener() {

        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            selectedEffect = effects.get(position);
        }

        public void onNothingSelected(AdapterView<?> parent) {
        }
    };

}

