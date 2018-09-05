package com.indieweb.indigenous.microsub.channel;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.microsub.timeline.TimelineActivity;
import com.indieweb.indigenous.model.Channel;

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
        public int position;
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

            holder.position = position;

            // Color of row.
            int color = context.getResources().getColor(R.color.listRowBackgroundColor);
            holder.row.setBackgroundColor(color);

            // Get row id.
            holder.row = convertView.findViewById(R.id.channel_row);

            // Name.
            holder.name.setText(channel.getName());

            // Unread.
            Integer unreadText = channel.getUnread();
            if (unreadText > 0) {
                holder.unread.setVisibility(View.VISIBLE);
                holder.unread.setText(String.valueOf(unreadText));
            }
            else {
                holder.unread.setVisibility(View.GONE);
            }

            // Set on touch listener.
            convertView.setOnTouchListener(eventTouch);
        }

        return convertView;
    }

    /**
     * OnTouchListener for channel row.
     */
    private View.OnTouchListener eventTouch = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View v, MotionEvent motionEvent) {
            ViewHolder holder = (ViewHolder)v.getTag();
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
                    Channel channel = channels.get(position);
                    holder.row.setBackgroundColor(color);
                    Intent intent = new Intent(context, TimelineActivity.class);
                    intent.putExtra("channelId", channel.getUid());
                    intent.putExtra("channelName", channel.getName());
                    intent.putExtra("unread", channel.getUnread());
                    channels.get(position).setUnread(0);
                    holder.unread.setVisibility(View.GONE);
                    context.startActivity(intent);
                    break;
            }
            return true;
        }
    };

}