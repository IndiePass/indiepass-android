package com.indieweb.indigenous.microsub.manage;

import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.microsub.MicrosubAction;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.User;

import java.util.List;

/**
 * Channels list adapter.
 */
public class ManageChannelListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<Channel> channels;
    private LayoutInflater mInflater;
    private final User user;

    public ManageChannelListAdapter(Context context, List<Channel> channels, User user) {
        this.context = context;
        this.channels = channels;
        this.user = user;
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
        public LinearLayout row;
        public Button update;
        public Button delete;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_manage_channel, null);
            holder = new ViewHolder();
            holder.row = convertView.findViewById(R.id.channel_row);
            holder.name = convertView.findViewById(R.id.channel_name);
            holder.update = convertView.findViewById(R.id.channelUpdate);
            holder.delete = convertView.findViewById(R.id.channelDelete);
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

            // Name.
            holder.name.setText(channel.getName());

            // Buttons
            holder.update.setOnClickListener(new ManageChannelListAdapter.OnUpdateClickListener(position));
            holder.delete.setOnClickListener(new ManageChannelListAdapter.OnDeleteClickListener(position));


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
            final Channel channel = channels.get(this.position);

            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Update channel");
            View view = LayoutInflater.from(context).inflate(R.layout.dialog_single_input, null);
            final EditText input = view.findViewById(R.id.editText);
            input.setText(channel.getName());
            input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    input.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            builder.setView(view);

            builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    String channelName = input.getText().toString();
                    if (!channelName.equals(channel.getName())) {
                        new MicrosubAction(context, user).updateChannel(channelName, channel.getUid());
                        channels.get(position).setName(channelName);
                        notifyDataSetChanged();
                    }
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
            input.requestFocus();
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
            final Channel channel = channels.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Are you sure you want to delete channel '"+ channel.getName() +"' ?");
            builder.setPositiveButton(context.getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    new MicrosubAction(context, user).deleteChannel(channel.getUid());
                    channels.remove(position);
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