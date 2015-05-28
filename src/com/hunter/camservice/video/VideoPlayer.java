package com.hunter.camservice.video;

import android.app.Activity;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.VideoView;

import java.util.Timer;
import java.util.TimerTask;

import com.hunter.camservice.R;

/**
 * Activity class for video player.
 */
public class VideoPlayer extends Activity {

    /**
     * Timer for moving slider.
     */
    private Timer sliderMove;

    /**
     * Play button.
     */
    private ImageButton playPauseButton;

    /**
     * Video screen.
     */
    private VideoView videoView;

    /**
     * Slider for moving video.
     */
    private SeekBar slider;

    /**
     * Inicialization method.
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_video_player);

        playPauseButton = (ImageButton) findViewById(R.id.playPauseButton);
        videoView = (VideoView) findViewById(R.id.videoView);
        slider = (SeekBar) findViewById(R.id.slider);

        String videoFilePath = getIntent().getStringExtra("videoFilePath");

        videoView.setVideoPath(videoFilePath);
        videoView.setOnCompletionListener(onFinishPlaying);

        slider.setOnSeekBarChangeListener(onSliderChange);
    }

    /**
     * Listener for user change of slider.
     */
    private SeekBar.OnSeekBarChangeListener onSliderChange = new SeekBar.OnSeekBarChangeListener() {

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            if (fromUser) {
                videoView.seekTo(seekBar.getProgress());
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    };

    /**
     * Timer class for moving slider.
     */
    private class SliderTask extends TimerTask {
        public void run() {

            slider.setProgress(videoView.getCurrentPosition());
        }
    }

    /**
     * Listener for end of playing video.
     */
    private MediaPlayer.OnCompletionListener onFinishPlaying = new MediaPlayer.OnCompletionListener() {

        public void onCompletion(MediaPlayer mp) {

            sliderMove.cancel();
            sliderMove.purge();
            playPauseButton.setImageResource(android.R.drawable.ic_media_play);
            slider.setProgress(slider.getMax());
        }
    };

    /**
     * Method for button play/pause.
     * @param v
     */
    public void onClickPlayStop(View v) {

        if (videoView.isPlaying()) {

            sliderMove.cancel();
            sliderMove.purge();
            videoView.pause();
            playPauseButton.setImageResource(android.R.drawable.ic_media_play);
        } else {

            slider.setMax(videoView.getDuration());
            sliderMove = new Timer();
            sliderMove.schedule(new SliderTask(), 0, 100);
            videoView.start();
            playPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        }
    }
}
