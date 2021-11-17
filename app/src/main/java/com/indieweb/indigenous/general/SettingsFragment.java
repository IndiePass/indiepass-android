package com.indieweb.indigenous.general;

import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;

import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

import com.indieweb.indigenous.R;

@SuppressWarnings("ConstantConditions")
public class SettingsFragment extends PreferenceFragmentCompat {

    private SettingsFragment.onPreferenceChangeListener callback;

    public void OnPreferenceChangeListener(SettingsFragment.onPreferenceChangeListener callback) {
        this.callback = callback;
    }

    public interface onPreferenceChangeListener {
        void onPreferenceChanged(int id, boolean visible);
    }

    @Override
    public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
        setPreferencesFromResource(R.xml.preferences, rootKey);
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requireActivity().setTitle(getString(R.string.settings));

        Preference dark = findPreference("night_mode");
        dark.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                restartApp();
                return true;
            }
        });

        Preference like = findPreference("pref_key_share_expose_like");
        like.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.parseBoolean(newValue.toString());
                if (checked) toggleAliasSetting("LikeAlias", 1);
                else toggleAliasSetting("LikeAlias", 2);
                return true;
            }
        });

        Preference repost = findPreference("pref_key_share_expose_repost");
        repost.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.parseBoolean(newValue.toString());
                if (checked) toggleAliasSetting("RepostAlias", 1);
                else toggleAliasSetting("RepostAlias", 2);
                return true;
            }
        });

        Preference bookmark = findPreference("pref_key_share_expose_bookmark");
        bookmark.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.parseBoolean(newValue.toString());
                if (checked) toggleAliasSetting("BookmarkAlias", 1);
                else toggleAliasSetting("BookmarkAlias", 2);
                return true;
            }
        });

        Preference reply = findPreference("pref_key_share_expose_reply");
        reply.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.parseBoolean(newValue.toString());
                if (checked) toggleAliasSetting("ReplyAlias", 1);
                else toggleAliasSetting("ReplyAlias", 2);
                return true;
            }
        });

        Preference upload = findPreference("pref_key_share_expose_upload");
        upload.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.parseBoolean(newValue.toString());
                if (checked) toggleAliasSetting("UploadAlias", 1);
                else toggleAliasSetting("UploadAlias", 2);
                return true;
            }
        });

        Preference feed = findPreference("pref_key_share_expose_feed");
        feed.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                boolean checked = Boolean.parseBoolean(newValue.toString());
                if (checked) toggleAliasSetting("FeedAlias", 1);
                else toggleAliasSetting("FeedAlias", 2);
                return true;
            }
        });

        Preference contact = findPreference("pref_key_contact_manage");
        contact.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                callback.onPreferenceChanged(R.id.nav_contacts, Boolean.parseBoolean(newValue.toString()));
                return true;
            }
        });

        Preference posts = findPreference("pref_key_source_post_list");
        posts.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                callback.onPreferenceChanged(R.id.nav_posts, Boolean.parseBoolean(newValue.toString()));
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
    private void toggleAliasSetting(String alias, Integer state) {
        PackageManager pm = requireContext().getPackageManager();
        ComponentName compName = new ComponentName(getContext().getPackageName(), getContext().getPackageName() + "." + alias);
        pm.setComponentEnabledSetting(compName, state, PackageManager.DONT_KILL_APP);
    }

    /**
     * Restart the app.
     */
    private void restartApp() {
        Intent intent = getActivity().getIntent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
        getActivity().finish();
        startActivity(intent);
    }

}