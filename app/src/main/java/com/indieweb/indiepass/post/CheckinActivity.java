package com.indieweb.indiepass.post;

import android.os.Bundle;
import android.view.MenuItem;

import com.indieweb.indiepass.R;

public class CheckinActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddMedia = true;
        canAddLocation = true;
        isCheckin = true;
        addCounter = true;
        setContentView(R.layout.activity_checkin);
        super.onCreate(savedInstanceState);
        if (!preparedDraft && !isTesting) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPostButtonClick(MenuItem item) {

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            saveDraft("checkin", null);
            return;
        }

        sendBasePost(item);
    }

}
