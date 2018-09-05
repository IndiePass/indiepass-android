package com.indieweb.indigenous.micropub.post;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.indieweb.indigenous.R;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class EventActivity extends BasePostActivity  {

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
        bodyParams.put("start", startDate.getText().toString());
        bodyParams.put("end", endDate.getText().toString());
        sendBasePost(item);
    }

    /**
     * Start date onclick listener.
     */
    class startDateOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showDateTimePickerDialog(startDate);
        }
    }

    /**
     * End date onclick listener
     */
    class endDateOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            showDateTimePickerDialog(endDate);
        }
    }

    /**
     * Shows a DateTimePicker dialog.
     */
    public void showDateTimePickerDialog(final TextView t) {
        final Date[] value = {new Date()};
        final Calendar cal = Calendar.getInstance();
        cal.setTime(value[0]);
        new DatePickerDialog(this,
                new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(DatePicker view,
                                                    int y, int m, int d) {
                        cal.set(Calendar.YEAR, y);
                        cal.set(Calendar.MONTH, m);
                        cal.set(Calendar.DAY_OF_MONTH, d);

                        // now show the time picker
                        new TimePickerDialog(EventActivity.this,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override public void onTimeSet(TimePicker view, int h, int min) {
                                        cal.set(Calendar.HOUR_OF_DAY, h);
                                        cal.set(Calendar.MINUTE, min);
                                        value[0] = cal.getTime();

                                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:00Z");
                                        String result;
                                        try {
                                            result = df.format(value[0]);
                                            t.setText(result);
                                        } catch (Exception ignored) { }

                                    }
                                }, cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE), true).show();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

}
