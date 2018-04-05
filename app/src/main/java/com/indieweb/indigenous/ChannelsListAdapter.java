package com.indieweb.indigenous;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.List;

/**
 * Channels list adapter.
 */
public class ChannelsListAdapter extends BaseAdapter implements OnClickListener {
    private final Context context;
    private final List<Channel> channels;

    public ChannelsListAdapter(Context context, List<Channel> channels) {
        this.context = context;
        this.channels = channels;
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

    public View getView(final int position, View convertView, ViewGroup parent) {

        if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.channel_list_item, null);
        }

        final Channel channel = channels.get(position);
        if (channel != null) {

            // Change color of row.
            assert convertView != null;
            String color = ((position % 2) == 0) ? "#f8f7f1" :  "#ffffff";
            final LinearLayout row = (LinearLayout) convertView.findViewById(R.id.channel_row);
            row.setBackgroundColor(Color.parseColor(color));

            // Name.
            final TextView name = (TextView) convertView.findViewById(R.id.channel_name);
            String text = channel.getName();
            if (channel.getUnread() == 0) {
                text += " - no unread items";
            }
            else {
                text += " - unread items: " + channel.getUnread();
            }
            name.setText(text);

            // Set on touch listener.
            // TODO check performclick
            row.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View view, MotionEvent motionEvent) {
                    String backColor = ((position % 2) == 0) ? "#f8f7f1" :  "#ffffff";
                    switch(motionEvent.getAction()) {
                        case MotionEvent.ACTION_UP:
                            row.setBackgroundColor(Color.parseColor(backColor));
                            Intent intent = new Intent(context, TimeLineActivity.class);
                            intent.putExtra("channelId", channel.getUid());
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