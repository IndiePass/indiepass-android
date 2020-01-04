package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.BaseCreate;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.util.Utility;

public class EventActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        postType = "Event";
        hType = "event";
        canAddMedia = true;
        canAddLocation = true;
        addCounter = true;
        setContentView(R.layout.activity_event);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);
        super.onCreate(savedInstanceState);

        // Start and end date buttons.
        startDate.setOnClickListener(new startDateOnClickListener());
        endDate.setOnClickListener(new endDateOnClickListener());
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        boolean hasErrors = false;

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            Draft draft = new Draft();

            if (!TextUtils.isEmpty(startDate.getText())) {
                draft.setStartDate(startDate.getText().toString());
            }

            if (!TextUtils.isEmpty(endDate.getText())) {
                draft.setEndDate(endDate.getText().toString());
            }

            saveDraft("event", draft);
            return;
        }

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
