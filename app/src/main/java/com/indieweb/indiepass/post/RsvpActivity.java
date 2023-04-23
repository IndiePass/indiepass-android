package com.indieweb.indiepass.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indiepass.R;
import com.indieweb.indiepass.model.Draft;

public class RsvpActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
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
            bodyParams.put("rsvp", getResources().getStringArray(R.array.rsvp_array_values)[rsvp.getSelectedItemPosition()]);
            sendBasePost(item);
        }
    }

}
