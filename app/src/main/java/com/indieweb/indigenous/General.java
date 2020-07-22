package com.indieweb.indigenous;

import java.util.ArrayList;

public interface General {

    String FEATURE_CHANNELS_REFRESH = "channels_refresh";
    String FEATURE_CHANNELS_MANAGE = "channels_manage";
    String FEATURE_CHANNELS_SHOW_SOURCES = "channels_show_sources";
    String FEATURE_CHANNELS_HIDE_READ = "channels_hide_read";
    String FEATURE_CHANNELS_READ_LATER = "channels_read_later";
    String FEATURE_HIDE_POST_TYPES = "hide_posts_types";
    String FEATURE_POSTS = "posts";
    String FEATURE_UPLOAD = "upload";
    String FEATURE_CONTACTS = "contacts";

    /**
     * Whether the app supports a certain general feature.
     *
     * @param feature
     *   The feature to check.
     *
     * @return boolean
     */
    boolean supports(String feature);

    /**
     * Handle the post action button click.
     */
    void handlePostActionButtonClick();

    /**
     * Handle the write post button click.
     */
    void handleWritePostClick();

    /**
     * Whether to hide post types on the share activity.
     *
     * @return boolean
     */
    boolean hidePostTypes();

    /**
     * Return protected post types.
     *
     * @return ArrayList
     */
    ArrayList<Integer> getProtectedPostTypes();
}
