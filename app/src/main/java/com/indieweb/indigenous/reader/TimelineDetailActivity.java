package com.indieweb.indigenous.reader;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.general.DebugActivity;
import com.indieweb.indigenous.post.ReadActivity;
import com.indieweb.indigenous.post.ReplyActivity;
import com.indieweb.indigenous.post.RsvpActivity;
import com.indieweb.indigenous.indieweb.microsub.MicrosubAction;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.Accounts;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.indieweb.indigenous.reader.Reader.RESPONSE_LIKE;
import static com.indieweb.indigenous.reader.Reader.RESPONSE_REPOST;
import static com.indieweb.indigenous.reader.TimelineActivity.MARK_READ_CHANNEL_CLICK;
import static com.indieweb.indigenous.reader.TimelineActivity.MARK_READ_MANUAL;
import static com.indieweb.indigenous.util.Utility.dateFormatStrings;

public class TimelineDetailActivity extends AppCompatActivity {

    RelativeLayout layout;
    protected TimelineItem item;
    User user;
    private Reader reader;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_detail);

        layout = findViewById(R.id.timeline_detail_root);

        Indigenous app = Indigenous.getInstance();
        item = app.getTimelineItem();
        if (item == null) {
            Snackbar.make(layout, getString(R.string.no_item_found), Snackbar.LENGTH_SHORT).show();
            finish();
        }
        else {

            user = new Accounts(this).getDefaultUser();
            reader = ReaderFactory.getReader(user, item.getChannelId(), TimelineDetailActivity.this);

            // Channel.
            TextView channel = findViewById(R.id.timeline_channel);
            if (item.getChannelName().length() > 0) {
                channel.setText(item.getChannelName());
            }
            else {
                channel.setVisibility(View.GONE);
            }

            // Name.
            TextView name = findViewById(R.id.timeline_name);
            if ((item.getType().equals("entry") || item.getType().equals("event")) && item.getName().length() > 0) {
                name.setText(item.getName());
            }
            else {
                name.setVisibility(View.GONE);
            }

            // Published.
            TextView published = findViewById(R.id.timeline_published);
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MM yyyy HH:mm");
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
            if (dateResult != null) {
                published.setText(formatOut.format(dateResult));
            }
            else {
                published.setVisibility(View.GONE);
            }

            // Author photo
            ImageView authorPhoto = findViewById(R.id.timeline_author_photo);
            Glide.with(TimelineDetailActivity.this)
                    .load(item.getAuthorPhoto())
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.avatar))
                    .into(authorPhoto);

            // Author name
            TextView authorName = findViewById(R.id.timeline_author);
            if (item.getAuthorName().length() > 0) {
                authorName.setText(item.getAuthorName());
            }
            else {
                authorName.setVisibility(View.GONE);
            }

            // Start
            TextView start = findViewById(R.id.timeline_start);
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
                    start.setVisibility(View.VISIBLE);
                    start.setText(String.format(getString(R.string.start_date_event), formatOut.format(startDate)));
                }
                else {
                    start.setVisibility(View.GONE);
                }
            }
            else {
                start.setVisibility(View.GONE);
            }

            // End
            TextView end = findViewById(R.id.timeline_end);
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
                    end.setVisibility(View.VISIBLE);
                    end.setText(String.format(getString(R.string.end_date_event), formatOut.format(endDate)));
                }
                else {
                    end.setVisibility(View.GONE);
                }
            }
            else {
                end.setVisibility(View.GONE);
            }

            // Location
            TextView location = findViewById(R.id.timeline_location);
            if (item.getLocation().length() > 0) {

                String locationString = "@ ";
                try {
                    new URL(item.getLocation()).toURI();
                    locationString += "<a href=\"" + item.getLocation() + "\">" + item.getLocation() + "</a>";
                }
                catch (Exception e) {
                    locationString += item.getLocation();
                }

                CharSequence sequence = Html.fromHtml(locationString);
                SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                for (URLSpan span : urls) {
                    Utility.makeLinkClickable(strBuilder, span, TimelineDetailActivity.this, null, null);
                }

                location.setMovementMethod(LinkMovementMethod.getInstance());
                location.setVisibility(View.VISIBLE);
                location.setText(strBuilder);
            }
            else {
                location.setMovementMethod(null);
                location.setVisibility(View.GONE);
            }

            // Response.
            String ResponseData = "";
            if (item.getType().equals("bookmark-of") || item.getType().equals("repost-of") || item.getType().equals("quotation-of") || item.getType().equals("in-reply-to") || item.getType().equals("like-of") || item.getType().equals("checkin")) {
                String ResponseText = "";
                String ResponseUrl = "";
                String ResponseLinkText = "";
                String ResponseSuffix = "";
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
                        if (item.getActor().length() > 0) {
                            ResponseSuffix = " by " + item.getActor();
                        }
                        break;
                    case "repost-of":
                    case "quotation-of":
                        ResponseText = "Repost of";
                        ResponseUrl = item.getResponseType(item.getType());
                        ResponseLinkText = ResponseUrl;
                        if (item.getActor().length() > 0) {
                            ResponseSuffix = " by " + item.getActor();
                        }
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
                        if (ResponseSuffix.length() > 0) {
                            ResponseData += " " + ResponseSuffix;
                        }
                    }
                }
                catch (Exception ignored) { }
            }

            TextView response = findViewById(R.id.timeline_response);
            if (ResponseData.length() > 0) {

                CharSequence sequence = Html.fromHtml(ResponseData);
                SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                for (URLSpan span : urls) {
                    Utility.makeLinkClickable(strBuilder, span, TimelineDetailActivity.this, null, null);
                }

                response.setText(strBuilder);
                response.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else {
                response.setVisibility(View.GONE);
            }

            // Content.
            final TextView content = findViewById(R.id.timeline_content);
            if (item.getHtmlContent().length() > 0 || item.getTextContent().length() > 0) {

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
                        Utility.makeLinkClickable(strBuilder, span, TimelineDetailActivity.this, reader, item);
                    }
                    content.setText(strBuilder);
                    content.setMovementMethod(LinkMovementMethod.getInstance());
                }
                else {
                    content.setMovementMethod(null);
                    content.setText(item.getTextContent().trim());
                }

                content.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (!content.isTextSelectable()) {
                            content.setTextIsSelectable(true);
                        }
                        return true;
                    }
                });

            }
            else {
                content.setMovementMethod(null);
                content.setVisibility(View.GONE);
            }

            // Reference.
            TextView reference = findViewById(R.id.timeline_reference);
            if (item.getReference().length() > 0) {
                reference.setText(item.getReference());
            }
            else {
                reference.setVisibility(View.GONE);
            }

            // Image.
            ImageView image = findViewById(R.id.timeline_image);
            TextView imageCount = findViewById(R.id.timeline_image_count);
            CardView card = findViewById(R.id.timeline_card);
            if (item.getPhotos().size() > 0) {

                Glide.with(TimelineDetailActivity.this)
                        .load(item.getPhotos().get(0))
                        .into(image);
                image.setOnClickListener(new OnImageClickListener());

                if (item.getPhotos().size() > 1) {
                    if (item.getPhotos().size() > 1) {
                        imageCount.setText(String.format(getString(R.string.number_of_images), item.getPhotos().size()));
                    }
                    else {
                        imageCount.setText(R.string.one_image);
                    }
                }
                else {
                    imageCount.setVisibility(View.GONE);
                }
            }
            else {
                image.setVisibility(View.GONE);
                card.setVisibility(View.GONE);
                imageCount.setVisibility(View.GONE);
            }

            // Button listeners.
            Button bookmark = findViewById(R.id.itemBookmark);
            Button read = findViewById(R.id.itemRead);
            Button reply = findViewById(R.id.itemReply);
            Button like = findViewById(R.id.itemLike);
            Button repost = findViewById(R.id.itemRepost);
            Button external = findViewById(R.id.itemExternal);
            Button rsvp = findViewById(R.id.itemRSVP);
            Button menu = findViewById(R.id.itemMenu);
            Button audio = findViewById(R.id.itemAudio);
            Button video = findViewById(R.id.itemVideo);
            Button map = findViewById(R.id.itemMap);

            if (Preferences.getPreference(getApplicationContext(), "pref_key_response_read", false)) {
                read.setOnClickListener(new OnReadClickListener());
            }
            else {
                read.setVisibility(View.GONE);
            }

            if (item.getUrl().length() > 0) {

                if (Preferences.getPreference(getApplicationContext(), "pref_key_response_bookmark", false)) {
                    bookmark.setOnClickListener(new OnBookmarkClickListener());
                    if (item.isBookmarked()) {
                        bookmark.setActivated(true);
                    }
                    else {
                        bookmark.setActivated(false);
                    }
                }
                else {
                    bookmark.setVisibility(View.GONE);
                }

                reply.setOnClickListener(new OnReplyClickListener());

                like.setOnClickListener(new OnLikeClickListener());
                if (item.isLiked()) {
                    like.setActivated(true);
                }
                else {
                    like.setActivated(false);
                }

                repost.setOnClickListener(new OnRepostClickListener());
                if (item.isReposted()) {
                    repost.setActivated(true);
                }
                else {
                    repost.setActivated(false);
                }


                external.setOnClickListener(new OnExternalClickListener());

                if (item.getType().equals("event")) {
                    rsvp.setVisibility(View.VISIBLE);
                    rsvp.setOnClickListener(new OnRsvpClickListener());
                }
                else {
                    rsvp.setVisibility(View.GONE);
                }

                boolean debugItemJSON = Preferences.getPreference(TimelineDetailActivity.this, "pref_key_debug_microsub_item_json", false);
                menu.setOnClickListener(new OnMenuClickListener(debugItemJSON));
            }
            else {
                bookmark.setVisibility(View.GONE);
                reply.setVisibility(View.GONE);
                like.setVisibility(View.GONE);
                repost.setVisibility(View.GONE);
                external.setVisibility(View.GONE);
                rsvp.setVisibility(View.GONE);
                menu.setVisibility(View.GONE);
            }

            // Audio.
            if (item.getAudio().length() > 0) {
                audio.setOnClickListener(new OnAudioClickListener());
            }
            else {
                audio.setVisibility(View.GONE);
            }

            // Video.
            if (item.getVideo().length() > 0) {
                video.setOnClickListener(new OnVideoClickListener());
            }
            else {
                video.setVisibility(View.GONE);
            }

            // Map.
            if (item.getLatitude().length() > 0 && item.getLongitude().length() > 0) {
                map.setOnClickListener(new OnMapClickListener());
            }
            else {
                map.setVisibility(View.GONE);
            }

        }
    }

    // Reply listener.
    class OnReplyClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent(TimelineDetailActivity.this, ReplyActivity.class);
            i.putExtra("incomingText", reader.getReplyId(item));
            startActivity(i);
        }
    }

    // Like listener.
    class OnLikeClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (!reader.supports(RESPONSE_LIKE)) {
                Snackbar.make(layout, getString(R.string.no_anonymous_posting), Snackbar.LENGTH_SHORT).show();
            }
            else {
                boolean liked = reader.doResponse(item, Reader.RESPONSE_LIKE);
                if (liked) {
                    item.setLiked(true);
                    v.setActivated(true);
                }
                else {
                    item.setLiked(false);
                    v.setActivated(false);
                }
            }
        }
    }

    // Repost listener.
    class OnRepostClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (!reader.supports(RESPONSE_REPOST)) {
                Snackbar.make(layout, getString(R.string.no_anonymous_posting), Snackbar.LENGTH_SHORT).show();
            }
            else {
                boolean reposted = reader.doResponse(item, Reader.RESPONSE_REPOST);
                if (reposted) {
                    item.setReposted(true);
                    v.setActivated(true);
                }
                else {
                    item.setReposted(false);
                    v.setActivated(false);
                }
            }
        }
    }

    // Bookmark listener.
    class OnBookmarkClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            if (!reader.supports(Reader.RESPONSE_BOOKMARK)) {
                Snackbar.make(layout, getString(R.string.no_anonymous_posting), Snackbar.LENGTH_SHORT).show();
            }
            else {
                boolean bookmarked = reader.doResponse(item, Reader.RESPONSE_BOOKMARK);
                if (bookmarked) {
                    item.setBookmarked(true);
                    v.setActivated(true);
                }
                else {
                    item.setBookmarked(false);
                    v.setActivated(false);
                }
            }
        }
    }

    // Read listener.
    class OnReadClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(TimelineDetailActivity.this, ReadActivity.class);
            String text = "";
            if (item.getUrl().length() > 0) {
                text = item.getUrl();
            }
            else if (item.getName().length() > 0) {
                text = item.getName();
            }
            i.putExtra("incomingText", text);
            startActivity(i);
        }
    }

    // Image listener.
    class OnImageClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent(TimelineDetailActivity.this, TimelineImageActivity.class);
            i.putStringArrayListExtra("photos", item.getPhotos());
            startActivity(i);
        }
    }

    // External listener.
    class OnExternalClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            try {
                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                intentBuilder.setToolbarColor(ContextCompat.getColor(TimelineDetailActivity.this, R.color.colorPrimary));
                intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(TimelineDetailActivity.this, R.color.colorPrimaryDark));
                CustomTabsIntent customTabsIntent = intentBuilder.build();
                customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                customTabsIntent.launchUrl(TimelineDetailActivity.this, Uri.parse(item.getUrl()));
            }
            catch (Exception ignored) { }

        }
    }

    // RSVP listener.
    class OnRsvpClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent(TimelineDetailActivity.this, RsvpActivity.class);
            i.putExtra("incomingText", item.getUrl());
            startActivity(i);
        }
    }

    // Audio listener.
    class OnAudioClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent(TimelineDetailActivity.this, TimelineAudioActivity.class);
            i.putExtra("audio", item.getAudio());
            i.putExtra("title", item.getName());
            i.putExtra("authorPhoto", item.getAuthorPhoto());
            i.putExtra("authorName", item.getAuthorName());
            startActivity(i);
        }
    }

    // Video listener.
    class OnVideoClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent(TimelineDetailActivity.this, TimelineVideoActivity.class);
            i.putExtra("video", item.getVideo());
            startActivity(i);
        }
    }

    // Map listener.
    class OnMapClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            Uri geoLocation = Uri.parse("geo:" + item.getLatitude() + "," + item.getLongitude());
            intent.setData(geoLocation);
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
            else {
                Snackbar.make(layout, getString(R.string.maps_info), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    // Menu listener.
    class OnMenuClickListener implements View.OnClickListener {

        final boolean debugJson;

        OnMenuClickListener(boolean debugJson) {
            this.debugJson = debugJson;
        }

        @Override
        public void onClick(View v) {
            final TimelineItem entry = item;

            PopupMenu popup = new PopupMenu(TimelineDetailActivity.this, v);
            Menu menu = popup.getMenu();
            popup.getMenuInflater().inflate(R.menu.timeline_list_item_menu, menu);

            if (this.debugJson) {
                MenuItem itemDebug = menu.findItem(R.id.timeline_entry_debug);
                if (itemDebug != null) {
                    itemDebug.setVisible(true);
                }
            }

            final Reader reader = ReaderFactory.getReader(user, item.getChannelId(), TimelineDetailActivity.this);
            if (!reader.supports(Reader.READER_MARK_READ)) {
                MenuItem itemMarkRead = menu.findItem(R.id.timeline_entry_mark_read);
                if (itemMarkRead != null) {
                    itemMarkRead.setVisible(false);
                }
                MenuItem itemMarkUnread = menu.findItem(R.id.timeline_entry_mark_unread);
                if (itemMarkUnread != null) {
                    itemMarkUnread.setVisible(false);
                }
            }
            else {
                if (!entry.isRead() && (item.getChannelId().equals(Preferences.getPreference(getApplicationContext(), "pref_key_read_later", "")) && item.getChannelId().equals("global") || Preferences.getPreference(TimelineDetailActivity.this, "pref_key_mark_read", MARK_READ_CHANNEL_CLICK) == MARK_READ_MANUAL)) {
                    MenuItem itemMarkRead = menu.findItem(R.id.timeline_entry_mark_read);
                    if (itemMarkRead != null) {
                        itemMarkRead.setVisible(true);
                    }
                    MenuItem itemMarkUnread = menu.findItem(R.id.timeline_entry_mark_unread);
                    if (itemMarkUnread != null) {
                        itemMarkUnread.setVisible(false);
                    }
                }
            }

            if (reader.hideDelete(item.getChannelId())) {
                MenuItem itemDelete = menu.findItem(R.id.timeline_entry_delete);
                if (itemDelete != null) {
                    itemDelete.setVisible(false);
                }
            }

            // Save contact menu item.
            if (reader.supports(Reader.READER_CONTACT)) {
                MenuItem itemContact = menu.findItem(R.id.timeline_save_author);
                if (itemContact != null) {
                    itemContact.setVisible(true);
                    reader.setContactLabel(itemContact, item);
                }
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(TimelineDetailActivity.this);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(final MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.timeline_entry_delete:
                            builder.setTitle(getString(R.string.delete_post_confirm));
                            builder.setPositiveButton(getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    new MicrosubAction(TimelineDetailActivity.this, user, layout).deletePost(entry.getChannelId(), entry.getId());
                                }
                            });
                            builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                            break;

                        case R.id.timeline_entry_mark_unread:
                            List<String> unreadEntries = new ArrayList<>();
                            new MicrosubAction(TimelineDetailActivity.this, user, layout).markUnread(entry.getChannelId(), unreadEntries, true);
                            Utility.notifyChannels(entry, 1);
                            break;

                        case R.id.timeline_entry_mark_read:
                            List<String> readEntries = new ArrayList<>();
                            new MicrosubAction(TimelineDetailActivity.this, user, layout).markRead(entry.getChannelId(), readEntries, false, true);
                            Utility.notifyChannels(entry, -1);
                            break;

                        case R.id.timeline_entry_debug:
                            Intent i = new Intent(TimelineDetailActivity.this, DebugActivity.class);
                            Indigenous app = Indigenous.getInstance();
                            app.setDebug(entry.getJson());
                            startActivity(i);
                            break;

                        case R.id.timeline_save_author:
                            if (entry.getAuthorName().length() > 0) {
                                reader.doResponse(entry, Reader.RESPONSE_CONTACT);
                            }
                            else {
                                Snackbar.make(layout, getString(R.string.contact_no_name), Snackbar.LENGTH_SHORT).show();
                            }
                            break;

                        case R.id.timeline_entry_share:
                            Intent share = new Intent(android.content.Intent.ACTION_SEND);
                            share.setType("text/plain");
                            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            share.putExtra(Intent.EXTRA_TEXT, entry.getUrl());
                            startActivity(Intent.createChooser(share, getString(R.string.share_post)));
                            break;
                    }
                    return true;
                }
            });
            popup.show();
        }
    }

}
