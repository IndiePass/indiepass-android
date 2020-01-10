package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.BaseCreate;
import com.indieweb.indigenous.util.Preferences;

public class UploadActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isMediaRequest = true;
        finishActivity = false;
        canAddMedia = true;
        postType = "Media";
        setContentView(R.layout.activity_upload);
        super.onCreate(savedInstanceState);

        if (title != null && Preferences.getPreference(getApplicationContext(), "pref_key_media_name", false)) {
            title.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        if (image.size() == 1 || video.size() == 1 || audio.size() == 1) {
            sendBasePost(item);
        }
        else {
            Snackbar.make(layout, getString(R.string.required_select_media), Snackbar.LENGTH_SHORT).show();
        }
    }

}
