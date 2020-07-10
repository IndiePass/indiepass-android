package com.indieweb.indigenous.post;

import com.android.volley.NetworkResponse;

public interface Post {

    String FEATURE_TITLE = "FEATURE_POST_TITLE";
    String FEATURE_CATEGORIES = "FEATURE_POST_CATEGORIES";
    String FEATURE_MEDIA_UPLOAD_DESCRIPTION = "FEATURE_MEDIA_UPLOAD_DESCRIPTION";

    String getEndpoint(boolean isMediaRequest);

    String getFileFromMediaResponse(NetworkResponse response);

    String getMainPostTitle();

    boolean useMediaEndpoint();

    void deletePost(String channelId, String id);

    boolean supports(String name);

    // TODO rename?
    boolean supportsPostParam(String name);

    String getPostParamName(String name);
}
