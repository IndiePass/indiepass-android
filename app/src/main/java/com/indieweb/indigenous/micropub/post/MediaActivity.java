package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

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
        if (bitmap == null) {
            Toast.makeText(getApplicationContext(), getString(R.string.required_select_image), Toast.LENGTH_SHORT).show();
        }
        else {
            sendBasePost(item);
        }
    }

}
