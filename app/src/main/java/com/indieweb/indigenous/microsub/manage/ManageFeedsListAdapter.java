package com.indieweb.indigenous.microsub.manage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.support.v7.widget.PopupMenu;
import android.text.Html;
import android.text.SpannableStringBuilder;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.indieweb.indigenous.DebugActivity;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.post.BookmarkActivity;
import com.indieweb.indigenous.micropub.post.LikeActivity;
import com.indieweb.indigenous.micropub.post.ReplyActivity;
import com.indieweb.indigenous.micropub.post.RepostActivity;
import com.indieweb.indigenous.micropub.post.RsvpActivity;
import com.indieweb.indigenous.microsub.MicrosubAction;
import com.indieweb.indigenous.microsub.timeline.TimelineAudioActivity;
import com.indieweb.indigenous.microsub.timeline.TimelineImageActivity;
import com.indieweb.indigenous.microsub.timeline.TimelineVideoActivity;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.Feed;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;
import com.indieweb.indigenous.widget.ExpandableTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Feed items list adapter.
 */
public class ManageFeedsListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<Feed> items;
    private LayoutInflater mInflater;
    private final User user;
    private final String channelId;

    ManageFeedsListAdapter(Context context, List<Feed> items, User user, String channelId) {
        this.context = context;
        this.items = items;
        this.user = user;
        this.channelId = channelId;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return items.size();
    }

    public Feed getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void onClick(View view) {}

    public static class ViewHolder {
        public TextView url;
        public Button delete;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_manage_feed, null);
            holder = new ViewHolder();
            holder.url = convertView.findViewById(R.id.url);
            holder.delete = convertView.findViewById(R.id.feedDelete);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        final Feed item = items.get(position);
        if (item != null) {
            holder.url.setText(item.getUrl());
            holder.delete.setOnClickListener(new OnDeleteClickListener(position));
        }

        return convertView;
    }

    // Delete listener.
    class OnDeleteClickListener implements OnClickListener {

        int position;

        OnDeleteClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final Feed feed = items.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Are you sure you want to delete feed '"+ feed.getUrl() +"' ?");
            builder.setPositiveButton(context.getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    new MicrosubAction(context, user).deleteFeed(feed.getUrl(), feed.getChannel());
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
        }
    }

}
