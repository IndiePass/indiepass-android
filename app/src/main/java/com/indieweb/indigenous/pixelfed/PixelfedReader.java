package com.indieweb.indigenous.pixelfed;

import android.content.Context;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.reader.ReaderBase;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class PixelfedReader extends ReaderBase {

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
                supported = false;
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
        c.setUid("pixelfed_home");
        c.setUnread(0);
        Channels.add(c);
        c = new Channel();
        c.setName(getContext().getString(R.string.channel_public));
        c.setUid("pixelfed_public");
        c.setUnread(0);
        Channels.add(c);
        if (this.getUser().isAuthenticated()) {
            c = new Channel();
            c.setName(getContext().getString(R.string.channel_my_posts));
            c.setUid("pixelfed_my_posts");
            c.setUnread(0);
            Channels.add(c);
            c = new Channel();
            c.setName(getContext().getString(R.string.channel_favourites));
            c.setUid("pixelfed_favourites");
            c.setUnread(0);
            Channels.add(c);
            c = new Channel();
            c.setName(getContext().getString(R.string.channel_bookmarks));
            c.setUid("pixelfed_bookmarks");
            c.setUnread(0);
            Channels.add(c);
        }
        return Channels;
    }

    @Override
    public List<Channel> getAdditionalChannels() {
        List<Channel> Channels = new ArrayList<>();

        if (getUser().isAnonymous()) {
            Channel channel = new Channel();
            channel.setUid("indigenous_pixelfed");
            channel.setName(getContext().getString(R.string.channel_pixelfed));
            channel.setUnread(0);
            Channels.add(channel);
        }

        return Channels;
    }

    @Override
    public boolean hideDelete(String channelId) {
        return !channelId.equals("pixelfed_my_posts");
    }

    @Override
    public String getTimelineEndpoint(User user, String channelId, boolean isGlobalUnread, boolean showUnread, boolean isSourceView, String sourceId, String pagerAfter) {
        String endpoint;
        if (channelId.equals("anonymous_pixelfed")) {
            endpoint = "https://pixelfed.social/api/v1/timelines/public";
        }
        // TODO works anonymous to?
        else if (isSourceView && sourceId != null) {
            endpoint = this.getUser().getMe() + "/api/v1/accounts/" + sourceId + "/statuses";
        }
        else {
            switch (channelId) {
                case "pixelfed_home":
                    endpoint = this.getUser().getMe() + "/api/v1/timelines/home";
                    break;
                case "pixelfed_favourites":
                    endpoint = this.getUser().getMe() + "/api/v1/favourites";
                    break;
                case "pixelfed_bookmarks":
                    endpoint = this.getUser().getMe() + "/api/v1/bookmarks";
                    break;
                case "pixelfed_my_posts":
                    endpoint = this.getUser().getMe() + "/api/v1/accounts/" + this.getUser().getExternalId() + "/statuses";
                    break;
                case "pixelfed_public":
                default:
                    endpoint = this.getUser().getMe() + "/api/v1/timelines/public";
                    break;
            }
        }

        return endpoint;
    }

    @Override
    public List<TimelineItem> parseTimelineResponse(String response, String channelId, String channelName, List<String> entries, boolean isGlobalUnread, boolean recursiveReference, String[] olderItems, Context context) {
        JSONObject object;
        List<TimelineItem> TimelineItems = new ArrayList<>();

        try {
            JSONArray itemList = new JSONArray(response);

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
                            // TODO use preview url?
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
                }
                item.setAuthorName(authorName);

                TimelineItems.add(item);
            }

        }
        catch (JSONException ignored) { }

        return TimelineItems;
    }

}
