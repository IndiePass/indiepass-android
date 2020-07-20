package com.indieweb.indigenous.pixelfed;

import android.content.Context;
import android.view.MenuItem;

import com.android.volley.Request;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.reader.ReaderBase;
import com.indieweb.indigenous.util.HTTPRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PixelfedReader extends ReaderBase {

    public static final int LIMIT = 20;
    public static final String CHANNEL_NAME_PIXELFED_ANONYMOUS = "pixelfed_anonymous";
    public static final String CHANNEL_NAME_HOME = "pixelfed_home";
    public static final String CHANNEL_NAME_PUBLIC = "pixelfed_public";
    public static final String CHANNEL_NAME_MY_POSTS = "pixelfed_my_posts";
    public static final String CHANNEL_NAME_FAVOURITES = "pixelfed_favourites";
    public static final String CHANNEL_NAME_BOOKMARKS = "pixelfed_bookmarks";

    public PixelfedReader(Context context, User user) {
        super(context, user);
    }

    @Override
    public boolean supports(String feature) {
        boolean supported = true;

        switch (feature) {
            case READER_CHANNEL_CAN_CACHE:
            case READER_CHANNEL_UNREAD:
            case READER_MARK_READ:
            case READER_DEBUG_CHANNELS:
            case READER_MOVE_ITEM:
            case READER_SEARCH:
            case READER_DETAIL_CLICK:
                supported = false;
                break;
            case READER_SOURCE_VIEW:
            case RESPONSE_LIKE:
            case RESPONSE_REPOST:
            case RESPONSE_BOOKMARK:
                supported = getUser().isAuthenticated();
                break;
        }

        return supported;
    }

    @Override
    public List<Channel> getChannels() {
        List<Channel> Channels = new ArrayList<>();
        Channel c;
        c = new Channel();
        c.setName(getContext().getString(R.string.channel_home));
        c.setUid(CHANNEL_NAME_HOME);
        c.setUnread(0);
        Channels.add(c);
        c = new Channel();
        c.setName(getContext().getString(R.string.channel_public));
        c.setUid(CHANNEL_NAME_PUBLIC);
        c.setUnread(0);
        Channels.add(c);
        if (this.getUser().isAuthenticated()) {
            c = new Channel();
            c.setName(getContext().getString(R.string.channel_my_posts));
            c.setUid(CHANNEL_NAME_MY_POSTS);
            c.setUnread(0);
            Channels.add(c);
            c = new Channel();
            c.setName(getContext().getString(R.string.channel_favourites));
            c.setUid(CHANNEL_NAME_FAVOURITES);
            c.setUnread(0);
            Channels.add(c);
            // Disabled until https://github.com/pixelfed/pixelfed/issues/2317 is fixed
            /*c = new Channel();
            c.setName(getContext().getString(R.string.channel_bookmarks));
            c.setUid(CHANNEL_NAME_BOOKMARKS");
            c.setUnread(0);
            Channels.add(c);*/
        }
        return Channels;
    }

    @Override
    public boolean hideDelete(String channelId) {
        return !channelId.equals(CHANNEL_NAME_MY_POSTS);
    }

    @Override
    public String getTimelineEndpoint(User user, String channelId, boolean isGlobalUnread, boolean showUnread, boolean isSourceView, String sourceId, boolean isTagView, String tag, boolean isSearch, String search, String pagerAfter) {
        String endpoint;

        if (channelId.equals(CHANNEL_NAME_PIXELFED_ANONYMOUS)) {
            endpoint = "https://pixelfed.social/api/v1/timelines/public?limit=" + LIMIT;
        }
        else if (isSourceView && sourceId != null) {
            endpoint = this.getUser().getMe() + "/api/v1/accounts/" + sourceId + "/statuses";
        }
        else if (isSearch && search.length() > 0) {
            endpoint = this.getUser().getMe() + "/api/v2/search?q=" + search;
        }
        else {
            switch (channelId) {
                case CHANNEL_NAME_HOME:
                    endpoint = this.getUser().getMe() + "/api/v1/timelines/home";
                    break;
                case CHANNEL_NAME_FAVOURITES:
                    endpoint = this.getUser().getMe() + "/api/v1/favourites";
                    break;
                case CHANNEL_NAME_BOOKMARKS:
                    endpoint = this.getUser().getMe() + "/api/v1/bookmarks";
                    break;
                case CHANNEL_NAME_MY_POSTS:
                    endpoint = this.getUser().getMe() + "/api/v1/accounts/" + this.getUser().getExternalId() + "/statuses";
                    break;
                case CHANNEL_NAME_PUBLIC:
                default:
                    endpoint = this.getUser().getMe() + "/api/v1/timelines/public";
                    break;
            }

            endpoint += "?limit=" + LIMIT;
        }

        if (pagerAfter != null && pagerAfter.length() > 0) {
            endpoint += "&max_id=" + pagerAfter;
        }

        return endpoint;
    }

    @Override
    public int getTimelineMethod(boolean isPreview, boolean isSearch) {
        return Request.Method.GET;
    }

    @Override
    public boolean sendTimelineAccessToken(String channelId) {
        boolean send = true;
        if (channelId.equals(CHANNEL_NAME_PIXELFED_ANONYMOUS)) {
            send = false;
        }
        return send;
    }

    @Override
    public List<TimelineItem> parseTimelineResponse(String response, String channelId, String channelName, List<String> entries, boolean isGlobalUnread, boolean isSearch, boolean recursiveReference, String[] olderItems, Context context) {
        JSONObject object;
        String maxId = "";
        List<TimelineItem> TimelineItems = new ArrayList<>();

        try {

            JSONArray itemList;
            if (isSearch) {
                JSONObject r = new JSONObject(response);
                itemList = new JSONArray(r.get("statuses"));
            }
            else {
                itemList = new JSONArray(response);
            }

            for (int i = 0; i < itemList.length(); i++) {
                object = itemList.getJSONObject(i);
                TimelineItem item = new TimelineItem();
                item.setJson(itemList.getString(i));

                String type = "entry";
                String url = "";
                String name = "";
                String authorName = "";

                item.setType(type);
                item.setName(name);
                item.setHtmlContent(object.getString("content"));
                item.setTextContent("");
                item.setReference("");
                item.setChannelId(channelId);
                item.setNumberOfComments(object.getInt("replies_count"));

                // Published
                String published = "";
                if (object.has("created_at")) {
                    published = object.getString("created_at");
                }
                item.setPublished(published);
                item.setRead(true);

                // It's possible that _id is empty. Don't let readers choke on it.
                try {
                    item.setId(object.getString("id"));
                } catch (Exception ignored) {}

                // Url.
                if (object.has("uri")) {
                    url = object.getString("uri");
                }
                item.setUrl(url);

                // Source id
                if (object.has("account")) {
                    try {
                        item.setSourceId(object.getJSONObject("account").getString("id"));
                    } catch (Exception ignored) {}
                }

                // Photos.
                if (object.has("media_attachments")) {
                    try {
                        JSONArray photos = object.getJSONArray("media_attachments");
                        for (int p = 0; p < photos.length(); p++) {
                            item.addPhoto(photos.getJSONObject(p).getString("url"));
                        }
                    }
                    catch (JSONException ignored) { }
                }

                // Author.
                if (object.has("account")) {
                    JSONObject author = object.getJSONObject("account");
                    if (author.has("display_name")) {
                        authorName = author.getString("display_name");
                    }
                    String authorUrl = "";
                    if (author.has("url")) {
                        authorUrl = author.getString("url");
                        item.setAuthorUrl(authorUrl);
                    }
                    if (authorName.equals("null") && authorUrl.length() > 0) {
                        authorName = authorUrl;
                    }

                    if (author.has("avatar")) {
                        String authorPhoto = author.getString("avatar");
                        if (!authorPhoto.equals("null") && authorPhoto.length() > 0) {
                            item.setAuthorPhoto(authorPhoto);
                        }
                    }

                    item.setAuthorId(author.getString("id"));
                }
                item.setAuthorName(authorName);

                // Responses.
                item.setLiked(object.getBoolean("favourited"));
                item.setBookmarked(object.getBoolean("bookmarked"));
                item.setReposted(object.getBoolean("reblogged"));

                maxId = item.getId();
                TimelineItems.add(item);
            }

            if (itemList.length() == LIMIT) {
                olderItems[0] = maxId;
            }

        }
        catch (JSONException ignored) { }

        return TimelineItems;
    }

    @Override
    public boolean doResponse(TimelineItem item, String response) {
        boolean isActive = false;
        String appUrl = null;

        switch (response) {
            case RESPONSE_LIKE:
                if (item.isLiked()) {
                    appUrl = getUser().getMe() + "/api/v1/statuses/" + item.getId() + "/unfavourite";
                }
                else {
                    appUrl = getUser().getMe() + "/api/v1/statuses/" + item.getId() + "/favourite";
                    isActive = true;
                }
                break;
            case RESPONSE_BOOKMARK:
                if (item.isBookmarked()) {
                    appUrl = getUser().getMe() + "/api/v1/statuses/" + item.getId() + "/unbookmark";
                }
                else {
                    appUrl = getUser().getMe() + "/api/v1/statuses/" + item.getId() + "/bookmark";
                    isActive = true;
                }
                break;
            case RESPONSE_REPOST:
                if (item.isReposted()) {
                    appUrl = getUser().getMe() + "/api/v1/statuses/" + item.getId() + "/unreblog";
                }
                else {
                    appUrl = getUser().getMe() + "/api/v1/statuses/" + item.getId() + "/reblog";
                    isActive = true;
                }
                break;
            case RESPONSE_CONTACT:
                if (item.getChannelId().equals(CHANNEL_NAME_HOME)) {
                    appUrl = getUser().getMe() + "/api/v1/accounts/" + item.getAuthorId() + "/unfollow";
                }
                else {
                    appUrl = getUser().getMe() + "/api/v1/accounts/" + item.getAuthorId() + "/follow";
                }
                break;
        }

        if (appUrl != null) {
            HTTPRequest r = new HTTPRequest(null, getUser(), getContext());
            r.doPostRequest(appUrl, null);
        }

        return isActive;
    }

    @Override
    public boolean canContact(String channelId) {
        return !channelId.equals(CHANNEL_NAME_MY_POSTS);
    }

    @Override
    public void setContactLabel(MenuItem menuItem, TimelineItem item) {
        if (item.getChannelId().equals(CHANNEL_NAME_HOME)) {
            menuItem.setTitle(getContext().getString(R.string.unfollow));
        }
        else {
            menuItem.setTitle(getContext().getString(R.string.follow));
        }
    }

    @Override
    public String getReplyId(TimelineItem item) {
        return item.getId();
    }



}
