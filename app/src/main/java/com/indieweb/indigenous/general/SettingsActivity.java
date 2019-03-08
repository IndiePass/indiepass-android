package com.indieweb.indigenous.general;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.SystemClock;
import android.preference.Preference;
import android.widget.Toast;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.microsub.broadcast.MicrosubBroadcastReceiver;

public class SettingsActivity extends AppCompatPreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);

        Preference like = findPreference("pref_key_share_expose_like");
        like.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked) toggleAliasSetting("LikeAlias", 1);
                else toggleAliasSetting("LikeAlias", 2);
                return true;
            }
        });

        Preference repost = findPreference("pref_key_share_expose_repost");
        repost.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked) toggleAliasSetting("RepostAlias", 1);
                else toggleAliasSetting("RepostAlias", 2);
                return true;
            }
        });

        Preference bookmark = findPreference("pref_key_share_expose_bookmark");
        bookmark.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked) toggleAliasSetting("BookmarkAlias", 1);
                else toggleAliasSetting("BookmarkAlias", 2);
                return true;
            }
        });

        Preference reply = findPreference("pref_key_share_expose_reply");
        reply.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked) toggleAliasSetting("ReplyAlias", 1);
                else toggleAliasSetting("ReplyAlias", 2);
                return true;
            }
        });

        Preference upload = findPreference("pref_key_share_expose_upload");
        upload.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked) toggleAliasSetting("UploadAlias", 1);
                else toggleAliasSetting("UploadAlias", 2);
                return true;
            }
        });

        Preference feed = findPreference("pref_key_share_expose_feed");
        feed.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked) toggleAliasSetting("FeedAlias", 1);
                else toggleAliasSetting("FeedAlias", 2);
                return true;
            }
        });

        Preference checkNewPosts = findPreference("pref_key_check_new_posts");
        checkNewPosts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.valueOf(newValue.toString());
                if (checked) setAlarmForPostCheck(getApplicationContext(), true);
                else setAlarmForPostCheck(getApplicationContext(),false);
                return true;
            }
        });

    }

    /**
     * Toggle the state of an alias.
     *
     * @param alias
     *   The alias name
     * @param state
     *   The state of the alias. 1 = enabled, 2 = disabled.
     */
    public void toggleAliasSetting(String alias, Integer state) {
        PackageManager pm = getApplicationContext().getPackageManager();
        ComponentName compName = new ComponentName(getPackageName(), getPackageName() + "." + alias);
        pm.setComponentEnabledSetting(compName, state, PackageManager.DONT_KILL_APP);
    }

    /**
     * Set alarm for checking new posts in the background.
     *
     * @param setAlarm
     *   Whether to set or unset the alarm.
     */
    public static void setAlarmForPostCheck(Context context, boolean setAlarm) {

        Intent intent = new Intent(context, MicrosubBroadcastReceiver.class);
        PendingIntent pendingIntent = PendingIntent.getBroadcast(context.getApplicationContext(), 234, intent, 0);
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(ALARM_SERVICE);

        if (setAlarm) {
            alarmManager.setInexactRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP, SystemClock.elapsedRealtime() + AlarmManager.INTERVAL_HOUR, AlarmManager.INTERVAL_HOUR, pendingIntent);
        }
        else {
            alarmManager.cancel(pendingIntent);
        }
    }
}