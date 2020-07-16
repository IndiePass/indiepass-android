package com.indieweb.indigenous.post;

import android.widget.MultiAutoCompleteTextView;

import com.android.volley.NetworkResponse;

public interface Post {

    String FEATURE_TITLE = "FEATURE_POST_TITLE";
    String FEATURE_CATEGORIES = "FEATURE_POST_CATEGORIES";
    String FEATURE_CONTACTS = "FEATURE_POST_CONTACTS";
    String FEATURE_AUDIO = "FEATURE_POST_AUDIO";
    String FEATURE_LOCATION = "FEATURE_POST_LOCATION";
    String FEATURE_POST_STATUS = "FEATURE_POST_STATUS";
    String FEATURE_MEDIA_UPLOAD_DESCRIPTION = "FEATURE_MEDIA_UPLOAD_DESCRIPTION";
    String FEATURE_SPOILER = "FEATURE_POST_SPOILER";
    String POST_PARAM_H = "h";
    String POST_PARAM_PUBLISHED = "published";
    String POST_PARAM_CONTENT = "content";
    String POST_PARAM_POST_STATUS = "post-status";
    String POST_PARAM_REPLY = "in-reply-to";
    String POST_PARAM_PHOTO = "photo";
    String POST_PARAM_VIDEO = "video";

    String getEndpoint(boolean isMediaRequest);

    String getFileFromMediaResponse(NetworkResponse response);

    String getMainPostTitle();

    boolean useMediaEndpoint();

    void deletePost(String channelId, String id);

    boolean supports(String name);

    boolean supportsPostParam(String name);

    String getPostParamName(String name);

    void prepareTagsAutocomplete(MultiAutoCompleteTextView tags);

    void prepareContactsAutocomplete(MultiAutoCompleteTextView body);

    boolean hideUrlField();

    boolean canNotPostAnonymous();

    String anonymousPostMessage();
}
