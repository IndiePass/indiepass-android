package com.indieweb.indigenous.tracker;

import android.content.Context;
import android.location.Location;
import android.preference.PreferenceManager;

import com.google.android.gms.location.LocationRequest;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Track;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

import java.text.DateFormat;
import java.util.Date;

import static com.indieweb.indigenous.tracker.TrackerService.TRACKER_FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
import static com.indieweb.indigenous.tracker.TrackerService.TRACKER_UPDATE_INTERVAL_IN_MILLISECONDS;

public class TrackerUtils {

    private static final String KEY_REQUESTING_LOCATION_UPDATES = "requesting_location_updates";

    /**
     * Returns whether the tracker is supported.
     *
     * @return boolean
     */
    public static boolean supportsTracker() {
        return true;
    }

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
    static LocationRequest getLocationRequest(Context context) {
        long interval = TRACKER_UPDATE_INTERVAL_IN_MILLISECONDS;
        long fastestInterval = TRACKER_FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS;
        User user = new Accounts(context).getCurrentUser();
        DatabaseHelper db = new DatabaseHelper(context);
        int trackerId = db.getLatestTrackId(user.getMeWithoutProtocol());
        if (trackerId > 0) {
            Track track = db.getTrack(trackerId);
            if (track != null) {
                interval = track.getInterval();
                fastestInterval = track.getInterval() / 2;
            }
        }

        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(interval);
        mLocationRequest.setFastestInterval(fastestInterval);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        return mLocationRequest;
    }

}
