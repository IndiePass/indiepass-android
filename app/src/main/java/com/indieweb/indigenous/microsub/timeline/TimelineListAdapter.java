package com.indieweb.indigenous.microsub.timeline;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.appcompat.widget.PopupMenu;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.general.DebugActivity;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.post.BookmarkActivity;
import com.indieweb.indigenous.micropub.post.ContactActivity;
import com.indieweb.indigenous.micropub.post.LikeActivity;
import com.indieweb.indigenous.micropub.post.ReadActivity;
import com.indieweb.indigenous.micropub.post.ReplyActivity;
import com.indieweb.indigenous.micropub.post.RepostActivity;
import com.indieweb.indigenous.micropub.post.RsvpActivity;
import com.indieweb.indigenous.microsub.MicrosubAction;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.Contact;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;
import com.indieweb.indigenous.widget.ExpandableTextView;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.indieweb.indigenous.microsub.timeline.TimelineActivity.MARK_READ_CHANNEL_CLICK;
import static com.indieweb.indigenous.microsub.timeline.TimelineActivity.MARK_READ_MANUAL;
import static com.indieweb.indigenous.model.TimelineStyle.TIMELINE_STYLE_COMPACT;
import static com.indieweb.indigenous.model.TimelineStyle.TIMELINE_STYLE_SUMMARY;
import static com.indieweb.indigenous.util.Utility.dateFormatStrings;

/**
 * Timeline items list adapter.
 */
