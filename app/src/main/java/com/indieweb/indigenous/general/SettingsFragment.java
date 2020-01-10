package com.indieweb.indigenous.general;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceScreen;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.push.PushNotificationActivity;
import com.indieweb.indigenous.tracker.TrackerUtils;

@SuppressWarnings("ConstantConditions")
public class SettingsFragment extends PreferenceFragmentCompat {

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().setTitle(getString(R.string.settings));

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

        Preference pushNotifications = findPreference("pref_key_push_notifications");
        pushNotifications.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
            @Override
            public boolean onPreferenceClick(Preference preference) {
                Intent i = new Intent(requireContext(), PushNotificationActivity.class);
                startActivity(i);
                return true;
            }
        });

        if (!TrackerUtils.supportsTracker()) {
            PreferenceScreen preferenceScreen = findPreference("preferenceScreen");
            PreferenceCategory tracker = findPreference("pref_key_category_tracker");
            if (tracker != null) {
                preferenceScreen.removePreference(tracker);
            }
        }
    }

    /**
     * Toggle the state of an alias.
     *
     * @param alias
     *   The alias name
     * @param state
     *   The state of the alias. 1 = enabled, 2 = disabled.
     */
    private void toggleAliasSetting(String alias, Integer state) {
        PackageManager pm = requireContext().getPackageManager();
        ComponentName compName = new ComponentName(getContext().getPackageName(), getContext().getPackageName() + "." + alias);
        pm.setComponentEnabledSetting(compName, state, PackageManager.DONT_KILL_APP);
    }

}