package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class ReplyActivity extends BaseCreateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Reply";
        urlPostKey = "in-reply-to";
        addCounter = true;
        setContentView(R.layout.activity_reply);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        boolean hasErrors = false;

        if (TextUtils.isEmpty(url.getText())) {
            hasErrors = true;
            url.setError(getString(R.string.required_field));
        }

        if (TextUtils.isEmpty(body.getText())) {
            hasErrors = true;
            body.setError(getString(R.string.required_field));
        }

        if (!hasErrors) {
            sendBasePost(item);
        }
    }

}
