package com.indieweb.indigenous.tracker;

import android.content.Context;

public class TrackerUtils {

    /**
     * Returns whether the tracker is supported.
     *
     * @return boolean
     */
    public static boolean supportsTracker() {
        return false;
    }

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The {@link Context}.
     */
    static boolean requestingLocationUpdates(Context context) {
        return false;
    }

}
