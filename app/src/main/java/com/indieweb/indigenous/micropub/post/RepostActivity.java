package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class RepostActivity extends BasePostActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Repost";
        urlPostKey = "repost-of";
        directSend = "pref_key_share_expose_repost";
        setContentView(R.layout.activity_repost);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        sendBasePost(item);
    }
}
