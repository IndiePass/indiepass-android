package com.indieweb.indigenous.indieauth;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.accounts.AuthenticatorException;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.LaunchActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.MicropubAction;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

import java.io.IOException;
import java.util.List;

public class UsersListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<User> items;
    private LayoutInflater mInflater;
    private final User currentUser;
    private final Activity activity;
    private final RelativeLayout layout;

    UsersListAdapter(Context context, Activity activity, List<User> items, User currentUser, RelativeLayout layout) {
        this.context = context;
        this.items = items;
        this.currentUser = currentUser;
        this.activity = activity;
        this.layout = layout;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return items.size();
    }

    public User getItem(int position) {
        return items.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void onClick(View view) {}

    public static class ViewHolder {
        public TextView name;
        public TextView url;
        TextView userCurrent;
        ImageView avatar;
        TextView endpoints;
        RelativeLayout row;
        Button sync;
        Button switchAccount;
        public Button delete;
    }

    @SuppressLint("InflateParams")
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_user, null);
            holder = new ViewHolder();
            holder.userCurrent = convertView.findViewById(R.id.user_list_current);
            holder.name = convertView.findViewById(R.id.user_list_name);
            holder.url = convertView.findViewById(R.id.user_list_url);
            holder.avatar = convertView.findViewById(R.id.user_list_avatar);
            holder.endpoints = convertView.findViewById(R.id.user_list_endpoints);
            holder.delete = convertView.findViewById(R.id.itemDelete);
            holder.sync = convertView.findViewById(R.id.itemSync);
            holder.switchAccount = convertView.findViewById(R.id.itemSwitch);
            holder.row = convertView.findViewById(R.id.user_list_row);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder)convertView.getTag();
        }

        final User item = items.get(position);
        if (item != null) {

            if (item.getMe().equals(currentUser.getMe())) {
                holder.userCurrent.setVisibility(View.VISIBLE);
                holder.userCurrent.setText(R.string.current_user);
                holder.switchAccount.setVisibility(View.GONE);
            }
            else {
                holder.userCurrent.setVisibility(View.GONE);
                holder.switchAccount.setVisibility(View.VISIBLE);
                holder.switchAccount.setOnClickListener(new OnSwitchClickListener(position));
            }

            // Url.
            holder.url.setText(item.getMe());

            // Avatar.
            if (item.getAvatar().length() > 0) {
                Glide.with(context)
                        .load(item.getAvatar())
                        .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.avatar_small))
                        .into(holder.avatar);
            }

            // Name.
            if (item.getName().length() > 0) {
                holder.name.setVisibility(View.VISIBLE);
                holder.name.setText(item.getName());
            }
            else {
                holder.name.setVisibility(View.GONE);
            }

            // Endpoints.
            String endpoints = "";
            if (item.getMicropubEndpoint().length() > 0) {
                endpoints += item.getMicropubEndpoint() + "\n";
            }
            if (item.getMicropubMediaEndpoint().length() > 0) {
                endpoints += item.getMicropubMediaEndpoint() + "\n";
            }
            if (item.getMicrosubEndpoint().length() > 0) {
                endpoints += item.getMicrosubEndpoint() + "\n";
            }
            if (item.getTokenEndpoint().length() > 0) {
                endpoints += item.getTokenEndpoint() + "\n";
            }
            if (item.getAuthorizationEndpoint().length() > 0) {
                endpoints += item.getAuthorizationEndpoint() + "\n";
            }
            holder.endpoints.setText(endpoints);

            // Button listeners.
            holder.sync.setOnClickListener(new OnSyncClickListener(position));
            holder.delete.setOnClickListener(new OnDeleteClickListener(position));
        }

        return convertView;
    }

    // Switch listener.
    class OnSwitchClickListener implements OnClickListener {

        int position;

        OnSwitchClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final User user = items.get(this.position);
            new Accounts(context).switchAccount(activity, user, layout);
        }
    }

    // Sync listener.
    class OnSyncClickListener implements OnClickListener {

        int position;

        OnSyncClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final User user = items.get(this.position);
            Snackbar.make(layout, String.format(context.getString(R.string.account_sync), user.getMe()), Snackbar.LENGTH_SHORT).show();
            new Endpoints(context, user, layout).refresh();
            new MicropubAction(context, user, layout).refreshConfig();
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
            final User user = items.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(String.format(context.getString(R.string.account_delete_confirm), user.getMe()));
            builder.setPositiveButton(context.getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {

                    AccountManager accountManager = AccountManager.get(context);

                    if (Build.VERSION.SDK_INT < 23) {
                        accountManager.removeAccount(user.getAccount(), new AccountManagerCallback<Boolean>() {
                            @Override
                            public void run(AccountManagerFuture<Boolean> accountManagerFuture) {
                                try {
                                    if (accountManagerFuture.getResult()) {
                                        handleSuccessRemoval(user, position);
                                    }
                                }
                                catch (android.accounts.OperationCanceledException | IOException | AuthenticatorException e) {
                                    Snackbar.make(layout, context.getString(R.string.account_delete_error), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }, null);
                    }
                    else {
                        accountManager.removeAccount(user.getAccount(), activity, new AccountManagerCallback<Bundle>() {
                            @Override
                            public void run(AccountManagerFuture<Bundle> accountManagerFuture) {
                                try {
                                    if (accountManagerFuture.getResult() != null) {
                                        handleSuccessRemoval(user, position);
                                    }
                                }
                                catch (android.accounts.OperationCanceledException | AuthenticatorException | IOException e) {
                                    Snackbar.make(layout, context.getString(R.string.account_delete_error), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }, null);
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

    /**
     * Handle success removal.
     *
     * @param user
     *   The user that was removed.
     * @param position
     *   The position in the adapter.
     */
    private void handleSuccessRemoval(User user, int position) {
        new IndieAuthAction(context, user).revoke();
        if (user.getMe().equals(currentUser.getMe())) {
            Snackbar.make(layout, String.format(context.getString(R.string.account_removed), user.getMe()), Snackbar.LENGTH_SHORT).show();
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    Intent main = new Intent(context, LaunchActivity.class);
                    context.startActivity(main);
                    activity.finish();
                }
            }, 700);
        }
        else {
            Snackbar.make(layout, String.format(context.getString(R.string.account_removed), user.getMe()), Snackbar.LENGTH_SHORT).show();
            items.remove(position);
            notifyDataSetChanged();
        }
    }
}
