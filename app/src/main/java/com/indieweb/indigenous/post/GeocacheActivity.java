package com.indieweb.indigenous.post;

import android.os.Bundle;
import android.view.MenuItem;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Draft;

public class GeocacheActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddMedia = true;
        canAddLocation = true;
        isCheckin = true;
        addCounter = true;
        setContentView(R.layout.activity_geocache);
        geocacheLogType = findViewById(R.id.geocacheLogType);
        super.onCreate(savedInstanceState);
        if (!preparedDraft && !isTesting) {
            startLocationUpdates();
        }
    }

    @Override
    public void onPostButtonClick(MenuItem item) {

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            Draft draft = new Draft();
            draft.setSpinner(geocacheLogType.getSelectedItem().toString());
            saveDraft("geocache", draft);
            return;
        }

        bodyParams.put("geocache-log-type", getResources().getStringArray(R.array.geocache_array_values)[geocacheLogType.getSelectedItemPosition()]);
        sendBasePost(item);
    }

}
