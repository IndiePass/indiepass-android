package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.BaseCreate;

public class CheckinActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddMedia = true;
        canAddLocation = true;
        postType = "Checkin";
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