public class TimelineListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<TimelineItem> items;
    private LayoutInflater mInflater;
    private boolean debugItemJSON;
    private boolean isSourceView;
    private final User user;
    private final String channelId;
    private final String channelName;
    private final ListView listView;
    private final int Style;
    private final RelativeLayout layout;

    TimelineListAdapter(Context context, List<TimelineItem> items, User user, String channelId, String channelName, ListView listView, boolean isSourceView, int style, RelativeLayout layout) {
        this.context = context;
        this.items = items;
        this.user = user;
        this.channelId = channelId;
        this.channelName = channelName;
        this.listView = listView;
        this.isSourceView = isSourceView;
        this.Style = style;
        this.layout = layout;
        this.debugItemJSON = Preferences.getPreference(context, "pref_key_debug_microsub_item_json", false);
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return items.size();
    }

    public TimelineItem getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void onClick(View view) {}

    public static class ViewHolder {
        public int position;
        public TextView meta;
        public TextView unread;
        public TextView channel;
        public TextView author;
        public ImageView authorPhoto;
        public TextView name;
        public TextView reference;
        public TextView response;
        public TextView published;
        public Button expand;
        public ExpandableTextView content;
        public ImageView image;
        public TextView imageCount;
        public CardView card;
        public LinearLayout row;
        public TextView start;
        public TextView end;
        public TextView location;
        public Button reply;
        public Button like;
        public Button repost;
        public Button bookmark;
        public Button read;
        public Button audio;
        public Button video;
        public Button external;
        public Button rsvp;
        public Button map;
        public Button menu;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {

            if (Style == TIMELINE_STYLE_COMPACT) {
                convertView = mInflater.inflate(R.layout.list_item_timeline_compact, null);
            }
            else {
                convertView = mInflater.inflate(R.layout.list_item_timeline, null);
            }

            holder = new ViewHolder();
            holder.name = convertView.findViewById(R.id.timeline_name);
            holder.meta = convertView.findViewById(R.id.timeline_meta);
            holder.row = convertView.findViewById(R.id.timeline_item_row);
            holder.reply = convertView.findViewById(R.id.itemReply);
            holder.bookmark = convertView.findViewById(R.id.itemBookmark);
            holder.read = convertView.findViewById(R.id.itemRead);
            holder.like = convertView.findViewById(R.id.itemLike);
            holder.repost = convertView.findViewById(R.id.itemRepost);
            holder.audio = convertView.findViewById(R.id.itemAudio);
            holder.video = convertView.findViewById(R.id.itemVideo);
            holder.external = convertView.findViewById(R.id.itemExternal);
            holder.rsvp = convertView.findViewById(R.id.itemRSVP);
            holder.menu = convertView.findViewById(R.id.itemMenu);
            holder.map = convertView.findViewById(R.id.itemMap);
            holder.image = convertView.findViewById(R.id.timeline_image);
            holder.card = convertView.findViewById(R.id.timeline_card);

            // Summary version.
            if (Style == TIMELINE_STYLE_SUMMARY) {
                holder.unread = convertView.findViewById(R.id.timeline_new);
                holder.published = convertView.findViewById(R.id.timeline_published);
                holder.start = convertView.findViewById(R.id.timeline_start);
                holder.end = convertView.findViewById(R.id.timeline_end);
                holder.location = convertView.findViewById(R.id.timeline_location);
                holder.author = convertView.findViewById(R.id.timeline_author);
                holder.imageCount = convertView.findViewById(R.id.timeline_image_count);
                holder.channel = convertView.findViewById(R.id.timeline_channel);
                holder.authorPhoto = convertView.findViewById(R.id.timeline_author_photo);
                holder.reference = convertView.findViewById(R.id.timeline_reference);
                holder.response = convertView.findViewById(R.id.timeline_response);
                holder.content = convertView.findViewById(R.id.timeline_content);
                holder.expand = convertView.findViewById(R.id.timeline_content_more);
            }

            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final TimelineItem item = items.get(position);
        if (item != null) {

            holder.position = position;

            // Color of row.
            int color = context.getResources().getColor(R.color.listRowBackgroundColor);
            holder.row.setBackgroundColor(color);

            // Published.
            SimpleDateFormat formatOut;
            if (Style == TIMELINE_STYLE_COMPACT) {
                formatOut = new SimpleDateFormat("dd MM yyyy");
            }
            else {
                formatOut = new SimpleDateFormat("dd MM yyyy HH:mm");
            }

            Date dateResult = null;
            for (String formatString : dateFormatStrings) {
                try {
                    if (dateResult != null) {
                        break;
                    }
                    dateResult = new SimpleDateFormat(formatString).parse(item.getPublished());
                }
                catch (ParseException ignored) {}
            }

            // Author.
            String author = "";
            if (item.getAuthorName().length() > 0) {
                author = item.getAuthorName();
            }

            // Meta
            if (Style == TIMELINE_STYLE_COMPACT) {

                List<String>  meta = new ArrayList<>();
                if (!item.isRead()) {
                    meta.add(context.getString(R.string.unread));
                }

                if (dateResult != null) {
                    meta.add(formatOut.format(dateResult));
                }

                if (author.length() > 0) {
                    meta.add(author);
                }

                if (item.getPhotos().size() > 0) {
                    if (item.getPhotos().size() > 1) {
                        meta.add(String.format(context.getString(R.string.number_of_images), item.getPhotos().size()));
                    }
                    else {
                        meta.add(context.getString(R.string.one_image));
                    }
                }

                if (item.getPhotos().size() > 0 && Preferences.getPreference(context, "pref_key_image_preview_compact", false)) {
                    Glide.with(context)
                            .load(item.getPhotos().get(0))
                            .into(holder.image);
                    holder.card.setVisibility(View.VISIBLE);
                    holder.image.setVisibility(View.VISIBLE);
                }
                else {
                    holder.card.setVisibility(View.GONE);
                    holder.image.setVisibility(View.GONE);
                }

                if (meta.size() > 0) {
                    holder.meta.setVisibility(View.VISIBLE);
                    holder.meta.setText(TextUtils.join(" - ", meta));
                }
                else {
                    holder.meta.setVisibility(View.GONE);
                }

            }

            // Name.
            if ((item.getType().equals("entry") || item.getType().equals("event")) && item.getName().length() > 0) {
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(item.getName());
            }
            else if (Style == TIMELINE_STYLE_COMPACT && item.getTextContent().length() > 0) {
                int length = item.getTextContent().length();
                String elipsis = "";
                // Replace first. Newlines are counted as a character too, so we could end up with
                // an exception. Let's still use a try/catch block though to be sure.
                String shortText = item.getTextContent().replace("\n", "").replace("\r", "").replace("\r\n", "");
                if (length > 100) {
                    length = 100;
                    elipsis = " ...";
                }
                shortText = shortText.substring(0, length) + elipsis;
                try {
                    holder.name.setVisibility(View.VISIBLE);
                    holder.name.setText(shortText);
                }
                catch (Exception ignored) {
                    holder.name.setVisibility(View.GONE);
                }
            }
            else {
                holder.name.setVisibility(View.GONE);
            }

            // Button listeners.
            if (Preferences.getPreference(context, "pref_key_response_read", false)) {
                holder.read.setOnClickListener(new OnReadClickListener(position));
            }
            else {
                holder.read.setVisibility(View.GONE);
            }

            if (item.getUrl().length() > 0) {

                if (Preferences.getPreference(context, "pref_key_response_bookmark", false)) {
                    holder.bookmark.setVisibility(View.VISIBLE);
                    holder.bookmark.setOnClickListener(new OnBookmarkClickListener(position));
                }
                else {
                    holder.bookmark.setVisibility(View.GONE);
                }

                holder.reply.setVisibility(View.VISIBLE);
                holder.like.setVisibility(View.VISIBLE);
                holder.repost.setVisibility(View.VISIBLE);
                holder.external.setVisibility(View.VISIBLE);

                holder.reply.setOnClickListener(new OnReplyClickListener(position));
                holder.like.setOnClickListener(new OnLikeClickListener(position));
                holder.repost.setOnClickListener(new OnRepostClickListener(position));
                holder.external.setOnClickListener(new OnExternalClickListener(position));

                if (item.getType().equals("event")) {
                    holder.rsvp.setVisibility(View.VISIBLE);
                    holder.rsvp.setOnClickListener(new OnRsvpClickListener(position));
                }
                else {
                    holder.rsvp.setVisibility(View.GONE);
                }
            }
            else {
                holder.bookmark.setVisibility(View.GONE);
                holder.reply.setVisibility(View.GONE);
                holder.like.setVisibility(View.GONE);
                holder.repost.setVisibility(View.GONE);
                holder.external.setVisibility(View.GONE);
                holder.rsvp.setVisibility(View.GONE);
            }

            // Menu listener.
            holder.menu.setOnClickListener(new OnMenuClickListener(position, this.debugItemJSON));

            // Audio.
            if (item.getAudio().length() > 0) {
                holder.audio.setVisibility(View.VISIBLE);
                holder.audio.setOnClickListener(new OnAudioClickListener(position));
            }
            else {
                holder.audio.setVisibility(View.GONE);
            }

            // Video.
            if (item.getVideo().length() > 0) {
                holder.video.setVisibility(View.VISIBLE);
                holder.video.setOnClickListener(new OnVideoClickListener(position));
            }
            else {
                holder.video.setVisibility(View.GONE);
            }

            // Map.
            if (item.getLatitude().length() > 0 && item.getLongitude().length() > 0) {
                holder.map.setVisibility(View.VISIBLE);
                holder.map.setOnClickListener(new OnMapClickListener(position));
            }
            else {
                holder.map.setVisibility(View.GONE);
            }

            // Summary version.
            if (Style == TIMELINE_STYLE_SUMMARY) {

                // Unread.
                if (!item.isRead()) {
                    holder.unread.setVisibility(View.VISIBLE);
                    holder.unread.setText(R.string.unread);
                }
                else {
                    holder.unread.setVisibility(View.GONE);
                }

                if (dateResult != null) {
                    holder.published.setVisibility(View.VISIBLE);
                    holder.published.setText(formatOut.format(dateResult));
                }
                else {
                    holder.published.setVisibility(View.GONE);
                }

                // Start
                if (item.getStart().length() > 0) {
                    Date startDate = null;
                    for (String formatString : dateFormatStrings) {
                        try {
                            if (startDate != null) {
                                break;
                            }
                            startDate = new SimpleDateFormat(formatString).parse(item.getStart());
                        }
                        catch (ParseException ignored) {}
                    }

                    if (startDate != null) {
                        holder.start.setVisibility(View.VISIBLE);
                        holder.start.setText(String.format(context.getString(R.string.start_date_event), formatOut.format(startDate)));
                    }
                    else {
                        holder.start.setVisibility(View.GONE);
                    }
                }
                else {
                    holder.start.setVisibility(View.GONE);
                }

                // End
                if (item.getEnd().length() > 0) {
                    Date endDate = null;
                    for (String formatString : dateFormatStrings) {
                        try {
                            if (endDate != null) {
                                break;
                            }
                            endDate = new SimpleDateFormat(formatString).parse(item.getEnd());
                        }
                        catch (ParseException ignored) {}
                    }

                    if (endDate != null) {
                        holder.end.setVisibility(View.VISIBLE);
                        holder.end.setText(String.format(context.getString(R.string.end_date_event), formatOut.format(endDate)));
                    }
                    else {
                        holder.end.setVisibility(View.GONE);
                    }
                }
                else {
                    holder.end.setVisibility(View.GONE);
                }

                // Location
                if (item.getLocation().length() > 0) {

                    String location = "@ ";
                    try {
                        new URL(item.getLocation()).toURI();
                        location += "<a href=\"" + item.getLocation() + "\">" + item.getLocation() + "</a>";
                    }
                    catch (Exception e) {
                        location += item.getLocation();
                    }

                    CharSequence sequence = Html.fromHtml(location);
                    SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                    URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                    for (URLSpan span : urls) {
                        makeLinkClickable(strBuilder, span);
                    }

                    holder.location.setMovementMethod(LinkMovementMethod.getInstance());
                    holder.location.setVisibility(View.VISIBLE);
                    holder.location.setText(strBuilder);
                }
                else {
                    holder.location.setMovementMethod(null);
                    holder.location.setVisibility(View.GONE);
                }

                // Channel.
                if ((channelId.equals("global") || isSourceView) && item.getChannelName().length() > 0) {
                    holder.channel.setVisibility(View.VISIBLE);
                    holder.channel.setText(item.getChannelName());
                }
                else {
                    holder.channel.setVisibility(View.GONE);
                }

                if (author.length() > 0) {
                    holder.author.setVisibility(View.VISIBLE);
                    holder.author.setText(author);
                }
                else {
                    holder.author.setVisibility(View.GONE);
                }

                // Author photo.
                Glide.with(context)
                        .load(item.getAuthorPhoto())
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.avatar_small))
                        .into(holder.authorPhoto);

                // Source view.
                if (!isSourceView && Preferences.getPreference(context, "pref_key_author_timeline", false)) {
                    holder.authorPhoto.setOnClickListener(new OnAuthorClickListener(position));
                }

                String ResponseData = "";
                if (item.getType().equals("bookmark-of") || item.getType().equals("repost-of") || item.getType().equals("quotation-of") || item.getType().equals("in-reply-to") || item.getType().equals("like-of") || item.getType().equals("checkin")) {
                    String ResponseText = "";
                    String ResponseUrl = "";
                    String ResponseLinkText = "";
                    switch (item.getType()) {
                        case "in-reply-to":
                            ResponseText = "In reply to";
                            ResponseUrl = item.getResponseType("in-reply-to");
                            ResponseLinkText = ResponseUrl;
                            break;
                        case "like-of":
                            ResponseText = "Like of";
                            ResponseUrl = item.getResponseType("like-of");
                            ResponseLinkText = ResponseUrl;
                            break;
                        case "repost-of":
                        case "quotation-of":
                            ResponseText = "Repost of";
                            ResponseUrl = item.getResponseType(item.getType());
                            ResponseLinkText = ResponseUrl;
                            break;
                        case "bookmark-of":
                            ResponseText = "Bookmark of";
                            ResponseUrl = item.getResponseType("bookmark-of");
                            ResponseLinkText = ResponseUrl;
                            break;
                        case "checkin":
                            ResponseText = "Checked in at ";
                            ResponseUrl = item.getResponseType("checkin-url");
                            ResponseLinkText = item.getResponseType("checkin");
                            break;
                    }

                    try {
                        if (ResponseText.length() > 0 && ResponseUrl.length() > 0) {
                            ResponseData = ResponseText + " <a href=\"" + ResponseUrl + "\">" + ResponseLinkText + "</a>";
                        }
                    }
                    catch (Exception ignored) { }
                }

                if (ResponseData.length() > 0) {
                    holder.response.setVisibility(View.VISIBLE);

                    CharSequence sequence = Html.fromHtml(ResponseData);
                    SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                    URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                    for (URLSpan span : urls) {
                        makeLinkClickable(strBuilder, span);
                    }

                    holder.response.setText(strBuilder);
                    holder.response.setMovementMethod(LinkMovementMethod.getInstance());
                }
                else {
                    holder.response.setVisibility(View.GONE);
                }

                // Content.
                if (item.getHtmlContent().length() > 0 || item.getTextContent().length() > 0) {

                    holder.content.setVisibility(View.VISIBLE);

                    if (item.getHtmlContent().length() > 0) {
                        CharSequence sequence;
                        String html = item.getHtmlContent();
                        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                            sequence = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
                        } else {
                            sequence = Html.fromHtml(html);
                        }

                        // Trim end.
                        sequence = Utility.trim(sequence);

                        SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                        URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                        for (URLSpan span : urls) {
                            makeLinkClickable(strBuilder, span);
                        }
                        holder.content.setText(strBuilder);
                        holder.content.setMovementMethod(LinkMovementMethod.getInstance());
                    }
                    else {
                        holder.content.setMovementMethod(null);
                        holder.content.setText(item.getTextContent().trim());
                    }

                    if (item.getTextContent().length() > 400) {
                        holder.expand.setVisibility(View.VISIBLE);

                        holder.expand.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(final View v) {
                                if (holder.content.isExpanded()) {
                                    holder.content.collapse();
                                    holder.expand.setText(R.string.read_more);
                                } else {
                                    holder.content.expand();
                                    holder.expand.setText(R.string.close);
                                }
                            }
                        });

                        // Set listener on end collapse.
                        holder.content.addOnExpandListener(new ExpandableTextView.OnExpandListener() {
                            @Override
                            public void onEndCollapse(@NonNull ExpandableTextView view) {
                                listView.setSelection(position);
                            }
                        });

                    }
                    else {
                        holder.expand.setVisibility(View.GONE);
                    }
                }
                else {
                    holder.content.setMovementMethod(null);
                    holder.content.setVisibility(View.GONE);
                    holder.expand.setVisibility(View.GONE);
                }

                // Reference.
                if (item.getReference().length() > 0) {
                    holder.reference.setVisibility(View.VISIBLE);
                    holder.reference.setText(item.getReference());
                }
                else {
                    holder.reference.setVisibility(View.GONE);
                }

                // Image.
                if (item.getPhotos().size() > 0) {

                    boolean imagePreview = Preferences.getPreference(context, "pref_key_image_preview", true);
                    if (imagePreview) {
                        Glide.with(context)
                                .load(item.getPhotos().get(0))
                                .into(holder.image);
                        holder.image.setVisibility(View.VISIBLE);
                        holder.card.setVisibility(View.VISIBLE);
                        holder.image.setOnClickListener(new OnImageClickListener(position));
                    }
                    else {
                        holder.image.setVisibility(View.GONE);
                        holder.card.setVisibility(View.GONE);
                        holder.imageCount.setTextSize(16);
                        holder.imageCount.setOnClickListener(new OnImageClickListener(position));
                    }

                    if (item.getPhotos().size() > 1 || !imagePreview) {
                        holder.imageCount.setVisibility(View.VISIBLE);
                        if (item.getPhotos().size() > 1) {
                            holder.imageCount.setText(String.format(context.getString(R.string.number_of_images), item.getPhotos().size()));
                        }
                        else {
                            holder.imageCount.setText(R.string.one_image);
                        }
                    }
                    else {
                        holder.imageCount.setVisibility(View.GONE);
                    }
                }
                else {
                    holder.image.setVisibility(View.GONE);
                    holder.card.setVisibility(View.GONE);
                    holder.imageCount.setVisibility(View.GONE);
                }

                // Set on touch listener.
                if (Preferences.getPreference(context, "pref_key_timeline_summary_detail_click", false)) {
                    convertView.setOnTouchListener(eventTouch);
                }
            }
            else {
                // Set on touch listener.
                convertView.setOnTouchListener(eventTouch);
            }
        }

        return convertView;
    }

    /**
     * OnTouchListener for channel row.
     */
    private View.OnTouchListener eventTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            TimelineListAdapter.ViewHolder holder = (TimelineListAdapter.ViewHolder)v.getTag();
            switch(motionEvent.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    int downColor = context.getResources().getColor(R.color.listRowBackgroundColorTouched);
                    holder.row.setBackgroundColor(downColor);
                    break;
                case MotionEvent.ACTION_CANCEL:
                    int cancelColor = context.getResources().getColor(R.color.listRowBackgroundColor);
                    holder.row.setBackgroundColor(cancelColor);
                    break;
                case MotionEvent.ACTION_UP:
                    int position = holder.position;
                    int color = context.getResources().getColor(R.color.listRowBackgroundColor);
                    TimelineItem item = items.get(position);
                    holder.row.setBackgroundColor(color);
                    Indigenous app = Indigenous.getInstance();
                    app.setTimelineItem(item);
                    Intent intent = new Intent(context, TimelineDetailActivity.class);
                    context.startActivity(intent);
                    break;
            }
            return true;
        }
    };

    /**
     * Link clickable.
     *
     * @param strBuilder
     *   A string builder.
     * @param span
     *   The span with url.
     */
    private void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(View view) {
                try {
                    CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                    intentBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
                    intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                    CustomTabsIntent customTabsIntent = intentBuilder.build();
                    customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    customTabsIntent.launchUrl(context, Uri.parse(span.getURL()));
                }
                catch (Exception ignored) { }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

    // Reply listener.
    class OnReplyClickListener implements OnClickListener {

        int position;

        OnReplyClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, ReplyActivity.class);
            TimelineItem item = items.get(this.position);
            i.putExtra("incomingText", item.getUrl());
            context.startActivity(i);
        }
    }

    // Like listener.
    class OnLikeClickListener implements OnClickListener {

        int position;

        OnLikeClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, LikeActivity.class);
            TimelineItem item = items.get(this.position);
            i.putExtra("incomingText", item.getUrl());
            context.startActivity(i);
        }
    }

    // Repost listener.
    class OnRepostClickListener implements OnClickListener {

        int position;

        OnRepostClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, RepostActivity.class);
            TimelineItem item = items.get(this.position);
            i.putExtra("incomingText", item.getUrl());
            context.startActivity(i);
        }
    }

    // Bookmark listener.
    class OnBookmarkClickListener implements OnClickListener {

        int position;

        OnBookmarkClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, BookmarkActivity.class);
            TimelineItem item = items.get(this.position);
            i.putExtra("incomingText", item.getUrl());
            context.startActivity(i);
        }
    }

    // Read listener.
    class OnReadClickListener implements OnClickListener {

        int position;

        OnReadClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, ReadActivity.class);
            TimelineItem item = items.get(this.position);
            String text = "";
            if (item.getUrl().length() > 0) {
                text = item.getUrl();
            }
            else if (item.getName().length() > 0) {
                text = item.getName();
            }
            i.putExtra("incomingText", text);
            context.startActivity(i);
        }
    }

    // Image listener.
    class OnImageClickListener implements OnClickListener {

        int position;

        OnImageClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, TimelineImageActivity.class);
            TimelineItem item = items.get(this.position);
            i.putStringArrayListExtra("photos", item.getPhotos());
            context.startActivity(i);
        }
    }

    // External listener.
    class OnExternalClickListener implements OnClickListener {

        int position;

        OnExternalClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            TimelineItem item = items.get(this.position);

            try {
                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                intentBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
                intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                CustomTabsIntent customTabsIntent = intentBuilder.build();
                customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                customTabsIntent.launchUrl(context, Uri.parse(item.getUrl()));
            }
            catch (Exception ignored) { }

        }
    }

    // RSVP listener.
    class OnRsvpClickListener implements OnClickListener {

        int position;

        OnRsvpClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, RsvpActivity.class);
            TimelineItem item = items.get(this.position);
            i.putExtra("incomingText", item.getUrl());
            context.startActivity(i);
        }
    }

    // Audio listener.
    class OnAudioClickListener implements OnClickListener {

        int position;

        OnAudioClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, TimelineAudioActivity.class);
            TimelineItem item = items.get(this.position);
            i.putExtra("audio", item.getAudio());
            i.putExtra("title", item.getName());
            i.putExtra("authorPhoto", item.getAuthorPhoto());
            i.putExtra("authorName", item.getAuthorName());
            context.startActivity(i);
        }
    }

    // Video listener.
    class OnVideoClickListener implements OnClickListener {

        int position;

        OnVideoClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, TimelineVideoActivity.class);
            TimelineItem item = items.get(this.position);
            i.putExtra("video", item.getVideo());
            context.startActivity(i);
        }
    }

    // Map listener.
    class OnMapClickListener implements OnClickListener {

        int position;

        OnMapClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            TimelineItem item = items.get(this.position);
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri geoLocation = Uri.parse("geo:" + item.getLatitude() + "," + item.getLongitude());
            intent.setData(geoLocation);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                    context.startActivity(intent);
            }
            else {
                Snackbar.make(layout, context.getString(R.string.maps_info), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    // Author listener.
    class OnAuthorClickListener implements OnClickListener {

        int position;

        OnAuthorClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            TimelineItem item = items.get(this.position);
            Intent intent = new Intent(context, TimelineActivity.class);
            intent.putExtra("channelId", channelId);
            intent.putExtra("channelName", channelName);
            intent.putExtra("sourceId", item.getSourceId());
            intent.putExtra("sourceName", item.getAuthorName());
            context.startActivity(intent);
        }
    }

    // Menu listener.
    class OnMenuClickListener implements OnClickListener {

        int position;
        boolean debugJson;

        OnMenuClickListener(int position, boolean debugJson) {
            this.position = position;
            this.debugJson = debugJson;
        }

        @Override
        public void onClick(View v) {
            final TimelineItem entry = items.get(position);

            PopupMenu popup = new PopupMenu(context, v);
            Menu menu = popup.getMenu();
            popup.getMenuInflater().inflate(R.menu.timeline_list_item_menu, menu);

            // Debug menu item.
            if (this.debugJson) {
                MenuItem itemDebug = menu.findItem(R.id.timeline_entry_debug);
                if (itemDebug != null) {
                    itemDebug.setVisible(true);
                }
            }

            // Move menu item.
            if (Preferences.getPreference(context, "pref_key_move_item", false)) {
                MenuItem itemMove = menu.findItem(R.id.timeline_entry_move);
                if (itemMove != null) {
                    itemMove.setVisible(true);
                }
            }

            // Save contact menu item.
            if (Preferences.getPreference(context, "pref_key_contact_manage", false)) {
                MenuItem itemContact = menu.findItem(R.id.timeline_save_author);
                if (itemContact != null) {
                    itemContact.setVisible(true);
                }
            }

            // Mark read menu item.
            if (!entry.isRead() && (channelId.equals(Preferences.getPreference(context, "pref_key_read_later", "")) || channelId.equals("global") || Preferences.getPreference(context, "pref_key_mark_read", MARK_READ_CHANNEL_CLICK) == MARK_READ_MANUAL)) {
                MenuItem itemMarkRead = menu.findItem(R.id.timeline_entry_mark_read);
                if (itemMarkRead != null) {
                    itemMarkRead.setVisible(true);
                }
                MenuItem itemMarkUnread = menu.findItem(R.id.timeline_entry_mark_unread);
                if (itemMarkUnread != null) {
                    itemMarkUnread.setVisible(false);
                }
            }

            // Get static app.
            final Indigenous app = Indigenous.getInstance();

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(final MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.timeline_entry_delete:
                            builder.setTitle(context.getString(R.string.delete_post_confirm));
                            builder.setPositiveButton(context.getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    new MicrosubAction(context, user, layout).deletePost(channelId, entry.getId());
                                    items.remove(position);
                                    notifyDataSetChanged();
                                }
                            });
                            builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                            break;

                        case R.id.timeline_entry_move:

                            final List<Channel> channels = app.getChannelsList();
                            final List<CharSequence> displayValues = new ArrayList<>();
                            for (Channel channel : channels) {
                                String uid = channel.getUid();
                                if (!uid.equals("notifications") && !uid.equals("global") && !uid.equals(channelId)) {
                                    displayValues.add(channel.getName());
                                }
                            }
                            final CharSequence[] channelItems = displayValues.toArray(new CharSequence[displayValues.size()]);

                            builder.setTitle(context.getString(R.string.select_channel_move));
                            builder.setSingleChoiceItems(channelItems, -1, null);
                            builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.setPositiveButton(R.string.move_item, new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    ListView lw = ((AlertDialog)dialog).getListView();
                                    Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                                    if (checkedItem != null) {
                                        for (Channel channel : channels) {
                                            if (channel.getName() == checkedItem) {
                                                MicrosubAction ms = new MicrosubAction(context, user, layout);
                                                boolean moved = ms.movePost(channel.getUid(), channel.getName(), entry.getId());
                                                if (moved && channel.getUid().equals(Preferences.getPreference(context, "pref_key_read_later", ""))) {
                                                    List<String> unreadEntries = new ArrayList<>();
                                                    unreadEntries.add(entry.getId());
                                                    ms.markUnread(channel.getUid(), unreadEntries, false);
                                                }
                                                items.remove(position);
                                                notifyDataSetChanged();
                                                break;
                                            }
                                        }
                                    }
                                }
                            });
                            builder.show();
                            break;

                        case R.id.timeline_entry_mark_unread:
                            List<String> unreadEntries = new ArrayList<>();
                            unreadEntries.add(entry.getId());
                            entry.setRead(false);
                            new MicrosubAction(context, user, layout).markUnread(channelId, unreadEntries, true);
                            break;

                        case R.id.timeline_entry_mark_read:
                            List<String> readEntries = new ArrayList<>();
                            readEntries.add(entry.getId());
                            entry.setRead(true);
                            new MicrosubAction(context, user, layout).markRead(channelId, readEntries, false, true);
                            break;

                        case R.id.timeline_entry_debug:
                            Intent i = new Intent(context, DebugActivity.class);
                            app.setDebug(entry.getJson());
                            context.startActivity(i);
                            break;

                        case R.id.timeline_save_author:
                            Indigenous app = Indigenous.getInstance();

                            if (entry.getAuthorName().length() > 0) {

                                Contact contact = new Contact();
                                contact.setName(entry.getAuthorName());
                                if (entry.getAuthorPhoto().length() > 0) {
                                    contact.setPhoto(entry.getAuthorPhoto());
                                }
                                if (entry.getAuthorUrl().length() > 0) {
                                    contact.setUrl(entry.getAuthorUrl());
                                }

                                app.setContact(contact);
                                Intent startActivity =  new Intent(context, ContactActivity.class);
                                startActivity.putExtra("addContact", true);
                                context.startActivity(startActivity);
                            }
                            else {
                                Snackbar.make(layout, context.getString(R.string.contact_no_name), Snackbar.LENGTH_SHORT).show();
                            }

                            break;

                        case R.id.timeline_entry_share:
                            Intent share = new Intent(android.content.Intent.ACTION_SEND);
                            share.setType("text/plain");
                            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            share.putExtra(Intent.EXTRA_TEXT, entry.getUrl());
                            context.startActivity(Intent.createChooser(share, context.getString(R.string.share_post)));
                            break;
                    }
                    return true;
                }
            });
            popup.show();
        }
    }

}
