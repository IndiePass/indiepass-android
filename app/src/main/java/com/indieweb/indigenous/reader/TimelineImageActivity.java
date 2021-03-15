// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.reader;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.viewpager.widget.PagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;

import java.util.ArrayList;

public class TimelineImageActivity extends AppCompatActivity {

    TextView info;
    ArrayList<String> photos = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_image);

        RelativeLayout layout = findViewById(R.id.image_root);

        // Get info text view.
        info = findViewById(R.id.timeline_image_info);

        photos = getIntent().getStringArrayListExtra("photos");
        if (photos != null && photos.size() > 0) {
            ViewPager viewPager = findViewById(R.id.view_pager);
            ImagePagerAdapter adapter = new ImagePagerAdapter();
            viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
                @Override
                public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                    if (photos.size() > 0) {
                        Integer total = photos.size();
                        String infoText = String.format(getString(R.string.image_pager), position + 1, total);
                        info.setText(infoText);
                    }
                }

                @Override
                public void onPageSelected(int position) { }

                @Override
                public void onPageScrollStateChanged(int state) { }
            });
            viewPager.setAdapter(adapter);
        }
        else {
            Snackbar.make(layout, getString(R.string.no_photos_found), Snackbar.LENGTH_SHORT).show();
        }
    }

    class ImagePagerAdapter extends PagerAdapter {

        private final LayoutInflater inflater;

        ImagePagerAdapter() {
            inflater = LayoutInflater.from(TimelineImageActivity.this);
        }

        @Override
        public int getCount() {
            return photos != null ? photos.size() : 0;
        }

        @Override
        public boolean isViewFromObject(@NonNull View view, @NonNull Object object) {
            return view == (object);
        }

        @NonNull
        @Override
        public Object instantiateItem(@NonNull ViewGroup container, int position) {
            View imageSlide = inflater.inflate(R.layout.widget_image_slide, null);
            final ImageView imageView = imageSlide.findViewById(R.id.timeline_image_fullscreen);

            Glide.with(TimelineImageActivity.this)
                .load(photos.get(position))
                .apply(new RequestOptions().placeholder(R.drawable.progress_loading))
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        imageView.setImageResource(0);
                        imageView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                        return false;
                    }
                })
                .into(imageView);
            container.addView(imageSlide, 0);

            return imageSlide;
        }

        @Override
        public void destroyItem(@NonNull ViewGroup container, int position, @NonNull Object object) {
            container.removeView((View) object);
        }
    }
}
