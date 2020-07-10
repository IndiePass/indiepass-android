package com.indieweb.indigenous.post;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.provider.Settings;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.post.Base;
import com.indieweb.indigenous.util.Utility;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

abstract public class BasePlatformCreate extends Base {

    LocationManager locationManager;
    LocationListener locationListener;
    public Boolean mRequestingLocationUpdates = false;
    public static final int REQUEST_CHECK_LOCATION_SETTINGS = 100;
    public static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;

    /**
     * Start location updates.
     */
    public void startLocationUpdates() {

        Dexter.withActivity(this)
                .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        if (locationManager == null) {
                            initLocationLibraries();
                        }

                        try {
                            mRequestingLocationUpdates = true;
                            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, UPDATE_INTERVAL_IN_MILLISECONDS, 0, locationListener);
                            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, UPDATE_INTERVAL_IN_MILLISECONDS, 0, locationListener);

                            // Set wrapper and coordinates visible (if it wasn't already).
                            toggleLocationVisibilities(true);
                        }
                        catch (SecurityException ignored) { }
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

    /**
     * Stop location updates.
     */
    private void stopLocationUpdates() {
        mRequestingLocationUpdates = false;
        locationManager.removeUpdates(locationListener);
    }

    /**
     * Initiate the location libraries and services.
     */
    private void initLocationLibraries() {
        locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);

        locationListener = new LocationListener() {
            public void onLocationChanged(Location location) {
                mCurrentLocation = location;
                updateLocationUI();

            }

            public void onStatusChanged(String provider, int status, Bundle extras) {}

            public void onProviderEnabled(String provider) {}

            public void onProviderDisabled(String provider) {}
        };

        // Check if the providers are enabled,
        final boolean gpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
        final boolean networkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
        if (!gpsEnabled && !networkEnabled) {
            // Build an alert dialog here that requests that the user enable
            // the location services, then when the user clicks the "OK" button,
            // call enableLocationSettings()
            enableLocationSettings();
        }

        startLocationUpdates();
    }

    /**
     * Open location settings.
     */
    private void enableLocationSettings() {
        Intent settingsIntent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
        startActivity(settingsIntent);
    }

    /**
     * Update the UI displaying the location data.
     *
     * We have the same function in the playstore implementation. So don't forget to update in
     * case something changes here.
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

}