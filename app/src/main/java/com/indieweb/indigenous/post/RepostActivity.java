package com.indieweb.indigenous.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class RepostActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        urlPostKey = "repost-of";
        autoSubmit = "pref_key_share_repost_auto_submit";
        addCounter = true;
        setContentView(R.layout.activity_repost);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            saveDraft("repost", null);
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
