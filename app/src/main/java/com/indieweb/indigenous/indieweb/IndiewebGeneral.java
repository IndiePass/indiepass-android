package com.indieweb.indigenous.indieweb;

import android.content.Context;

import com.indieweb.indigenous.GeneralBase;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Preferences;

import java.util.ArrayList;

public class IndiewebGeneral extends GeneralBase {

    public IndiewebGeneral(Context context, User user) {
        super(context, user);
    }

    @Override
    public boolean supports(String feature) {
        boolean supported = true;

        switch (feature) {
            case FEATURE_UPLOAD:
                String micropubMediaEndpoint = this.getUser().getMicropubMediaEndpoint();
                if (micropubMediaEndpoint == null || micropubMediaEndpoint.length() == 0) {
                    supported = false;
                }
                break;
            case FEATURE_POSTS:
                supported = Preferences.getPreference(this.getContext(), "pref_key_source_post_list", false);
                break;
            case FEATURE_CONTACTS:
                supported = Preferences.getPreference(this.getContext(), "pref_key_contact_manage", false);
                break;
            case FEATURE_HIDE_POST_TYPES:
                supported = Preferences.getPreference(this.getContext(), "pref_key_post_type_hide", false);
                break;
        }

        return supported;
    }

    @Override
    public boolean hidePostTypes() {
        return false;
    }

    @Override
    public ArrayList<Integer> getProtectedPostTypes() {
        ArrayList<Integer> protectedTypes = new ArrayList<>();
        protectedTypes.add(R.id.createMedia);
        return protectedTypes;
    }
}
