package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class MediaActivity extends BasePostActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        isMediaRequest = true;
        finishActivity = false;
        canAddImage = true;
        postType = "Media";
        setContentView(R.layout.activity_media);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        sendBasePost(item);
    }

}
