package com.indieweb.indigenous.indieweb.microsub;

import android.content.Context;
import android.content.Intent;

import com.android.volley.Request;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.Contact;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.post.BookmarkActivity;
import com.indieweb.indigenous.post.ContactActivity;
import com.indieweb.indigenous.post.LikeActivity;
import com.indieweb.indigenous.post.RepostActivity;
import com.indieweb.indigenous.reader.Reader;
import com.indieweb.indigenous.reader.ReaderBase;
import com.indieweb.indigenous.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Whitelist;
import org.jsoup.select.Elements;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.indieweb.indigenous.util.Utility.getSingleJsonValueFromArrayOrString;
import static com.indieweb.indigenous.util.Utility.checkReference;

public class IndieWebReader extends ReaderBase {

    public IndieWebReader(Context context, User user) {
        super(context, user);
    }

    @Override
    public boolean supports(String feature) {
        boolean supported = super.supports(feature);

        switch (feature) {
            case READER_CHANNEL_UNREAD:
                supported = Preferences.getPreference(getContext(), "pref_key_unread_items_channel", false);
                break;
            case READER_SOURCE_VIEW:
                supported = Preferences.getPreference(this.getContext(), "pref_key_author_timeline", false);
                break;
            case READER_MOVE_ITEM:
                supported = Preferences.getPreference(this.getContext(), "pref_key_move_item", false);
                break;
            case READER_SEARCH:
                supported = Preferences.getPreference(this.getContext(), "pref_key_search", false);
                break;
            case READER_CONTACT:
                supported = Preferences.getPreference(this.getContext(), "pref_key_contact_manage", false);
                break;
            case READER_DETAIL_CLICK:
                supported = Preferences.getPreference(this.getContext(), "pref_key_timeline_summary_detail_click", false);
                break;
        }

        return supported;
    }

    @Override
    public boolean hasEndpoint() {
        return this.getUser().getMicrosubEndpoint().length() > 0;
    }

    @Override
    public String getChannelEndpoint(boolean showSources) {
        String endpoint = this.getUser().getMicrosubEndpoint();
        if (endpoint.contains("?")) {
            endpoint += "&action=channels";
        }
        else {
            endpoint += "?action=channels";
        }

        if (showSources) {
            endpoint += "&method=tree";
        }

        return endpoint;
    }

    @Override
    public List<Channel> parseChannelResponse(String data, boolean fromCache) {
        List<Channel> Channels = new ArrayList<>();

        try {
            JSONObject object;
            JSONObject source;
            JSONObject microsubResponse = new JSONObject(data);
            JSONArray channelList = microsubResponse.getJSONArray("channels");

            int unreadChannels = 0;
            int totalUnread = 0;
            for (int i = 0; i < channelList.length(); i++) {
                object = channelList.getJSONObject(i);
                Channel channel = new Channel();
                channel.setUid(object.getString("uid"));
                channel.setName(object.getString("name"));

                int unread = 0;
                if (!fromCache && object.has("unread")) {
                    Object unreadCheck = object.get("unread");
                    if (unreadCheck instanceof Integer) {
                        unread = (Integer) unreadCheck;
                        totalUnread += unread;
                        if (unread > 0) {
                            unreadChannels++;
                        }
                    }
                    if (unreadCheck instanceof Boolean) {
                        if ((Boolean) unreadCheck) {
                            unread = -1;
                        }
                    }
                }

                channel.setUnread(unread);
                Channels.add(channel);

                // Sources.
                if (object.has("sources")) {
                    int unreadSources = 0;
                    List<Channel> Sources = new ArrayList<>();
                    JSONArray sources = object.getJSONArray("sources");

                    if (sources.length() < 2) {
                        continue;
                    }

                    for (int s = 0; s < sources.length(); s++) {
                        source = sources.getJSONObject(s);
                        Channel channelSource = new Channel();
                        channelSource.setUid(object.getString("uid"));
                        channelSource.setSourceId(source.getString("uid"));
                        String label = source.getString("url");
                        if (source.getString("name").length() > 0) {
                            label = source.getString("name");
                        }
                        channelSource.setName(label);

                        int sourceUnread = 0;
                        if (!fromCache && source.has("unread")) {
                            Object unreadCheck = source.get("unread");
                            if (unreadCheck instanceof Integer) {
                                sourceUnread = (Integer) unreadCheck;
                                if (sourceUnread > 0) {
                                    unreadSources++;
                                }
                            }
                            if (unreadCheck instanceof Boolean) {
                                if ((Boolean) unreadCheck) {
                                    unreadSources++;
                                    sourceUnread = -1;
                                }
                            }
                        }

                        channelSource.setUnread(sourceUnread);
                        Sources.add(channelSource);
                    }

                    // Set number of unread sources and add to collection.
                    for (Channel s : Sources) {
                        s.setUnreadSources(unreadSources);
                        Channels.add(s);
                    }
                }
            }

            try {
                if (getUser().isAuthenticated() && supports(Reader.READER_CHANNEL_UNREAD) && unreadChannels > 1 && totalUnread > 0) {
                    Channel channel = new Channel();
                    channel.setUid("global");
                    channel.setName(getContext().getString(R.string.channel_unread_items));
                    channel.setUnread(totalUnread);
                    Channels.add(0, channel);
                }
            }
            catch (Exception ignored) {}
        }
        catch (JSONException ignored) { }

        return Channels;
    }

