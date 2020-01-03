package com.indieweb.indigenous.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Track;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Utility;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.multi.MultiplePermissionsListener;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

public class TrackActivity extends AppCompatActivity implements View.OnClickListener {

    // A reference to the service used to get location updates.
    private TrackerService mService = null;

    // Tracks the bound state of the service.
    private boolean mBound = false;

    User user;
    DatabaseHelper db;
    int trackerId = 0;
    Button action;
    Track track;
    EditText title;
    Spinner transport;
    EditText timeAmmount;
    Spinner timeUnit;
    public static final int REQUEST_CHECK_BACKGROUND_LOCATION_SETTINGS = 100;

    // Monitors the state of the connection to the service.
    private final ServiceConnection mServiceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TrackerService.LocalBinder binder = (TrackerService.LocalBinder) service;
            mService = binder.getService();
            mBound = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            mBound = false;
        }
    };

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_BACKGROUND_LOCATION_SETTINGS && resultCode == RESULT_OK) {
            startBackgroundService();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_track);
        action = findViewById(R.id.startNewTrack);
        action.setOnClickListener(this);
        user = new Accounts(this).getCurrentUser();
        db = new DatabaseHelper(getApplicationContext());
        title = findViewById(R.id.title);
        timeAmmount = findViewById(R.id.time_amount);
        timeUnit = findViewById(R.id.time_unit);
        transport = findViewById(R.id.transport);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int trackerId = extras.getInt("trackId");
            if (trackerId != 0) {
                Track t = db.getTrack(trackerId);
                if (t != null) {
                    track = t;
                    setTitle(getString(R.string.edit_track));
                    Button save = findViewById(R.id.editTrack);
                    save.setOnClickListener(this);
                    save.setVisibility(View.VISIBLE);
                    action.setVisibility(View.GONE);
                    title.setText(track.getTitle());
                    String[] trans = getResources().getStringArray(R.array.transport_array);
                    for (int i = 0; i < trans.length; i++) {
                        if (trans[i].equals(track.getTransport())) {
                            transport.setSelection(i);
                        }
                    }

                    // Hide some stuff.
                    TextView tt = findViewById(R.id.interval_text);
                    tt.setVisibility(View.GONE);
                    LinearLayout lt = findViewById(R.id.track_interval);
                    lt.setVisibility(View.GONE);
                }
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Bind to the service. If the service is in foreground mode, this signals to the service
        // that since this activity is in the foreground, the service can exit foreground mode.
        bindService(new Intent(this, TrackerService.class), mServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        if (mBound) {
            // Unbind from the service. This signals to the service that this activity is no longer
            // in the foreground, and the service can respond by promoting itself to a foreground
            // service.
            unbindService(mServiceConnection);
            mBound = false;
        }
        super.onStop();
    }

    @Override
    public void onClick(View v) {

        // Title is always required.
        if (TextUtils.isEmpty(title.getText())) {
            title.setError(getString(R.string.required_field));
            return;
        }

        switch (v.getId()) {

            case R.id.editTrack:
                track.setTitle(title.getText().toString());
                db.saveTrack(track, false);
                Toast.makeText(getApplicationContext(), getString(R.string.track_updated), Toast.LENGTH_SHORT).show();
                Intent returnIntent = new Intent();
                setResult(RESULT_OK, returnIntent);
                finish();
                break;

            case R.id.startNewTrack:

                if (TextUtils.isEmpty(timeAmmount.getText())) {
                    timeAmmount.setError(getString(R.string.required_field));
                    return;
                }

                int interval = 0;
                int amount = Integer.parseInt(timeAmmount.getText().toString());
                if (timeUnit.getSelectedItem().toString().equals("second(s)")) {
                    interval = amount * 1000;
                }
                else {
                    interval = (amount * 60) * 1000;
                }

                if (interval <= 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.interval_negative), Toast.LENGTH_SHORT).show();
                    return;
                }

                setButtonState(false);

                Track track = new Track();
                track.setTitle(title.getText().toString());
                track.setAccount(user.getMeWithoutProtocol());
                track.setTransport(transport.getSelectedItem().toString());
                track.setInterval(interval);
                try {
                    db.saveTrack(track, false);
                    Toast.makeText(getApplicationContext(), getString(R.string.track_started), Toast.LENGTH_LONG).show();
                    trackerId = db.getLatestTrackId(user.getMeWithoutProtocol());
                    if (trackerId > 0) {
                        startTracker();
                    }
                    else {
                        setButtonState(true);
                        Toast.makeText(getApplicationContext(), getString(R.string.track_start_error_no_id), Toast.LENGTH_LONG).show();
                    }
                }
                catch (Exception e) {
                    setButtonState(true);
                    Toast.makeText(getApplicationContext(), String.format(getString(R.string.tracker_error_start), e.getMessage()), Toast.LENGTH_LONG).show();
                }
                break;
        }
    }

    /**
     * Set state of button.
     *
     * @param enable
     *   Whether to enable the button or not.
     */
    private void setButtonState(boolean enable) {
        if (enable) {
            action.setEnabled(enable);
        }
        else {
            action.setEnabled(enable);
        }
    }

    /**
     * Start tracker.
     */
    public void startTracker() {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            Dexter.withActivity(this)
                    .withPermissions(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
                    .withListener(new MultiplePermissionsListener() {

                        @Override
                        public void onPermissionsChecked(MultiplePermissionsReport report) {
                            if (report.areAllPermissionsGranted()) {
                                startBackgroundService();
                            }
                            else if (report.isAnyPermissionPermanentlyDenied() || report.getDeniedPermissionResponses().size() > 0) {
                                Utility.openSettings(getApplicationContext());
                            }
                            else {
                                Utility.openSettings(getApplicationContext());
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(List<PermissionRequest> permissions, PermissionToken token) {
                            token.continuePermissionRequest();
                        }

                    }).check();
        }
        else {
            Dexter.withActivity(this)
                    .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                    .withListener(new PermissionListener() {
                        @Override
                        public void onPermissionGranted(PermissionGrantedResponse response) {
                            startBackgroundService();
                        }

                        @Override
                        public void onPermissionDenied(PermissionDeniedResponse response) {
                            if (response.isPermanentlyDenied()) {
                                Utility.openSettings(getApplicationContext());
                            }
                        }

                        @Override
                        public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                            token.continuePermissionRequest();
                        }

                    }).check();
        }
    }

    /**
     * Start background service.
     */
    public void startBackgroundService() {

        LocationRequest mLocationRequest = TrackerUtils.getLocationRequest(getApplicationContext());
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest mLocationSettingsRequest = builder.build();

        SettingsClient mSettingsClient = LocationServices.getSettingsClient(this);
        mSettingsClient
                .checkLocationSettings(mLocationSettingsRequest)
                .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                    @SuppressLint("MissingPermission")
                    @Override
                    public void onSuccess(LocationSettingsResponse locationSettingsResponse) {
                        //noinspection MissingPermission
                        mService.requestLocationUpdates();
                        Intent returnIntent = new Intent();
                        setResult(RESULT_OK, returnIntent);
                        finish();
                    }
                })
                .addOnFailureListener(this, new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        int statusCode = ((ApiException) e).getStatusCode();
                        switch (statusCode) {
                            case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                                try {
                                    // Show the dialog by calling startResolutionForResult(),
                                    // and check the result in onActivityResult().
                                    ResolvableApiException rae = (ResolvableApiException) e;
                                    rae.startResolutionForResult(TrackActivity.this, REQUEST_CHECK_BACKGROUND_LOCATION_SETTINGS);
                                }
                                catch (IntentSender.SendIntentException sie) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.location_intent_error), Toast.LENGTH_SHORT).show();
                                }
                                break;
                            case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                                Toast.makeText(getApplicationContext(), getString(R.string.location_settings_error), Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

}
