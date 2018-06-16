package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class ReplyActivity extends BasePostActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Reply";
        urlPostKey = "in-reply-to";
        setContentView(R.layout.activity_reply);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        sendBasePost(item);
    }

}
