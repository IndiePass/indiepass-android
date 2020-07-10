package com.indieweb.indigenous.pixelfed;

import android.content.Context;

import com.android.volley.NetworkResponse;
import com.android.volley.toolbox.HttpHeaderParser;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.post.PostBase;
import com.indieweb.indigenous.util.HTTPRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

public class PixelfedPost extends PostBase {

    public PixelfedPost(Context context, User user) {
        super(context, user);
    }

    @Override
    public String getMainPostTitle() {
        return this.getContext().getString(R.string.add_post);
    }

    @Override
    public String getEndpoint(boolean isMediaRequest) {
        if (isMediaRequest) {
            return this.getUser().getMe() + "/api/v1/media";
        }
        return this.getUser().getMe() + "/api/v1/statuses";
    }

    @Override
    public String getFileFromMediaResponse(NetworkResponse response) {
        String data;
        try {
            data = new String(response.data, HttpHeaderParser.parseCharset(response.headers, "utf-8"));
        }
        catch (UnsupportedEncodingException ignored) {
            data = new String(response.data);
        }
        String fileId = "";
        try {
            JSONObject o = new JSONObject(data);
            fileId = o.getString("id");
        }
        catch (JSONException ignored) {}

        return fileId;
    }

    @Override
    public boolean supports(String name) {
        boolean supported = true;

        switch (name) {
            case FEATURE_TITLE:
            case FEATURE_CATEGORIES:
                supported = false;
        }

        return supported;
    }

    @Override
    public boolean supportsPostParam(String name) {
        boolean supported = true;
        switch (name) {
            // TODO create constants
            case "h":
            case "published":
            case "post-status":
                supported = false;
                break;
        }

        return supported;
    }

    @Override
    public String getPostParamName(String name) {
        String paramName = name;
        switch (name) {
            case "content":
                paramName = "status";
                break;
            case "published":
                paramName = "scheduled_at";
                break;
            case "in-reply-to":
                paramName = "in_reply_to_id";
                break;
            case "photo":
            case "video":
                paramName = "media_ids";
                break;
        }

        return paramName;
    }

    @Override
    public void deletePost(String channelId, String id) {
        String endpoint = getUser().getMe() + "/api/v1/statuses/" + id;
        HTTPRequest r = new HTTPRequest(null, getUser(), getContext());
        r.doDeleteRequest(endpoint, null);
    }
}
