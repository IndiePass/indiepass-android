package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.util.Preferences;

public class UploadActivity extends BaseCreateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isMediaRequest = true;
        finishActivity = false;
        canAddImage = true;
        postType = "Media";
        setContentView(R.layout.activity_upload);
        super.onCreate(savedInstanceState);

        if (title != null && Preferences.getPreference(getApplicationContext(), "pref_key_media_name", true)) {
            title.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        if (imageUris.size() == 1) {
            sendBasePost(item);
        }
        else {
            Toast.makeText(getApplicationContext(), getString(R.string.required_select_image), Toast.LENGTH_SHORT).show();
        }
    }

}
