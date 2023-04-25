package com.indieweb.indigenous.reader;

import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.VideoView;
import androidx.appcompat.app.AppCompatActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.util.Preferences;

public class TimelineVideoActivity extends AppCompatActivity {

    VideoView videoPlayer;
    TextView noVideo;
    MediaController mediaController;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_video);
        noVideo = findViewById(R.id.timeline_video_not_video_found);
        videoPlayer = findViewById(R.id.timeline_video);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            final String videoUrl = extras.getString("video");

            if (videoUrl != null) {
                if (videoUrl.contains("youtube.com")) {
                    hideVideoPlayer(false);
                    TextView youtubeVideo = findViewById(R.id.timeline_video_youtube);
                    youtubeVideo.setVisibility(View.VISIBLE);
                    youtubeVideo.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(videoUrl));
                            startActivity(i);
                        }
                    });
                } else {
                    videoPlayer.setVideoPath(videoUrl);
                    videoPlayer.canPause();
                    mediaController = new MediaController(this);
                    mediaController.setAnchorView(videoPlayer);
                    videoPlayer.setMediaController(mediaController);
                    videoPlayer.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                        @Override
                        public void onPrepared(MediaPlayer mp) {
                            mediaController.show();
                            if (Preferences.getPreference(TimelineVideoActivity.this, "pref_key_video_autoplay", false)) {
                                videoPlayer.start();
                            }
                        }
                    });
                }
            } else {
                hideVideoPlayer(true);
            }
        } else {
            hideVideoPlayer(true);
        }
    }

    public void hideVideoPlayer(boolean showVideoNotFound) {
        videoPlayer.setVisibility(View.GONE);
        if (showVideoNotFound) {
            noVideo.setVisibility(View.VISIBLE);
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
