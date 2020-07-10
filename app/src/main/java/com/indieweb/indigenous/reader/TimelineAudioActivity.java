package com.indieweb.indigenous.reader;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.keenfin.audioview.AudioView;

public class TimelineAudioActivity extends AppCompatActivity {

    AudioView audioPlayer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_audio);
        final LinearLayout layout = findViewById(R.id.audio_root);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String title = extras.getString("title");
            String authorName = extras.getString("authorName");
            String authorPhoto = extras.getString("authorPhoto");
            final String audioUrl = extras.getString("audio");

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
                Snackbar.make(layout, getString(R.string.audio_not_loaded), Snackbar.LENGTH_SHORT).show();
            }

            // Allow to open in favorite intent.
            Button open = findViewById(R.id.audioExternal);
            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    try {
                        Intent intent = new Intent(Intent.ACTION_VIEW);
                        intent.setDataAndTypeAndNormalize(Uri.parse(audioUrl), "audio/*");
                        startActivity(intent);
                    }
                    catch (Exception ignored) {
                        Snackbar.make(layout, getString(R.string.audio_no_app_found), Snackbar.LENGTH_SHORT).show();
                    }
                }
            });

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
