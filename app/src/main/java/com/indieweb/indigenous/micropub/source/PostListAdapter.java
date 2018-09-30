package com.indieweb.indigenous.micropub.source;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.MicropubActionDelete;
import com.indieweb.indigenous.micropub.post.UpdateActivity;
import com.indieweb.indigenous.model.PostListItem;
import com.indieweb.indigenous.model.User;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import at.blogc.android.views.ExpandableTextView;

/**
 * Source post list items list adapter.
 */
public class PostListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<PostListItem> items;
    private LayoutInflater mInflater;
    private final boolean showDeleteButton;
    private final User user;

    PostListAdapter(Context context, List<PostListItem> items, User user, boolean showDeleteButton) {
        this.context = context;
        this.items = items;
        this.user = user;
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
        public TextView published;
        public Button expand;
        public ExpandableTextView content;
        public LinearLayout row;
        public Button update;
        public Button delete;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.source_post_list_item, null);
            holder = new ViewHolder();
            holder.published = convertView.findViewById(R.id.source_post_list_published);
            holder.name = convertView.findViewById(R.id.source_post_list_name);
            holder.content = convertView.findViewById(R.id.source_post_list_content);
            holder.expand = convertView.findViewById(R.id.source_post_list_content_more);
            holder.update = convertView.findViewById(R.id.itemUpdate);
            holder.delete = convertView.findViewById(R.id.itemDelete);
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
                holder.update.setOnClickListener(new OnUpdateClickListener(position));

                if (showDeleteButton) {
                    holder.delete.setVisibility(View.VISIBLE);
                    holder.delete.setOnClickListener(new OnDeleteClickListener(position));
                }
                else {
                }
            }
            else {
                holder.update.setVisibility(View.GONE);
            }
        }

        return convertView;
    }

    // Update listener.
    class OnUpdateClickListener implements OnClickListener {

        int position;

        OnUpdateClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Intent i = new Intent(context, UpdateActivity.class);
            PostListItem item = items.get(this.position);
            i.putExtra("incomingText", item.getUrl());
            context.startActivity(i);
        }
    }

    // Delete listener.
    class OnDeleteClickListener implements OnClickListener {

        int position;

        OnDeleteClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final PostListItem item = items.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Are you sure you want to delete this post ?");
            builder.setPositiveButton(context.getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    new MicropubActionDelete(context, user, item.getUrl()).deletePost();
                    items.remove(position);
                    // TODO what happens if this is on page 2 for instance
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
