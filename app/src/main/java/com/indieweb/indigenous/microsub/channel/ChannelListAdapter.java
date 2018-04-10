package com.indieweb.indigenous.microsub.channel;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.microsub.timeline.TimelineActivity;

import java.util.List;

/**
 * Channels list adapter.
 */
public class ChannelListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<Channel> channels;
    private LayoutInflater mInflater;

    public ChannelListAdapter(Context context, List<Channel> channels) {
        this.context = context;
        this.channels = channels;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return channels.size();
    }

    public Channel getItem(int position) {
        return channels.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void onClick(View view) {

    }

    public static class ViewHolder {
        public TextView name;
        public TextView unread;
        public LinearLayout row;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.channel_list_item, null);
            holder = new ViewHolder();
            holder.row = convertView.findViewById(R.id.channel_row);
            holder.name = convertView.findViewById(R.id.channel_name);
            holder.unread = convertView.findViewById(R.id.channel_unread);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Channel channel = channels.get(position);
        if (channel != null) {

            // Change color of row.
            String color = ((position % 2) == 0) ? "#f8f7f1" :  "#ffffff";
            holder.row = convertView.findViewById(R.id.channel_row);
            holder.row.setBackgroundColor(Color.parseColor(color));

            // Name.
            holder.name.setText(channel.getName());

            // Unread.
            Integer unreadText = channel.getUnread();
            if (unreadText > 0) {
                holder.unread.setVisibility(View.VISIBLE);
                holder.unread.setText(unreadText);
            }
            else {
                holder.unread.setVisibility(View.GONE);
            }

            // Set on touch listener.
            // TODO check performclick
            holder.row.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    String backColor = ((position % 2) == 0) ? "#f8f7f1" :  "#ffffff";
                    switch(motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            holder.row.setBackgroundColor(Color.parseColor(backColor));
                            Intent intent = new Intent(context, TimelineActivity.class);
                            intent.putExtra("channelId", channel.getUid());
                            intent.putExtra("channelName", channel.getName());
                            intent.putExtra("unread", channel.getUnread());
                            context.startActivity(intent);
                            break;
                    }
                    return true;
                }
            });

        }

        return convertView;
    }
}