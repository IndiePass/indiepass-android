package com.indieweb.indigenous.tracker;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.location.LocationRequest;
import com.indieweb.indigenous.R;

import java.text.DateFormat;
import java.util.Date;

import static com.indieweb.indigenous.tracker.TrackerService.TRACKER_FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
import static com.indieweb.indigenous.tracker.TrackerService.TRACKER_UPDATE_INTERVAL_IN_MILLISECONDS;

class TrackerUtils {

    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context)
                .getBoolean(KEY_REQUESTING_LOCATION_UPDATES, false);
    }

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    static void setRequestingLocationUpdates(Context context, boolean requestingLocationUpdates) {
        PreferenceManager.getDefaultSharedPreferences(context)
                .edit()
                .putBoolean(KEY_REQUESTING_LOCATION_UPDATES, requestingLocationUpdates)
                .apply();
    }

    /**
     * Returns the {@code location} object as a human readable string.
     * @param location  The {@link Location}.
     */
    static String getLocationText(Location location) {
        return location == null ? "Unknown location" :
                "(" + location.getLatitude() + ", " + location.getLongitude() + ")";
    }

    /**
     * Location updated.
     *
     * @param context
     *   The context.
     *
     * @return string
     */
    static String getLocationTitle(Context context) {
        return context.getString(R.string.location_saved, DateFormat.getDateTimeInstance().format(new Date()));
    }


    /**
     * Get location request.
     *
     * @return LocationRequest
     */
    static LocationRequest getLocationRequest() {
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(TRACKER_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(TRACKER_FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

}
