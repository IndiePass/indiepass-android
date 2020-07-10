package com.indieweb.indigenous.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Draft;

public class RsvpActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "RSVP";
        urlPostKey = "in-reply-to";
        addCounter = true;
        setContentView(R.layout.activity_rsvp);
        rsvp = findViewById(R.id.rsvp);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            Draft draft = new Draft();
            draft.setSpinner(rsvp.getSelectedItem().toString());
            saveDraft("rsvp", draft);
            return;
        }

        if (TextUtils.isEmpty(url.getText())) {
            url.setError(getString(R.string.required_field));
        }
        else {
            bodyParams.put("rsvp", rsvp.getSelectedItem().toString());
            sendBasePost(item);
        }
    }

}
