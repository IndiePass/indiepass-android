package com.indieweb.indigenous.reader;

import android.content.Context;

import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;

import java.util.List;

public interface Reader {

    String READER_CHANNEL_CAN_CACHE = "reader_channel_can_cache";
    String READER_CHANNEL_UNREAD = "reader_channel_unread";
    String READER_SOURCE_VIEW = "reader_author_view";
    String READER_MOVE_ITEM = "reader_move_item";
    String READER_MARK_READ = "reader_mark_read";
    String READER_DEBUG_CHANNELS = "reader_debug_channels";

    /**
     * Whether to hide the delete button.
     *
     * @param channelId
     *   The current channel id.
     *
     * @return boolean
     */
    boolean hideDelete(String channelId);

    /**
     * Whether the reader supports a certain feature.
     *
     * @param feature
     *   The feature to check.
     *
     * @return boolean
     */
    boolean supports(String feature);

    /**
     * Whether there's a reader endpoint.
     *
     * @return boolean
     */
    boolean hasEndpoint();

    /**
     * Get the channel endpoint
     *
     * @param showSources
     *   Whether to show the sources or not
     *
     * @return string
     */
    String getChannelEndpoint(boolean showSources);

    /**
     * Get list of channels.
     *
     * @return List<Channel>
     */
    List<Channel> getChannels();

    /**
     * Get list of additional channels.
     *
     * @return List<Channel>
     */
    List<Channel> getAdditionalChannels();

    /**
     * Parse channel response
     *
     * @param data
     *   The data
     * @param fromCache
     *   Whether the response came from cache.
     *
     * @return List<Channel>
     */
    List<Channel> parseChannelResponse(String data, boolean fromCache);

    /**
     * Get the timeline endpoint.
     *
     * @param user
     *   The current user
     * @param channelId
     *   The current channel id
     * @param isGlobalUnread
     *   Whether this is the global unread
     * @param showUnread
     *   Whether to show unread
     * @param isSourceView
     *   Whether this is the source view
     * @param sourceId
     *   The current source id
     * @param pagerAfter
     *   The pager after
     *
     * @return String
     */
    String getTimelineEndpoint(User user, String channelId, boolean isGlobalUnread, boolean showUnread, boolean isSourceView, String sourceId, String pagerAfter);

    /**
     * Parse a timeline response.
     *
     * @param response
     *   The response to parse
     * @param channelId
     *   The current channel id
     * @param channelName
     *   The current channel name
     * @param entries
     *   Entries to mark read
     * @param isGlobalUnread
     *   Whether this is the global unread
     * @param recursiveReference
     *   Whether to check recursive reference
     * @param olderItems
     *   Paging or not.
     * @param context
     *   The current context.
     *
     * @return List<TimelineItem>
     */
    List<TimelineItem> parseTimelineResponse(String response, String channelId, String channelName, List<String> entries, boolean isGlobalUnread, boolean recursiveReference, String[] olderItems, Context context);

    /**
     * Mark items read.
     *
     * @param channelId
     *   The current channel id
     * @param entries
     *   The entries to mark read
     * @param markAllRead
     *   Whether to mark everything read
     * @param showMessage
     *   Whether to show a snackbar message.
     */
    void markRead(String channelId, List<String> entries, boolean markAllRead, boolean showMessage);
}
