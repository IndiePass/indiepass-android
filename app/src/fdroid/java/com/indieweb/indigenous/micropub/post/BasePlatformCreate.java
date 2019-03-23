package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.View;
import com.indieweb.indigenous.micropub.Base;

abstract public class BasePlatformCreate extends Base {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddLocation = false;
        super.onCreate(savedInstanceState);
    }

    /**
     * Start location updates.
     */
    public void startLocationUpdates() {
    }

    /**
     * Update the UI displaying the location data.
     */
    public void updateLocationUI() {
    }

    /**
     * Toggle location visibilities.
     */
    public void toggleLocationVisibilities(Boolean toggleWrapper) {
    }

    /**
     * Stop location updates.
     */
    private void stopLocationUpdates() {
    }

    /**
     * Initiate the location libraries and services.
     */
    private void initLocationLibraries() {
    }

    /**
     * Open settings screen.
     */
    private void openSettings() {
    }

    // Location query listener.
    class OnLocationLabelQueryListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

        }
    }

}