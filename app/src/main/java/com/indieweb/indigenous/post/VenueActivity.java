package com.indieweb.indigenous.post;

import android.os.Bundle;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class VenueActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddMedia = true;
        canAddLocation = true;
        postType = "Venue";
        isCheckin = true;
        addCounter = true;
        hType = "card";
        setContentView(R.layout.activity_venue);
        super.onCreate(savedInstanceState);
        if (!preparedDraft && !isTesting) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPostButtonClick(MenuItem item) {

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            saveDraft("venue", null);
            return;
        }

        sendBasePost(item);
    }

}
