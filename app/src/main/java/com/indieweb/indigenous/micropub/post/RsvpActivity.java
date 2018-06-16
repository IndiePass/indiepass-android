package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Spinner;
import com.indieweb.indigenous.R;

public class RsvpActivity extends BasePostActivity {

    Spinner rsvp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "RSVP";
        urlPostKey = "in-reply-to";
        rsvp = findViewById(R.id.rsvp);
        setContentView(R.layout.activity_rsvp);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        bodyParams.put("rsvp", rsvp.getSelectedItem().toString());
        sendBasePost(item);
    }

}