    @Override
    public String getTimelineEndpoint(User user, String channelId, boolean isGlobalUnread, boolean showUnread, boolean isSourceView, String sourceId, boolean isTagView, String tag, boolean isSearch, String search, String pagerAfter) {
        String endpoint =  user.getMicrosubEndpoint();

        endpoint += "?action=timeline&channel=" + channelId;

        // Global unread.
        if (isGlobalUnread || showUnread) {
            endpoint += "&is_read=false";
        }

        // Individual timeline.
        if (isSourceView) {
            endpoint += "&source=" + sourceId;
        }

        // Pager.
        if (pagerAfter != null && pagerAfter.length() > 0) {
            endpoint += "&after=" + pagerAfter;
        }

        return endpoint;
    }

    @Override
    public int getTimelineMethod(boolean isPreview, boolean isSearch) {
        if (isPreview || isSearch) {
            return Request.Method.POST;
        }

        return Request.Method.GET;
    }

    @Override
    public Map<String, String> getTimelineParams(boolean isPreview, boolean isSearch, String channelId, String previewUrl, String searchQuery) {
        Map<String, String> params = new HashMap<>();

        if (isPreview) {
            params.put("action", "preview");
            params.put("url", previewUrl);
        }

        if (isSearch) {
            params.put("action", "search");
            params.put("channel", channelId);
            params.put("query", searchQuery);
        }

        return params;
    }

