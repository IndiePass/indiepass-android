package com.indieweb.indigenous.microsub.timeline;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.indieweb.indigenous.R;
import com.keenfin.audioview.AudioView;

public class TimelineAudioActivity extends AppCompatActivity {

    AudioView audioPlayer;

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
            try {
                audioPlayer.setDataSource(audioUrl);
            }
            catch (Exception ignored) {
                Toast.makeText(getApplicationContext(), getString(R.string.audio_not_loaded), Toast.LENGTH_SHORT).show();
            }

        }
    }

    @Override
    public void onBackPressed() {
        if (audioPlayer != null) {
            audioPlayer.stop();
        }
        super.onBackPressed();
    }

    @Override
    public void onDestroy() {
        if (audioPlayer != null) {
            audioPlayer.stop();
        }
        super.onDestroy();
    }

}
