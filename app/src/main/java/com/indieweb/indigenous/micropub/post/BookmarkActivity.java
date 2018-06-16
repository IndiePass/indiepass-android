package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;
import com.indieweb.indigenous.R;

public class BookmarkActivity extends BasePostActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Bookmark";
        urlPostKey = "bookmark-of";
        directSend = "pref_key_share_expose_bookmark";
        setContentView(R.layout.activity_bookmark);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        sendBasePost(item);
    }
}
