package com.indieweb.indigenous.micropub.post;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.net.Uri;
import android.os.Looper;
import android.provider.Settings;
import androidx.annotation.NonNull;

import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.indieweb.indigenous.BuildConfig;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.Base;
import com.indieweb.indigenous.model.Place;
import com.indieweb.indigenous.util.Preferences;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

abstract public class BasePlatformCreate extends Base {

    private FusedLocationProviderClient mFusedLocationClient;
    private Boolean mRequestingLocationUpdates = false;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.addLocation) {
            if (!mRequestingLocationUpdates) {
                Dexter.withActivity(this)
                        .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                        .withListener(new PermissionListener() {
                            @Override
                            public void onPermissionGranted(PermissionGrantedResponse response) {
                                mRequestingLocationUpdates = true;
                                startLocationUpdates();
                            }

                            @Override
                            public void onPermissionDenied(PermissionDeniedResponse response) {
                                if (response.isPermanentlyDenied()) {
                                    openSettings();
                                }
                            }

                            @Override
                            public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                                token.continuePermissionRequest();
                            }

                        }).check();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            startLocationUpdates();
        }
    }

    /**
     * Start location updates.
     */
    public void startLocationUpdates() {

        initLocationLibraries();
        if (mSettingsClient == null) {
            return;
        }

        // Set wrapper and coordinates visible (if it wasn't already).
        toggleLocationVisibilities(true);

        mSettingsClient
            .checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                    //noinspection MissingPermission
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                    updateLocationUI();
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
                                rae.startResolutionForResult(BasePlatformCreate.this, REQUEST_CHECK_SETTINGS);
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

    /**
     * Update the UI displaying the location data.
     */
    public void updateLocationUI() {
        if (mCurrentLocation != null) {

            coordinates = String.format("%s,%s,%s", mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude(), mCurrentLocation.getAltitude());
            String coordinatesText = String.format(getString(R.string.location_coordinates), coordinates);
            locationCoordinates.setText(coordinatesText);
            latitude = mCurrentLocation.getLatitude();
            longitude = mCurrentLocation.getLongitude();

            setChanges(true);

            // Toggle some visibilities.
            toggleLocationVisibilities(false);

            // Stop updates.
            stopLocationUpdates();
        }
        else {
            locationCoordinates.setText(R.string.getting_coordinates);
        }
    }

    /**
     * Toggle location visibilities.
     */
    public void toggleLocationVisibilities(Boolean toggleWrapper) {

        if (toggleWrapper) {
            locationWrapper.setVisibility(View.VISIBLE);
            locationCoordinates.setVisibility(View.VISIBLE);
            return;
        }

        boolean showLocationVisibility = Preferences.getPreference(getApplicationContext(), "pref_key_location_visibility", false);
        boolean showLocationName = Preferences.getPreference(getApplicationContext(), "pref_key_location_label", false);
        boolean showLocationQueryButton = Preferences.getPreference(getApplicationContext(), "pref_key_location_label_query", false);
        if (isCheckin || showLocationVisibility || showLocationName || showLocationQueryButton) {
            if (showLocationName || isCheckin) {
                locationName.setVisibility(View.VISIBLE);
            }
            if (showLocationVisibility) {
                locationVisibility.setVisibility(View.VISIBLE);
            }
            if (showLocationQueryButton) {
                locationQuery.setVisibility(View.VISIBLE);
                locationQuery.setOnClickListener(new OnLocationLabelQueryListener());
            }
            if (isCheckin) {
                locationUrl.setVisibility(View.VISIBLE);
            }
        }
    }

    /**
     * Stop location updates.
     */
    private void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Initiate the location libraries and services.
     */
    private void initLocationLibraries() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Open settings screen.
     */
    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    // Location query listener.
    class OnLocationLabelQueryListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            // Get geo from the endpoint.
            String MicropubEndpoint = user.getMicropubEndpoint();
            if (MicropubEndpoint.contains("?")) {
                MicropubEndpoint += "&q=geo";
            }
            else {
                MicropubEndpoint += "?q=geo";
            }

            if (mCurrentLocation != null) {
                MicropubEndpoint += "&lat=" + mCurrentLocation.getLatitude() + "&lon=" + mCurrentLocation.getLongitude();
            }
            else if (latitude != null && longitude != null) {
                MicropubEndpoint += "&lat=" + latitude.toString() + "&lon=" + longitude.toString();
            }

            Toast.makeText(getApplicationContext(), R.string.location_get, Toast.LENGTH_SHORT).show();
            StringRequest getRequest = new StringRequest(Request.Method.GET, MicropubEndpoint,
                    new Response.Listener<String>() {
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public void onResponse(String response) {
                            placeItems.clear();
                            String label = "";
                            String visibility = "";
                            try {
                                JSONObject geoResponse = new JSONObject(response);

                                // Geo property.
                                if (geoResponse.has("geo")) {
                                    JSONObject geoObject = geoResponse.getJSONObject("geo");
                                    if (geoObject.has("label")) {
                                        label = geoObject.getString("label");
                                    }
                                    if (geoObject.has("visibility")) {
                                        visibility = geoObject.getString("visibility");
                                    }
                                }

                                // Places property.
                                if (geoResponse.has("places")) {
                                    JSONObject placeObject;
                                    JSONArray placesList = geoResponse.getJSONArray("places");
                                    for (int i = 0; i < placesList.length(); i++) {
                                        Place place = new Place();
                                        placeObject = placesList.getJSONObject(i);

                                        if (placeObject.has("label")) {
                                            place.setName(placeObject.getString("label"));

                                            if (placeObject.has("longitude")) {
                                                place.setLongitude(placeObject.getString("longitude"));
                                            }

                                            if (placeObject.has("latitude")) {
                                                place.setLatitude(placeObject.getString("latitude"));
                                            }

                                            if (placeObject.has("url")) {
                                                place.setUrl(placeObject.getString("url"));
                                            }

                                            placeItems.add(place);
                                        }
                                    }
                                }
                            }
                            catch (JSONException e) {
                                Toast.makeText(getApplicationContext(), getString(R.string.location_error) + e.getMessage(), Toast.LENGTH_LONG).show();
                            }

                            if (label.length() > 0 || placeItems.size() > 0) {
                                if (placeItems.size() > 0) {
                                    Toast.makeText(getApplicationContext(), getString(R.string.venues_found), Toast.LENGTH_SHORT).show();

                                    locationName.setThreshold(1);
                                    final ArrayAdapter venuesAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.popup_item, placeItems);
                                    locationName.setAdapter(venuesAdapter);
                                    locationName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                            Place selected = (Place) arg0.getAdapter().getItem(arg2);
                                            locationName.setText(selected.getName());
                                            if (selected.getUrl().length() > 0) {
                                                locationUrl.setText(selected.getUrl());
                                            }
                                        }
                                    });
                                    locationName.setOnTouchListener(new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                                            if (!locationName.getText().toString().equals("")) {
                                                venuesAdapter.getFilter().filter(null);
                                            }
                                            locationName.showDropDown();
                                            return false;
                                        }
                                    });
                                }
                                else {
                                    locationName.setText(label);
                                }
                                if (visibility.length() > 0) {
                                    int selection = 0;
                                    if (visibility.equals("private")) {
                                        selection = 1;
                                    }
                                    else if (visibility.equals("protected")) {
                                        selection = 2;
                                    }
                                    locationVisibility.setSelection(selection);
                                }
                            }
                            else {
                                Toast.makeText(getApplicationContext(), getString(R.string.location_no_results), Toast.LENGTH_SHORT).show();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {}
                    }
            )
            {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Accept", "application/json");
                    headers.put("Authorization", "Bearer " + user.getAccessToken());
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(getRequest);
        }
    }

}