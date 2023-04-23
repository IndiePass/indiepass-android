package com.indieweb.indiepass.indieweb.micropub.source;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.indieweb.indiepass.R;
import com.indieweb.indiepass.indieweb.micropub.MicropubAction;
import com.indieweb.indiepass.post.ReplyActivity;
import com.indieweb.indiepass.post.UpdateActivity;
import com.indieweb.indiepass.model.PostListItem;
import com.indieweb.indiepass.model.User;
import com.indieweb.indiepass.util.Utility;
import com.indieweb.indiepass.widget.ExpandableTextView;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.indieweb.indiepass.MainActivity.UPDATE_POST;

/**
 * Source post list items list adapter.
 */
public class PostListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<PostListItem> items;
    private final LayoutInflater mInflater;
    private final boolean showUpdateButton;
    private final boolean showDeleteButton;
    private final User user;
    private final RelativeLayout layout;

    PostListAdapter(Context context, List<PostListItem> items, User user, boolean showUpdateButton, boolean showDeleteButton, RelativeLayout layout) {
        this.context = context;
        this.items = items;
        this.user = user;
        this.layout = layout;
        this.showUpdateButton = showUpdateButton;
        this.showDeleteButton = showDeleteButton;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return items.size();
    }

    public PostListItem getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void onClick(View view) {}

    public static class ViewHolder {
        public TextView name;
        public TextView url;
        public TextView published;
        public TextView postStatus;
        public Button expand;
        public ExpandableTextView content;
        public LinearLayout row;
        public Button update;
        public Button delete;
        public Button external;
        public Button reply;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_source_post, null);
            holder = new ViewHolder();
            holder.published = convertView.findViewById(R.id.source_post_list_published);
            holder.postStatus = convertView.findViewById(R.id.source_post_list_post_status);
            holder.name = convertView.findViewById(R.id.source_post_list_name);
            holder.url = convertView.findViewById(R.id.source_post_list_url);
            holder.content = convertView.findViewById(R.id.source_post_list_content);
            holder.expand = convertView.findViewById(R.id.source_post_list_content_more);
            holder.update = convertView.findViewById(R.id.itemUpdate);
            holder.delete = convertView.findViewById(R.id.itemDelete);
            holder.external = convertView.findViewById(R.id.itemExternal);
            holder.reply = convertView.findViewById(R.id.itemReply);
            holder.row = convertView.findViewById(R.id.source_post_list_item_row);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        final PostListItem item = items.get(position);
        if (item != null) {

            // Color of row.
            int color = context.getResources().getColor(R.color.listRowBackgroundColor);
            holder.row.setBackgroundColor(color);

            // Published.
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MM yyyy HH:mm");
            Date result;
            try {
                result = formatIn.parse(item.getPublished());
                holder.published.setVisibility(View.VISIBLE);
                holder.published.setText(formatOut.format(result));
            }
            catch (ParseException ignored) {
                holder.published.setVisibility(View.GONE);
            }

            // Post status.
            if (item.getPostStatus().length() > 0) {
                holder.postStatus.setVisibility(View.VISIBLE);
                holder.postStatus.setText(String.format(context.getString(R.string.post_status), item.getPostStatus()));
            }
            else {
                holder.postStatus.setVisibility(View.GONE);
            }

            // Url.
            if (item.getUrl().length() > 0) {
                holder.url.setVisibility(View.VISIBLE);
                holder.url.setText(item.getUrl());
            }
            else {
                holder.url.setVisibility(View.GONE);
            }

            // Name.
            if (item.getName().length() > 0) {
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(item.getName());
            }
            else {
                holder.name.setVisibility(View.GONE);
            }

            // Content.
            if (item.getContent().length() > 0) {

                CharSequence sequence;
                String html = item.getContent();
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.N) {
                    sequence = Html.fromHtml(html, Html.FROM_HTML_MODE_LEGACY);
                }
                else {
                    sequence = Html.fromHtml(html);
                }

                // Trim end.
                sequence = Utility.trim(sequence);
                holder.content.setVisibility(View.VISIBLE);
                holder.content.setText(sequence);

                if (item.getContent().length() > 400) {
                    holder.expand.setVisibility(View.VISIBLE);
                    holder.expand.setOnClickListener(new OnClickListener()
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

            // Button listeners.
            if (item.getUrl().length() > 0) {
                holder.external.setOnClickListener(new OnExternalClickListener(position));
                holder.reply.setOnClickListener(new OnReplyClickListener(position));

                if (showUpdateButton) {
                    holder.update.setVisibility(View.VISIBLE);
                    holder.update.setOnClickListener(new OnUpdateClickListener(position));
                }

                if (showDeleteButton) {
                    holder.delete.setVisibility(View.VISIBLE);
                    holder.delete.setOnClickListener(new OnDeleteClickListener(position));
                }
            }
            else {
                holder.update.setVisibility(View.GONE);
                holder.external.setVisibility(View.GONE);
                holder.reply.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    // Update listener.
    class OnUpdateClickListener implements OnClickListener {

        final int position;

        OnUpdateClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, UpdateActivity.class);
            PostListItem item = items.get(this.position);
            i.putExtra("url", item.getUrl());
            i.putExtra("status", item.getPostStatus());
            ((Activity) context).startActivityForResult(i, UPDATE_POST);
        }
    }

    // Reply listener.
    class OnReplyClickListener implements OnClickListener {

        final int position;

        OnReplyClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final PostListItem item = items.get(this.position);
            Intent CreateReply = new Intent(context, ReplyActivity.class);
            CreateReply.putExtra("incomingText", item.getUrl());
            context.startActivity(CreateReply);
        }
    }

    // External listener.
    class OnExternalClickListener implements OnClickListener {

        final int position;

        OnExternalClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            PostListItem item = items.get(this.position);

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

    // Delete listener.
    class OnDeleteClickListener implements OnClickListener {

        final int position;

        OnDeleteClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final PostListItem item = items.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.post_delete_confirm));
            builder.setPositiveButton(context.getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    new MicropubAction(context, user, layout).deleteItem(item.getUrl());
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
