package com.indieweb.indiepass.post;

import android.widget.MultiAutoCompleteTextView;

import com.android.volley.NetworkResponse;

public interface Post {

    String FEATURE_TITLE = "FEATURE_POST_TITLE";
    String FEATURE_CATEGORIES = "FEATURE_POST_CATEGORIES";
    String FEATURE_CONTACTS = "FEATURE_POST_CONTACTS";
    String FEATURE_AUDIO = "FEATURE_POST_AUDIO";
    String FEATURE_LOCATION = "FEATURE_POST_LOCATION";
    String FEATURE_POST_STATUS = "FEATURE_POST_STATUS";
    String FEATURE_POST_SENSITIVITY = "FEATURE_POST_SENSITIVITY";
    String FEATURE_MEDIA_UPLOAD_DESCRIPTION = "FEATURE_MEDIA_UPLOAD_DESCRIPTION";
    String FEATURE_SPOILER = "FEATURE_POST_SPOILER";
    String POST_PARAM_H = "h";
    String POST_PARAM_PUBLISHED = "published";
    String POST_PARAM_CONTENT = "content";
    String POST_PARAM_POST_STATUS = "post-status";
    String POST_PARAM_REPLY = "in-reply-to";
    String POST_PARAM_PHOTO = "photo";
    String POST_PARAM_VIDEO = "video";

    /**
     * Get post endpoint
     *
     * @param isMediaRequest
     *   Whether this is a media request or not.
     *
     * @return String
     */
    String getEndpoint(boolean isMediaRequest);

    /**
     * Get file from the media response.
     *
     * @param response
     *   The current response
     *
     * @return String
     */
    String getFileFromMediaResponse(NetworkResponse response);

    /**
     * Get the main post title.
     *
     * @return String
     */
    String getMainPostTitle();

    /**
     * Whether to use the media endpoint or not to upload.
     *
     * @return boolean
     */
    boolean useMediaEndpoint();

    /**
     * Delete a post.
     *
     * @param channelId
     *   The current channel id.
     * @param id
     *   The post id
     */
    void deletePost(String channelId, String id);

    /**
     * Whether the post supports a certain feature.
     *
     * @param feature
     *   The feature to check
     *
     * @return boolean
     */
    boolean supports(String feature);

    /**
     * Whether a post param is supported or not
     *
     * @param name
     *   The post parameter name

     * @return boolean
     */
    boolean supportsPostParam(String name);

    /**
     * Get the name of a post param.
     *
     * @param name
     *   The name of the post param. Defaults to the one used by IndieWeb
     *
     * @return String
     */
    String getPostParamName(String name);

    /**
     * Prepare the autocomplete tags field.
     *
     * @param tags
     *   The tags field
     */
    void prepareTagsAutocomplete(MultiAutoCompleteTextView tags);

    /**
     * Prepare the autocomplete body field.
     *
     * @param body
     *   The body field
     */
    void prepareContactsAutocomplete(MultiAutoCompleteTextView body);

    /**
     * Whether to hide the URL field in the reply post screen.
     *
     * @return boolean
     */
    boolean hideUrlField();

    /**
     * Whether the anonymous account can not post.
     *
     * @return boolean
     */
    boolean canNotPostAnonymous();

    /**
     * Return the message in case the anonymous user can not post.
     *
     * @return String
     */
    String anonymousPostMessage();
}
