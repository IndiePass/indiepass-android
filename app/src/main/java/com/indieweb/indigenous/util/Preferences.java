package com.indieweb.indigenous.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class Preferences {

    /**
     * Get a string preference.
     *
     * @param context
     *   The current context
     * @param pref
     *   The preference key
     * @param DefaultValue
     *   The default value
     *
     * @return
     *   The preference
     */
    public static String getPreference(Context context, String pref, String DefaultValue) {
        SharedPreferences preference = PreferenceManager.getDefaultSharedPreferences(context);
        return preference.getString(pref, DefaultValue);
    }

}
