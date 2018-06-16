package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class LikeActivity extends BasePostActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Like";
        urlPostKey = "like-of";
        directSend = "pref_key_share_expose_like";
        setContentView(R.layout.activity_like);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        sendBasePost(item);
    }
}
