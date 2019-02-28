package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.util.Utility;

public class EventActivity extends BaseCreateActivity {

    TextView startDate;
    TextView endDate;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Event";
        hType = "event";
        canAddImage = true;
        canAddLocation = true;
        addCounter = true;
        setContentView(R.layout.activity_event);
        super.onCreate(savedInstanceState);

        // Start and end date buttons.
        startDate = findViewById(R.id.startDate);
        startDate.setOnClickListener(new startDateOnClickListener());
        endDate = findViewById(R.id.endDate);
        endDate.setOnClickListener(new endDateOnClickListener());
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        boolean hasErrors = false;

        if (TextUtils.isEmpty(title.getText())) {
            hasErrors = true;
            title.setError(getString(R.string.required_field));
        }

        if (TextUtils.isEmpty(body.getText())) {
            hasErrors = true;
            body.setError(getString(R.string.required_field));
        }

        if (TextUtils.isEmpty(startDate.getText())) {
            hasErrors = true;
            startDate.setError(getString(R.string.required_field));
        }

        if (TextUtils.isEmpty(endDate.getText())) {
            hasErrors = true;
            endDate.setError(getString(R.string.required_field));
        }

        if (!hasErrors) {
            bodyParams.put("start", startDate.getText().toString());
            bodyParams.put("end", endDate.getText().toString());
            sendBasePost(item);
        }

    }

    /**
     * Start date onclick listener.
     */
    class startDateOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Utility.showDateTimePickerDialog(EventActivity.this, startDate);
        }
    }

    /**
     * End date onclick listener
     */
    class endDateOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Utility.showDateTimePickerDialog(EventActivity.this, endDate);
        }
    }


}