    @Override
    public List<TimelineItem> parseTimelineResponse(String response, String channelId, String channelName, List<String> entries, boolean isGlobalUnread, boolean isSearch, boolean recursiveReference, String[] olderItems, Context context) {
        JSONObject object;
        JSONArray itemList;
        List<TimelineItem> TimelineItems = new ArrayList<>();

        try {
            JSONObject microsubResponse = new JSONObject(response);
            itemList = microsubResponse.getJSONArray("items");

            // Paging. Can be empty.
            if (microsubResponse.has("paging")) {
                try {
                    if (microsubResponse.getJSONObject("paging").has("after")) {
                        olderItems[0] = microsubResponse.getJSONObject("paging").getString("after");
                    }
                    else {
                        olderItems[0] = "";
                    }
                }
                catch (JSONException ignored) {
                    olderItems[0] = "";
                }
            }

            for (int i = 0; i < itemList.length(); i++) {
                object = itemList.getJSONObject(i);
                TimelineItem item = new TimelineItem();
                item.setJson(itemList.getString(i));

                boolean addContent = true;
                boolean isRead = false;
                String type = "entry";
                String url = "";
                String name = "";
                String textContent = "";
                String htmlContent = "";
                String authorName = "";

                // Type.
                if (object.has("type")) {
                    type = object.getString("type");
                }

                // Ignore 'card' type.
                if (type.equals("card")) {
                    continue;
                }

                // It's possible that _id is empty. Don't let readers choke on it.
                try {
                    item.setId(object.getString("_id"));
                } catch (Exception ignored) {}

                // Source id is experimental.
                if (object.has("_source")) {
                    try {
                        item.setSourceId(object.getString("_source"));
                    } catch (Exception ignored) {}
                }

                // Is read.
                if (object.has("_is_read")) {
                    isRead = object.getBoolean("_is_read");
                }
                item.setRead(isRead);
                if (!item.isRead() && item.getId() != null) {
                    entries.add(item.getId());
                }

                // Channel name and id.
                item.setChannelId(channelId);
                if (object.has("_channel") && isGlobalUnread) {
                    try {
                        String itemChannelName = object.getJSONObject("_channel").getString("name");
                        item.setChannelName(itemChannelName);
                    }
                    catch (Exception ignored) {}
                }
                else if (channelName != null && channelName.length() > 0) {
                    item.setChannelName(channelName);
                }

                // Author.
                if (object.has("author")) {

                    JSONObject author = object.getJSONObject("author");
                    if (author.has("name")) {
                        authorName = author.getString("name");
                    }
                    String authorUrl = "";
                    if (author.has("url")) {
                        authorUrl = author.getString("url");
                        item.setAuthorUrl(authorUrl);
                    }
                    if (authorName.equals("null") && authorUrl.length() > 0) {
                        authorName = authorUrl;
                    }

                    if (author.has("photo")) {
                        String authorPhoto = author.getString("photo");
                        if (!authorPhoto.equals("null") && authorPhoto.length() > 0) {
                            item.setAuthorPhoto(authorPhoto);
                        }
                    }
                }
                item.setAuthorName(authorName);

                // In reply to.
                if (object.has("in-reply-to")) {
                    type = "in-reply-to";
                    String value = getSingleJsonValueFromArrayOrString(type, object);
                    if (value.length() > 0) {
                        item.addToResponseType(type, value);
                        checkReference(object, value, item, false, false, 0, context);
                    }
                }

                // Follow-of.
                if (object.has("follow-of")) {
                    type = "follow-of";
                    textContent = context.getString(R.string.started_following);
                }

                // Repost.
                if (object.has("repost-of")) {
                    type = "repost-of";
                    addContent = false;
                    String value = getSingleJsonValueFromArrayOrString(type, object);
                    if (value.length() > 0) {
                        item.addToResponseType(type, value);
                        checkReference(object, value, item, true, recursiveReference, 0, context);
                    }
                }

                // Quotation of.
                if (object.has("quotation-of")) {
                    type = "quotation-of";
                    String value = getSingleJsonValueFromArrayOrString(type, object);
                    if (value.length() > 0) {
                        item.addToResponseType(type, value);
                        checkReference(object, value, item, true, false, 0, context);
                    }
                }

                // Like.
                if (object.has("like-of")) {
                    type = "like-of";
                    addContent = false;
                    String value = getSingleJsonValueFromArrayOrString(type, object);
                    if (value.length() > 0) {
                        item.addToResponseType(type, value);
                        checkReference(object, value, item, false, false, 0, context);
                    }
                }

                // Bookmark.
                if (object.has("bookmark-of")) {
                    type = "bookmark-of";
                    addContent = false;
                    String value = getSingleJsonValueFromArrayOrString(type, object);
                    if (value.length() > 0) {
                        item.addToResponseType(type, value);
                        checkReference(object, value, item, false, false, 0, context);
                    }
                }

                // A checkin.
                if (object.has("checkin")) {
                    type = "checkin";
                    item.addToResponseType(type, object.getJSONObject("checkin").getString("name"));
                    String checkinUrl = "";
                    try {
                        checkinUrl = object.getJSONObject("checkin").getString("url");
                    }
                    catch (Exception ignored) {}
                    item.addToResponseType("checkin-url", checkinUrl);

                    try {
                        item.setLatitude(object.getJSONObject("checkin").getString("latitude"));
                        item.setLongitude(object.getJSONObject("checkin").getString("longitude"));
                    }
                    catch (Exception ignored) {}

                }

                // Location.
                if (object.has("location")) {
                    String location = getSingleJsonValueFromArrayOrString("location", object);
                    item.setLocation(location);
                }

                // Set type.
                item.setType(type);

                // Url.
                if (object.has("url")) {
                    url = object.getString("url");
                }
                item.setUrl(url);

                // Published
                String published = "";
                if (object.has("published")) {
                    published = object.getString("published");
                }
                item.setPublished(published);

                // Start
                String start = "";
                if (object.has("start")) {
                    start = object.getString("start");
                }
                item.setStart(start);

                // End
                String end = "";
                if (object.has("end")) {
                    end = object.getString("end");
                }
                item.setEnd(end);

                // Content.
                if (object.has("content") && addContent) {
                    JSONObject content = object.getJSONObject("content");

                    if (content.has("text")) {
                        addContent = false;
                        textContent = content.getString("text");
                    }

                    if (content.has("html")) {
                        addContent = false;
                        htmlContent = content.getString("html");

                        // Clean html, remove images and put them in photo.
                        try {
                            Document doc = Jsoup.parse(htmlContent);
                            Elements imgs = doc.select("img");
                            for (Element img : imgs) {
                                String photoUrl = img.absUrl("src");
                                if (!photoUrl.contains("spacer.gif") && !photoUrl.contains("spacer.png")) {
                                    item.addPhoto(photoUrl);
                                }
                            }
                            htmlContent = Jsoup.clean(htmlContent, Whitelist.basic());
                        }
                        catch (Exception ignored) {}
                    }

                }
                else if (object.has("summary") && addContent) {
                    addContent = false;
                    textContent = object.getString("summary");
                }

                // RSVP
                if (object.has("rsvp") && addContent) {
                    try {
                        textContent = "RSVP: " + object.getString("rsvp");
                    }
                    catch (JSONException ignored) {}
                }

                // Name.
                if (object.has("name")) {
                    name = object.getString("name").replace("\n", "").replace("\r", "");
                    if (name.equals(textContent)) {
                        name = "";
                    }
                }
                else if (object.has("summary") && addContent) {
                    name = object.getString("summary").replace("\n", "").replace("\r", "");
                }

                // Photos.
                if (object.has("photo")) {
                    try {
                        Object photoObject = object.get("photo");
                        if (photoObject instanceof JSONArray) {
                            JSONArray photos = object.getJSONArray("photo");
                            for (int p = 0; p < photos.length(); p++) {
                                item.addPhoto(photos.getString(p));
                            }
                        }
                        else {
                            item.addPhoto(object.getString("photo"));
                        }
                    }
                    catch (JSONException ignored) {}
                }

                // Audio.
                if (object.has("audio")) {
                    String audio = getSingleJsonValueFromArrayOrString("audio", object);
                    item.setAudio(audio);
                }

                // Video.
                if (object.has("video")) {
                    String video = getSingleJsonValueFromArrayOrString("video", object);
                    item.setVideo(video);
                }

                // Detect YouTube video.
                if (item.getUrl().length() > 0 && item.getUrl().contains("youtube.com")) {
                    item.setVideo(item.getUrl());
                }

                //Log.d("indigenous_debug", "text before: " + item.getTextContent());
                //Log.d("indigenous_debug", "html before: " + item.getHtmlContent());
                //Log.d("indigenous_debug", "reference before: " + item.getReference());

                // Swap reference and content if content is empty.
                if (item.swapReference() && Preferences.getPreference(context, "pref_key_timeline_author_original", false) && textContent.length() == 0 && htmlContent.length() == 0 && item.getReference().length() > 0) {
                    //Log.d("indigenous_debug", "swapping ref / content");
                    item.setTextContent(item.getReference());
                    item.setReference("");
                }

                // Set values of name, text and html content.
                item.setName(name);
                if (item.getTextContent().length() == 0) {
                    item.setTextContent(textContent);
                }
                item.setHtmlContent(htmlContent);

                //Log.d("indigenous_debug", "text after: " + item.getTextContent());
                //Log.d("indigenous_debug", "html after: " + item.getHtmlContent());
                //Log.d("indigenous_debug", "reference after: " + item.getReference());

                TimelineItems.add(item);
            }
        }
        catch (JSONException ignored) {}

        return TimelineItems;
    }

