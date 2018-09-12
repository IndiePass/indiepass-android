package com.indieweb.indigenous.microsub.timeline;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.CardView;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.post.BookmarkActivity;
import com.indieweb.indigenous.micropub.post.LikeActivity;
import com.indieweb.indigenous.micropub.post.ReplyActivity;
import com.indieweb.indigenous.micropub.post.RepostActivity;
import com.indieweb.indigenous.micropub.post.RsvpActivity;
import com.indieweb.indigenous.model.TimelineItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import at.blogc.android.views.ExpandableTextView;

/**
 * Timeline items list adapter.
 */
public class TimelineListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<TimelineItem> items;
    private LayoutInflater mInflater;

    TimelineListAdapter(Context context, List<TimelineItem> items) {
        this.context = context;
        this.items = items;
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
        public TextView unread;
        public TextView author;
        public ImageView authorPhoto;
        public TextView name;
        // TODO might need a better name
        public TextView context;
        public TextView published;
        public Button expand;
        public ExpandableTextView content;
        public ImageView image;
        public CardView card;
        public LinearLayout row;
        public Button reply;
        public Button like;
        public Button repost;
        public Button bookmark;
        public Button audio;
        public Button external;
        public Button rsvp;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.timeline_list_item, null);
            holder = new ViewHolder();
            holder.unread = convertView.findViewById(R.id.timeline_new);
            holder.published = convertView.findViewById(R.id.timeline_published);
            holder.author = convertView.findViewById(R.id.timeline_author);
            holder.authorPhoto = convertView.findViewById(R.id.timeline_author_photo);
            holder.name = convertView.findViewById(R.id.timeline_name);
            holder.context = convertView.findViewById(R.id.timeline_context);
            holder.content = convertView.findViewById(R.id.timeline_content);
            holder.expand = convertView.findViewById(R.id.timeline_content_more);
            holder.image = convertView.findViewById(R.id.timeline_image);
            holder.card = convertView.findViewById(R.id.timeline_card);
            holder.row = convertView.findViewById(R.id.timeline_item_row);
            holder.reply = convertView.findViewById(R.id.itemReply);
            holder.bookmark = convertView.findViewById(R.id.itemBookmark);
            holder.like = convertView.findViewById(R.id.itemLike);
            holder.repost = convertView.findViewById(R.id.itemRepost);
            holder.audio = convertView.findViewById(R.id.itemAudio);
            holder.external = convertView.findViewById(R.id.itemExternal);
            holder.rsvp = convertView.findViewById(R.id.itemRSVP);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        final TimelineItem item = items.get(position);
        if (item != null) {

            // Color of row.
            int color = context.getResources().getColor(R.color.listRowBackgroundColor);
            holder.row.setBackgroundColor(color);

            // Unread.
            if (!item.isRead()) {
                holder.unread.setVisibility(View.VISIBLE);
                holder.unread.setText(R.string.unread);
            }
            else {
                holder.unread.setVisibility(View.GONE);
            }

            // Published.
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd'T'kk:mm:ssZ");
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MMM yyyy kk:mm");
            Date result;
            try {
                result = formatIn.parse(item.getPublished());
                holder.published.setVisibility(View.VISIBLE);
                holder.published.setText(formatOut.format(result));
            } catch (ParseException ignored) {
                holder.published.setVisibility(View.GONE);
            }

            // Author.
            if (item.getAuthorName().length() > 0) {
                holder.author.setVisibility(View.VISIBLE);
                holder.author.setText(item.getAuthorName());
            }
            else {
                holder.author.setVisibility(View.GONE);
            }

            // Author photo.
            Glide.with(context)
                    .load(item.getAuthorPhoto())
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.avatar_small))
                    .into(holder.authorPhoto);

            // Name.
            if ((item.getType().equals("entry") || item.getType().equals("event")) && item.getName().length() > 0) {
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(item.getName());
            }
            else {
                holder.name.setVisibility(View.GONE);
            }

            String ContextData = "";
            if (item.getType().equals("bookmark-of") || item.getType().equals("repost-of") || item.getType().equals("in-reply-to") || item.getType().equals("like-of") || item.getType().equals("checkin")) {
                String ContextText = "";
                String ContextUrl = "";
                String ContextLinkText = "";
                switch (item.getType()) {
                    case "in-reply-to":
                        ContextText = "In reply to";
                        ContextUrl = item.getSubType("in-reply-to");
                        ContextLinkText = ContextUrl;
                        break;
                    case "like-of":
                        ContextText = "Like of";
                        ContextUrl = item.getSubType("like-of");
                        ContextLinkText = ContextUrl;
                        break;
                    case "repost-of":
                        ContextText = "Repost of";
                        ContextUrl = item.getSubType("repost-of");
                        ContextLinkText = ContextUrl;
                        break;
                    case "bookmark-of":
                        ContextText = "Bookmark of";
                        ContextUrl = item.getSubType("bookmark-of");
                        ContextLinkText = ContextUrl;
                        break;
                    case "checkin":
                        ContextText = "Checked in at ";
                        ContextUrl = item.getSubType("checkin-url");
                        ContextLinkText = item.getSubType("checkin");
                        break;
                }

                if (ContextText.length() > 0 && ContextUrl.length() > 0) {
                    ContextData = ContextText + " <a href=\"" + ContextUrl + "\">" + ContextLinkText + "</a>";
                }
            }

            if (ContextData.length() > 0) {
                holder.context.setVisibility(View.VISIBLE);
                //holder.context.setClickable(true);

                CharSequence sequence = Html.fromHtml(ContextData);
                SpannableStringBuilder strBuilder = new SpannableStringBuilder(sequence);
                URLSpan[] urls = strBuilder.getSpans(0, sequence.length(), URLSpan.class);
                for (URLSpan span : urls) {
                    makeLinkClickable(strBuilder, span);
                }

                holder.context.setText(strBuilder);
                holder.context.setMovementMethod(LinkMovementMethod.getInstance());
            }
            else {
                holder.context.setVisibility(View.GONE);
            }

            // Content.
            if (item.getHtmlContent().length() > 0 || item.getTextContent().length() > 0) {

                holder.content.setVisibility(View.VISIBLE);

                if (item.getHtmlContent().length() > 0) {
                    CharSequence sequence;
                    String html = item.getHtmlContent();
                    if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                        sequence = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
                    }
                    else {
                        sequence = Html.fromHtml(html);
                    }

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
                    holder.content.setText(item.getTextContent());
                }

                if (item.getTextContent().length() > 400) {
                    holder.expand.setVisibility(View.VISIBLE);
                    holder.expand.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(final View v) {
                            if (holder.content.isExpanded()) {
                                holder.content.collapse();
                                holder.expand.setText(R.string.read_more);
                            }
                            else {
                                holder.content.expand();
                                holder.expand.setText(R.string.close);
                            }
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

            // Image.
            if (item.getPhoto().length() > 0) {
                Glide.with(context)
                        .load(item.getPhoto())
                        .into(holder.image);
                holder.image.setVisibility(View.VISIBLE);
                holder.card.setVisibility(View.VISIBLE);
                holder.image.setOnClickListener(new OnImageClickListener(position));
            }
            else {
                holder.image.setVisibility(View.GONE);
                holder.card.setVisibility(View.GONE);
            }

            // Audio.
            if (item.getAudio().length() > 0) {
                holder.audio.setVisibility(View.VISIBLE);
                holder.audio.setOnClickListener(new OnAudioClickListener(position));
            }
            else {
                holder.audio.setVisibility(View.GONE);
            }

            // Button listeners.
            if (item.getUrl().length() > 0) {
                holder.bookmark.setVisibility(View.VISIBLE);
                holder.reply.setVisibility(View.VISIBLE);
                holder.like.setVisibility(View.VISIBLE);
                holder.repost.setVisibility(View.VISIBLE);
                holder.external.setVisibility(View.VISIBLE);

                holder.bookmark.setOnClickListener(new OnBookmarkClickListener(position));
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
        }

        return convertView;
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
            i.putExtra("imageUrl", item.getPhoto());
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

}
