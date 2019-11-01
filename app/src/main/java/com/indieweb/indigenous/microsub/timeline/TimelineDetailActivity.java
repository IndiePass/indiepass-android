package com.indieweb.indigenous.microsub.timeline;

import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.general.DebugActivity;
import com.indieweb.indigenous.micropub.post.BookmarkActivity;
import com.indieweb.indigenous.micropub.post.LikeActivity;
import com.indieweb.indigenous.micropub.post.ReplyActivity;
import com.indieweb.indigenous.micropub.post.RepostActivity;
import com.indieweb.indigenous.micropub.post.RsvpActivity;
import com.indieweb.indigenous.microsub.MicrosubAction;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.indieweb.indigenous.microsub.timeline.TimelineActivity.MARK_READ_CHANNEL_CLICK;
import static com.indieweb.indigenous.microsub.timeline.TimelineActivity.MARK_READ_MANUAL;
import static com.indieweb.indigenous.util.Utility.dateFormatStrings;

public class TimelineDetailActivity extends AppCompatActivity {

    protected TimelineItem item;
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline_detail);

        Indigenous app = Indigenous.getInstance();
        item = app.getTimelineItem();
        if (item == null) {
            Toast.makeText(TimelineDetailActivity.this, "No item found", Toast.LENGTH_SHORT).show();
            finish();
        }
        else {

            user = new Accounts(this).getCurrentUser();

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
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MMM yyyy kk:mm");
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
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.avatar_small))
                    .into(authorPhoto);

            // Author name
            TextView authorName = findViewById(R.id.timeline_author);
            if (item.getAuthorName().length() > 0) {
                authorName.setText(item.getAuthorName());
            }
            else {
                authorName.setVisibility(View.GONE);
            }

            // Response.
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

            TextView response = findViewById(R.id.timeline_response);
            if (ResponseData.length() > 0) {

                CharSequence sequence = Html.fromHtml(ResponseData);
                SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                for (URLSpan span : urls) {
                    makeLinkClickable(strBuilder, span);
                }

                response.setText(strBuilder);
                response.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else {
                response.setVisibility(View.GONE);
            }

            // Content.
            TextView content = findViewById(R.id.timeline_content);
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
                        makeLinkClickable(strBuilder, span);
                    }
                    content.setText(strBuilder);
                    content.setMovementMethod(LinkMovementMethod.getInstance());
                }
                else {
                    content.setMovementMethod(null);
                    content.setText(item.getTextContent().trim());
                }
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

            // Image or map.
            boolean staticMap = Preferences.getPreference(TimelineDetailActivity.this, "pref_key_static_map", true);
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
                        imageCount.setText(String.format("%d images", item.getPhotos().size()));
                    }
                    else {
                        imageCount.setText(R.string.one_image);
                    }
                }
                else {
                    imageCount.setVisibility(View.GONE);
                }
            }
            else if (staticMap && item.getLongitude().length() > 0 && item.getLatitude().length() > 0) {
                String mapUrl = "http://atlas.p3k.io/map/img?marker[]=lat:" + item.getLatitude() + ";lng:" + item.getLongitude() + ";icon:small-blue-cutout&basemap=gray&width=460&height=460&zoom=14";
                Glide.with(TimelineDetailActivity.this)
                        .load(mapUrl)
                        .into(image);
            }
            else {
                image.setVisibility(View.GONE);
                card.setVisibility(View.GONE);
                imageCount.setVisibility(View.GONE);
            }

            // Button listeners.
            Button bookmark = findViewById(R.id.itemBookmark);
            Button reply = findViewById(R.id.itemReply);
            Button like = findViewById(R.id.itemLike);
            Button repost = findViewById(R.id.itemRepost);
            Button external = findViewById(R.id.itemExternal);
            Button rsvp = findViewById(R.id.itemRSVP);
            Button menu = findViewById(R.id.itemMenu);
            Button audio = findViewById(R.id.itemAudio);
            Button video = findViewById(R.id.itemVideo);
            Button map = findViewById(R.id.itemMap);

            if (item.getUrl().length() > 0) {
                bookmark.setOnClickListener(new OnBookmarkClickListener());
                reply.setOnClickListener(new OnReplyClickListener());
                like.setOnClickListener(new OnLikeClickListener());
                repost.setOnClickListener(new OnRepostClickListener());
                external.setOnClickListener(new OnExternalClickListener());

                if (item.getType().equals("event")) {
                    rsvp.setVisibility(View.VISIBLE);
                    rsvp.setOnClickListener(new OnRsvpClickListener());
                }
                else {
                    rsvp.setVisibility(View.GONE);
                }
            }
            else {
                bookmark.setVisibility(View.GONE);
                reply.setVisibility(View.GONE);
                like.setVisibility(View.GONE);
                repost.setVisibility(View.GONE);
                external.setVisibility(View.GONE);
                rsvp.setVisibility(View.GONE);
            }

            // Menu listener.
            boolean debugItemJSON = Preferences.getPreference(TimelineDetailActivity.this, "pref_key_debug_microsub_item_json", false);
            menu.setOnClickListener(new OnMenuClickListener(debugItemJSON));

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
            i.putExtra("incomingText", item.getUrl());
            startActivity(i);
        }
    }

    // Like listener.
    class OnLikeClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent(TimelineDetailActivity.this, LikeActivity.class);
            i.putExtra("incomingText", item.getUrl());
            startActivity(i);
        }
    }

    // Repost listener.
    class OnRepostClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent(TimelineDetailActivity.this, RepostActivity.class);
            i.putExtra("incomingText", item.getUrl());
            startActivity(i);
        }
    }

    // Bookmark listener.
    class OnBookmarkClickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            Intent i = new Intent(TimelineDetailActivity.this, BookmarkActivity.class);
            i.putExtra("incomingText", item.getUrl());
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
                Toast.makeText(TimelineDetailActivity.this, "Install a maps application to view this location", Toast.LENGTH_SHORT).show();
            }

        }
    }

    // Menu listener.
    class OnMenuClickListener implements View.OnClickListener {

        boolean debugJson;

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

            if (!entry.isRead() && (item.getChannelId().equals("global") || Preferences.getPreference(TimelineDetailActivity.this, "pref_key_mark_read", MARK_READ_CHANNEL_CLICK) == MARK_READ_MANUAL)) {
                MenuItem itemMarkRead = menu.findItem(R.id.timeline_entry_mark_read);
                if (itemMarkRead != null) {
                    itemMarkRead.setVisible(true);
                }
                MenuItem itemMarkUnread = menu.findItem(R.id.timeline_entry_mark_unread);
                if (itemMarkUnread != null) {
                    itemMarkUnread.setVisible(false);
                }
            }

            final AlertDialog.Builder builder = new AlertDialog.Builder(TimelineDetailActivity.this);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(final MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.timeline_entry_delete:
                            builder.setTitle("Are you sure you want to delete this post ?");
                            builder.setPositiveButton(getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog,int id) {
                                    new MicrosubAction(TimelineDetailActivity.this, user).deletePost(entry.getChannelId(), entry.getId());
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
                            new MicrosubAction(TimelineDetailActivity.this, user).markUnread(entry.getChannelId(), unreadEntries);
                            Toast.makeText(TimelineDetailActivity.this, "Item marked unread", Toast.LENGTH_SHORT).show();
                            break;

                        case R.id.timeline_entry_mark_read:
                            List<String> readEntries = new ArrayList<>();
                            new MicrosubAction(TimelineDetailActivity.this, user).markRead(entry.getChannelId(), readEntries, false);
                            Toast.makeText(TimelineDetailActivity.this, "Item marked read", Toast.LENGTH_SHORT).show();
                            break;

                        case R.id.timeline_entry_debug:
                            Intent i = new Intent(TimelineDetailActivity.this, DebugActivity.class);
                            Indigenous app = Indigenous.getInstance();
                            app.setDebug(entry.getJson());
                            startActivity(i);
                            break;

                        case R.id.timeline_entry_share:
                            Intent share = new Intent(android.content.Intent.ACTION_SEND);
                            share.setType("text/plain");
                            share.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
                            share.putExtra(Intent.EXTRA_TEXT, entry.getUrl());
                            startActivity(Intent.createChooser(share, "Share post"));
                            break;
                    }
                    return true;
                }
            });
            popup.show();
        }
    }

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
            public void onClick(@NonNull View view) {
                try {
                    CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                    intentBuilder.setToolbarColor(ContextCompat.getColor(TimelineDetailActivity.this, R.color.colorPrimary));
                    intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(TimelineDetailActivity.this, R.color.colorPrimaryDark));
                    CustomTabsIntent customTabsIntent = intentBuilder.build();
                    customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                    customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    customTabsIntent.launchUrl(TimelineDetailActivity.this, Uri.parse(span.getURL()));
                }
                catch (Exception ignored) { }
            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

}