    @Override
    public void markRead(String channelId, List<String> entries, boolean markAllRead, boolean showMessage) {
        new MicrosubAction(this.getContext(), this.getUser(), null).markRead(channelId, entries, markAllRead, false);
    }

    @Override
    public boolean doResponse(TimelineItem item, String response) {
        Intent i;
        switch (response) {
            case RESPONSE_LIKE:
                i = new Intent(getContext(), LikeActivity.class);
                i.putExtra("incomingText", item.getUrl());
            break;
            case RESPONSE_BOOKMARK:
                i = new Intent(getContext(), BookmarkActivity.class);
                i.putExtra("incomingText", item.getUrl());
                break;
            case RESPONSE_CONTACT:
                Indigenous app2 = Indigenous.getInstance();
                Contact contact = new Contact();
                contact.setName(item.getAuthorName());
                if (item.getAuthorPhoto().length() > 0) {
                    contact.setPhoto(item.getAuthorPhoto());
                }
                if (item.getAuthorUrl().length() > 0) {
                    contact.setUrl(item.getAuthorUrl());
                }

                app2.setContact(contact);
                i =  new Intent(getContext(), ContactActivity.class);
                i.putExtra("addContact", true);
                break;
            case RESPONSE_REPOST:
            default:
                i = new Intent(getContext(), RepostActivity.class);
                i.putExtra("incomingText", item.getUrl());
                break;
        }

        getContext().startActivity(i);
        return false;
    }

    @Override
    public String getReplyId(TimelineItem item) {
        return item.getUrl();
    }
}
