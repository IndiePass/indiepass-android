package com.indieweb.indigenous.microsub.manage;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.microsub.MicrosubAction;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.User;

import java.util.Collections;
import java.util.List;

/**
 * Manage channels list adapter.
 */
public class ManageChannelListAdapter extends RecyclerView.Adapter<ManageChannelListAdapter.ViewHolder> implements ItemMoveCallback.ItemTouchHelperContract {

    private boolean moved = false;
    private boolean isShare;
    private String url;
    private final Context context;
    private final List<Channel> channels;
    private final User user;
    private final StartDragListener mStartDragListener;

    @Override
    public void onRowMoved(int fromPosition, int toPosition) {

        moved = true;
        if (fromPosition < toPosition) {
            for (int i = fromPosition; i < toPosition; i++) {
                Collections.swap(channels, i, i + 1);
            }
        }
        else {
            for (int i = fromPosition; i > toPosition; i--) {
                Collections.swap(channels, i, i - 1);
            }
        }

        notifyItemMoved(fromPosition, toPosition);
    }

    @Override
    public void onRowSelected(ManageChannelListAdapter.ViewHolder myViewHolder) {
        int color = context.getResources().getColor(R.color.listRowBackgroundColorTouched);
        myViewHolder.rowView.setBackgroundColor(color);
    }

    @Override
    public void onRowClear(ManageChannelListAdapter.ViewHolder myViewHolder) {
        int color = context.getResources().getColor(R.color.listRowBackgroundColor);
        myViewHolder.rowView.setBackgroundColor(color);

        // Only send request when an item has moved.
        if (moved) {
            new MicrosubAction(context, user).orderChannels(channels);
        }

        moved = false;

    }

    ManageChannelListAdapter(Context context, List<Channel> channels, User user, StartDragListener startDragListener, boolean isShare, String url) {
        this.context = context;
        this.channels = channels;
        this.user = user;
        this.isShare = isShare;
        this.url = url;
        this.mStartDragListener = startDragListener;
    }

    private Channel getItem(int position) {
        return channels.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_item_manage_channel, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, final int position) {

        holder.name.setText(channels.get(position).getName() + " (id: " + channels.get(position).getUid() + ")");

        if (isShare) {
            holder.drag.setVisibility(View.GONE);
            holder.update.setVisibility(View.GONE);
            holder.delete.setVisibility(View.GONE);
        }
        else {
            holder.update.setOnClickListener(new ManageChannelListAdapter.OnUpdateClickListener(position));
            holder.delete.setOnClickListener(new ManageChannelListAdapter.OnDeleteClickListener(position));
        }

        holder.rowView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        int downColor = context.getResources().getColor(R.color.listRowBackgroundColorTouched);
                        holder.rowView.setBackgroundColor(downColor);
                        break;
                    case MotionEvent.ACTION_CANCEL:
                        int cancelColor = context.getResources().getColor(R.color.listRowBackgroundColor);
                        holder.rowView.setBackgroundColor(cancelColor);
                        break;
                    case MotionEvent.ACTION_UP:
                        int color = context.getResources().getColor(R.color.listRowBackgroundColor);
                        Channel channel = channels.get(position);
                        holder.rowView.setBackgroundColor(color);
                        if (isShare) {
                            Intent intent = new Intent(context, FeedActivity.class);
                            intent.putExtra("channelId", channel.getUid());
                            intent.putExtra("channelName", channel.getName());
                            intent.putExtra("url", url);
                            context.startActivity(intent);
                        }
                        else {
                            Intent intent = new Intent(context, ManageFeedsActivity.class);
                            intent.putExtra("channelId", channel.getUid());
                            intent.putExtra("channelName", channel.getName());
                            context.startActivity(intent);
                        }
                        break;
                }
                return true;
            }
        });

        holder.drag.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mStartDragListener.requestDrag(holder);
                }
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View rowView;
        public TextView name;
        public Button update;
        public Button delete;
        public Button drag;

        ViewHolder(View itemView) {
            super(itemView);

            rowView = itemView;
            name = itemView.findViewById(R.id.channel_name);
            drag = itemView.findViewById(R.id.channelDrag);
            update = itemView.findViewById(R.id.channelUpdate);
            delete = itemView.findViewById(R.id.channelDelete);
        }
    }

    // Update listener.
    class OnUpdateClickListener implements OnClickListener {

        int position;

        OnUpdateClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final Channel channel = getItem(this.position);

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