package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class CheckinActivity extends BaseCreateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddImage = true;
        canAddLocation = true;
        postType = "Checkin";
        isCheckin = true;
        addCounter = true;
        setContentView(R.layout.activity_checkin);
        super.onCreate(savedInstanceState);
        startLocationUpdates();
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        sendBasePost(item);
    }

}
