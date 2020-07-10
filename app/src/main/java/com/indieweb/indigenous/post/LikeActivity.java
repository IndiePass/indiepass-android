package com.indieweb.indigenous.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class LikeActivity extends BaseCreate {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Like";
        urlPostKey = "like-of";
        autoSubmit = "pref_key_share_like_auto_submit";
        setContentView(R.layout.activity_like);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            saveDraft("like", null);
            return;
        }

        if (TextUtils.isEmpty(url.getText())) {
            url.setError(getString(R.string.required_field));
        }
        else {
            sendBasePost(item);
        }
    }
}
