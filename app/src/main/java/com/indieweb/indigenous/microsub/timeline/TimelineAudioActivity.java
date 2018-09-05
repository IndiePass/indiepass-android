package com.indieweb.indigenous.microsub.timeline;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.hugomatilla.audioplayerview.AudioPlayerView;
import com.indieweb.indigenous.R;

public class TimelineAudioActivity extends AppCompatActivity {

    AudioPlayerView audioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_audio);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString("title");
            String audioUrl = extras.getString("audio");
            String authorName = extras.getString("authorName");
            String authorPhoto = extras.getString("authorPhoto");

            // Author photo.
            ImageView authorPhotoView = findViewById(R.id.timeline_audio_author_photo);
            Glide.with(this)
                    .load(authorPhoto)
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.avatar_small))
                    .into(authorPhotoView);

            // Author name.
            TextView authorNameView = findViewById(R.id.timeline_audio_author);
            authorNameView.setText(authorName);

            // Title.
            TextView titleView = findViewById(R.id.timeline_audio_title);
            if (title.length() > 0) {
                titleView.setText(title);
            }
            else {
                titleView.setText(audioUrl);
            }

            audioPlayer = findViewById(R.id.timeline_audio);
            audioPlayer.withUrl(audioUrl);

        }
    }

    @Override
    public void onBackPressed() {
        if (audioPlayer != null) {
            audioPlayer.destroy();
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        if (audioPlayer != null) {
            audioPlayer.destroy();
        }
        super.onDestroy();
    }

}
