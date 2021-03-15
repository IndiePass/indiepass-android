// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class IssueActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        urlPostKey = "in-reply-to";
        addCounter = true;
        canAddMedia = true;
        setContentView(R.layout.activity_issue);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        boolean hasErrors = false;

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            saveDraft("issue", null);
            return;
        }

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
