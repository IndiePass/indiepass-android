package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.BaseCreate;

public class ReplyActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Reply";
        urlPostKey = "in-reply-to";
        addCounter = true;
        canAddImage = true;
        setContentView(R.layout.activity_reply);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        boolean hasErrors = false;

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            saveDraft("reply", null);
            return;
        }

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
