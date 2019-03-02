package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Spinner;

import com.indieweb.indigenous.R;

public class GeocacheActivity extends BaseCreateActivity {

    Spinner geocacheLogType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddImage = true;
        canAddLocation = true;
        postType = "Geocache";
        isCheckin = true;
        addCounter = true;
        setContentView(R.layout.activity_geocache);
        super.onCreate(savedInstanceState);
        geocacheLogType = findViewById(R.id.geocacheLogType);
        startLocationUpdates();
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        bodyParams.put("geocache-log-type", geocacheLogType.getSelectedItem().toString());
        sendBasePost(item);
    }

}
