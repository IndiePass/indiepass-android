package com.indieweb.indigenous.tracker;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

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

    // The BroadcastReceiver used to listen from broadcasts from the service.
    private MyReceiver myReceiver;

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
        myReceiver = new MyReceiver();
        setContentView(R.layout.activity_track);
        action = findViewById(R.id.startNewTrack);
        action.setOnClickListener(this);
        user = new Accounts(this).getCurrentUser();
        db = new DatabaseHelper(getApplicationContext());
        title = findViewById(R.id.title);

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
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(myReceiver,
                new IntentFilter(TrackerService.ACTION_BROADCAST));
    }

    @Override
    protected void onPause() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(myReceiver);
        super.onPause();
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

                setButtonState(false);

                // TODO check a current track isn't running.
                Track track = new Track();
                track.setTitle(title.getText().toString());
                track.setAccount(user.getMeWithoutProtocol());
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
     * Receiver for broadcasts sent by {@link TrackerService}.
     */
    private class MyReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            Location location = intent.getParcelableExtra(TrackerService.EXTRA_LOCATION);
            if (location != null) {
                Toast.makeText(TrackActivity.this, TrackerUtils.getLocationText(location),
                        Toast.LENGTH_SHORT).show();
            }
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

        LocationRequest mLocationRequest = TrackerUtils.getLocationRequest();
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
