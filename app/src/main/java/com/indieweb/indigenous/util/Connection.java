package com.indieweb.indigenous.util;

import android.content.Context;
import android.net.ConnectivityManager;

public class Connection {

    private final Context context;

    public Connection(Context context) {
        this.context = context;
    }

    /**
     * Check if we have an internet connection.
     *
     * @return boolean.
     */
    public boolean hasConnection() {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        if ((cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected()) {
            return true;
        }
        return false;
    }


}
