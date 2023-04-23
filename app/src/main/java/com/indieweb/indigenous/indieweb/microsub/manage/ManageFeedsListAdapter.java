package com.indieweb.indigenous.indieweb.microsub.manage;

import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.*;
import androidx.appcompat.app.AlertDialog;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.indieweb.microsub.MicrosubAction;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.Feed;
import com.indieweb.indigenous.model.User;

import java.util.ArrayList;
import java.util.List;

/**
 * Feed items list adapter.
 */
public class ManageFeedsListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<Feed> items;
    private final LayoutInflater mInflater;
    private final User user;
    private final RelativeLayout layout;

    ManageFeedsListAdapter(Context context, List<Feed> items, User user, RelativeLayout layout) {
        this.context = context;
        this.items = items;
        this.user = user;
        this.layout = layout;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    public void onClick(View view) {
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_manage_feed, null);
            holder = new ViewHolder();
            holder.url = convertView.findViewById(R.id.url);
            holder.move = convertView.findViewById(R.id.feedMove);
            holder.delete = convertView.findViewById(R.id.feedDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Feed item = items.get(position);
        if (item != null) {
            holder.url.setText(item.getUrl());
            holder.move.setOnClickListener(new OnMoveClickListener(position));
            holder.delete.setOnClickListener(new OnDeleteClickListener(position));
        }

        return convertView;
    }

    public static class ViewHolder {
        public TextView url;
        public Button delete;
        public Button move;
    }

    // Delete listener.
    class OnDeleteClickListener implements OnClickListener {

        final int position;

        OnDeleteClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final Feed feed = items.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(String.format(context.getString(R.string.delete_feed_confirm), feed.getUrl()));
            builder.setPositiveButton(context.getString(R.string.delete_post), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    new MicrosubAction(context, user, layout).deleteFeed(feed.getUrl(), feed.getChannel());
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

    // Move listener.
    class OnMoveClickListener implements OnClickListener {

        final int position;

        OnMoveClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final Feed feed = items.get(this.position);
            final Indigenous app = Indigenous.getInstance();

            final List<Channel> channels = app.getChannelsList();
            final List<CharSequence> displayValues = new ArrayList<>();
            for (Channel channel : channels) {
                String uid = channel.getUid();
                if (!uid.equals("notifications") && !uid.equals("global") && !uid.equals(feed.getChannel())) {
                    displayValues.add(channel.getName());
                }
            }
            final CharSequence[] channelItems = displayValues.toArray(new CharSequence[displayValues.size()]);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.feed_move_to_channel));
            builder.setSingleChoiceItems(channelItems, -1, null);
            builder.setPositiveButton(context.getString(R.string.move_item), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    ListView lw = ((AlertDialog) dialog).getListView();
                    Object checkedItem = lw.getAdapter().getItem(lw.getCheckedItemPosition());
                    if (checkedItem != null) {
                        for (Channel channel : channels) {
                            if (channel.getName() == checkedItem) {
                                new MicrosubAction(context, user, layout).subscribe(feed.getUrl(), channel.getUid(), true);
                                items.remove(position);
                                notifyDataSetChanged();
                                break;
                            }
                        }
                    }
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
