package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class IssueActivity extends BaseCreateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Issue";
        urlPostKey = "in-reply-to";
        addCounter = true;
        canAddImage = true;
        setContentView(R.layout.activity_issue);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        boolean hasErrors = false;

        if (TextUtils.isEmpty(url.getText())) {
            hasErrors = true;
            url.setError(getString(R.string.required_field));
        }

        if (TextUtils.isEmpty(title.getText())) {
            hasErrors = true;
            title.setError(getString(R.string.required_field));
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
