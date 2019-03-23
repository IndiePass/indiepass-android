package com.indieweb.indigenous.microsub.timeline;

import android.media.MediaPlayer;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.MediaController;
import android.widget.VideoView;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.util.Preferences;

public class TimelineVideoActivity extends AppCompatActivity {

    VideoView videoPlayer;
    MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_video);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String videoUrl = extras.getString("video");

            videoPlayer = findViewById(R.id.timeline_video);
            videoPlayer.setVideoPath(videoUrl);
            videoPlayer.canPause();
            mediaController = new MediaController(this);
            mediaController.setAnchorView(videoPlayer);
            videoPlayer.setMediaController(mediaController);

            videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener()  {
                @Override
                public void onPrepared(MediaPlayer mp) {
                    mediaController.show();
                    if (Preferences.getPreference(TimelineVideoActivity.this, "pref_key_video_autoplay", false)) {
                        videoPlayer.start();
                    }
                }
            });
        }
    }

    @Override
    public void onBackPressed() {
        if (videoPlayer != null) {
            videoPlayer.stopPlayback();
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        if (videoPlayer != null) {
            videoPlayer.stopPlayback();
        }
        super.onDestroy();
    }

}
