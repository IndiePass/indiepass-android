package com.indieweb.indigenous;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.OvershootInterpolator;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.bumptech.glide.Glide;
import at.blogc.android.views.ExpandableTextView;

import java.util.List;


/**
 * Timeline items list adapter.
 */
public class TimelineListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<TimelineItem> items;
    private LayoutInflater mInflater;

    public TimelineListAdapter(Context context, List<TimelineItem> items) {
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
        public TextView author;
        public TextView name;
        public Button expand;
        public ExpandableTextView content;
        public ImageView image;
        public LinearLayout row;
    }

    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.timeline_list_item, null);
            holder = new ViewHolder();
            holder.author = convertView.findViewById(R.id.timeline_author);
            holder.name = convertView.findViewById(R.id.timeline_name);
            holder.content = convertView.findViewById(R.id.timeline_content);
            holder.content.setInterpolator(new OvershootInterpolator());
            holder.expand = convertView.findViewById(R.id.timeline_content_more);
            holder.image = convertView.findViewById(R.id.timeline_image);
            holder.row = convertView.findViewById(R.id.timeline_item_row);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        final TimelineItem item = items.get(position);
        if (item != null) {

            String color = ((position % 2) == 0) ? "#f8f7f1" :  "#ffffff";
            holder.row.setBackgroundColor(Color.parseColor(color));

            // Author.
            holder.author.setText(item.getAuthorName());

            // Name.
            holder.name.setText(item.getName());

            // Content.
            holder.content.setText(item.getContent());
            if (item.getContent().length() > 400) {
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

            // Image.
            if (item.getPhoto().length() > 0) {
                Glide.with(context).load(item.getPhoto()).into(holder.image);
                holder.image.setVisibility(View.VISIBLE);
            }
            else {
                holder.image.setVisibility(View.GONE);
            }
        }

        return convertView;
    }
}