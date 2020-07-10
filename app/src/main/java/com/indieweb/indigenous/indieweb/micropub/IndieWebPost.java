package com.indieweb.indigenous.indieweb.micropub;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.indieweb.microsub.MicrosubAction;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.post.PostBase;
import com.indieweb.indigenous.util.Preferences;

public class IndieWebPost extends PostBase {

    public IndieWebPost(Context context, User user) {
        super(context, user);
    }

    @Override
    public String getMainPostTitle() {
        return this.getContext().getString(R.string.add_article);
    }

    @Override
    public String getEndpoint(boolean isMediaRequest) {

        String endpoint = this.getUser().getMicropubEndpoint();
        if (isMediaRequest) {
            endpoint = this.getUser().getMicropubMediaEndpoint();
        }

        return endpoint;
    }

    @Override
    public boolean supports(String name) {
        boolean supported = true;

        if (FEATURE_MEDIA_UPLOAD_DESCRIPTION.equals(name)) {
            supported = false;
        }

        return supported;
    }

    @Override
    public boolean useMediaEndpoint() {
        return Preferences.getPreference(getContext(), "pref_key_upload_media_endpoint", false);
    }

    @Override
    public String getFileFromMediaResponse(NetworkResponse response) {
        return response.headers.get("Location");
    }

    @Override
    public void deletePost(String channelId, String id) {
        new MicrosubAction(getContext(), getUser(), null).deletePost(channelId, id);
    }
}
