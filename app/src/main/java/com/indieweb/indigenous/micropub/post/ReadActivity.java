package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.BaseCreate;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.util.Preferences;

public class ReadActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Read";
        urlPostKey = "read-of";
        addCounter = true;
        setContentView(R.layout.activity_read);
        read = findViewById(R.id.read);
        super.onCreate(savedInstanceState);
        if (!preparedDraft) {
            read.setSelection(Preferences.getPreference(getApplicationContext(), "pref_key_read_default", 1));
        }
    }

    @Override
    public void onPostButtonClick(MenuItem item) {

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            Draft draft = new Draft();
            draft.setSpinner(read.getSelectedItem().toString());
            saveDraft("read", draft);
            return;
        }

        if (TextUtils.isEmpty(url.getText())) {
            url.setError(getString(R.string.required_field));
        }
        else {
            if (!read.getSelectedItem().toString().equals("none")) {
                bodyParams.put("read-status", read.getSelectedItem().toString());
            }
            sendBasePost(item);
        }
    }

}
