package com.indieweb.indigenous.microsub.timeline;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.indieweb.indigenous.R;

public class TimelineImageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_image);

        Button close = findViewById(R.id.close);
        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String imageUrl = extras.getString("imageUrl");
            ImageView imageView = findViewById(R.id.timeline_image_fullscreen);
            Glide.with(this)
                    .load(imageUrl)
                    .into(imageView);

        }
    }
}
