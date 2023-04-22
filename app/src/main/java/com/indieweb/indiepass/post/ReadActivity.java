package com.indieweb.indiepass.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indiepass.R;
import com.indieweb.indiepass.model.Draft;
import com.indieweb.indiepass.util.Preferences;

public class ReadActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            if (read.getSelectedItemPosition() != 0) {
                String readStatus = getResources().getStringArray(R.array.read_array_post_values)[read.getSelectedItemPosition()];
                bodyParams.put("read-status", readStatus);
            }
            sendBasePost(item);
        }
    }

}
