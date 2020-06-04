package com.indieweb.indigenous.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import androidx.appcompat.app.AlertDialog;

import android.os.Handler;
import android.util.Log;
import android.widget.RelativeLayout;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.LaunchActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.indieauth.IndieAuthActivity;
import com.indieweb.indigenous.model.User;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;
import static com.indieweb.indigenous.indieauth.IndieAuthActivity.ACCOUNT_TYPE;

public class Accounts {

    private final Context context;

    public Accounts(Context context) {
        this.context = context;
    }

    /**
     * Gets the default user.
     *
     * @return User
     */
    public User getDefaultUser() {
        User user = new User();

        SharedPreferences preferences = context.getSharedPreferences("indigenous", MODE_PRIVATE);
        String accountName = preferences.getString("account", "");
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccountsByType(ACCOUNT_TYPE);
        if (accounts.length > 0) {
            for (Account account : accounts) {
                if (account.name.equals(accountName)) {
                    user.setValid(true);
                    user.setMe(accountName);

                    String token = "";
                    try {
                        token = accountManager.peekAuthToken(account, IndieAuthActivity.TOKEN_TYPE);
                    }
                    catch (Exception ignored) {}

                    user.setAccessToken(token);
                    user.setAvatar(accountManager.getUserData(account, "author_avatar"));
                    user.setName(accountManager.getUserData(account, "author_name"));
                    user.setTokenEndpoint(accountManager.getUserData(account, "token_endpoint"));
                    user.setAuthorizationEndpoint(accountManager.getUserData(account, "authorization_endpoint"));
                    user.setMicrosubEndpoint(accountManager.getUserData(account, "microsub_endpoint"));
                    user.setMicropubEndpoint(accountManager.getUserData(account, "micropub_endpoint"));
                    user.setMicropubMediaEndpoint(accountManager.getUserData(account, "micropub_media_endpoint"));
                    user.setSyndicationTargets(accountManager.getUserData(account, "syndication_targets"));
                    user.setPostTypes(accountManager.getUserData(account, "post_types"));
                    user.setAccount(account);
                }
            }
        }
        // Anonymous user.
        else {
            user.setValid(true);
            user.setAnonymous(true);
            user.setMe("https://indigenous.realize.be");
            user.setName("Anonymous");
            user.setAccessToken(Preferences.getPreference(context, "anonymous_token", ""));
            user.setMicrosubEndpoint(Preferences.getPreference(context, "anonymous_microsub_endpoint", context.getString(R.string.anonymous_microsub_endpoint)));
            user.setMicropubEndpoint(Preferences.getPreference(context, "anonymous_micropub_endpoint", ""));
        }

        return user;
    }

    /**
     * Get number of users.
     *
     * @return int
     */
    public int getCount() {
        int numberOfUsers = 0;

        Account[] AllAccounts = this.getAllAccounts();
        numberOfUsers = AllAccounts.length;

        return numberOfUsers;
    }

    /**
     * Switch account dialog.
     *
     * @param activity
     *   The current activity
     * @param user
     *   The user to switch to.
     * @param layout
     *   The current layout.
     */
    public void switchAccount(final Activity activity, final User user, final RelativeLayout layout) {
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(String.format(context.getString(R.string.account_switch), user.getMe()));
        builder.setPositiveButton(context.getString(R.string.switch_account),new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                Snackbar.make(layout, String.format(context.getString(R.string.account_selected), user.getMe()), Snackbar.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = context.getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                editor.putString("account", user.getAccount().name);
                editor.apply();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent Main = new Intent(context, LaunchActivity.class);
                        context.startActivity(Main);
                        activity.finish();
                    }
                }, 700);

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

    /**
     * Select account dialog.
     *
     * @param activity
     *   The current activity
     * @param layout
     *   The current layout
     *
     * TODO we have a couple of variations of this code in other places, try to merge them.
     */
    public void selectAccount(final Activity activity, final RelativeLayout layout) {
        final List<String> accounts = new ArrayList<>();

        final Account[] AllAccounts = this.getAllAccounts();
        for (Account account: AllAccounts) {
            accounts.add(account.name);
        }

        final CharSequence[] accountItems = accounts.toArray(new CharSequence[accounts.size()]);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle(activity.getString(R.string.account_select));

        builder.setCancelable(true);
        builder.setItems(accountItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                Snackbar.make(layout, String.format(context.getString(R.string.account_selected), accounts.get(index)), Snackbar.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = context.getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                editor.putString("account", accounts.get(index));
                editor.apply();
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        Intent Main = new Intent(context, LaunchActivity.class);
                        context.startActivity(Main);
                        activity.finish();
                    }
                }, 700);
            }
        });
        builder.show();
    }

    /**
     * Returns all accounts.
     *
     * @return Account[]
     */
    public Account[] getAllAccounts() {
        AccountManager accountManager = AccountManager.get(context);
        return accountManager.getAccountsByType(ACCOUNT_TYPE);
    }

    /**
     * Returns users.
     *
     * @return User[]
     */
    public List<User> getAllUsers() {
        List<User> users = new ArrayList<>();
        AccountManager accountManager = AccountManager.get(context);
        for (Account a : accountManager.getAccountsByType(ACCOUNT_TYPE)) {
            User user = new User();
            user.setAccount(a);
            user.setMe(a.name);
            String token = "";
            try {
                token = accountManager.peekAuthToken(a, IndieAuthActivity.TOKEN_TYPE);
            }
            catch (Exception ignored) {}

            user.setAccessToken(token);
            user.setAvatar(accountManager.getUserData(a, "author_avatar"));
            user.setName(accountManager.getUserData(a, "author_name"));
            user.setTokenEndpoint(accountManager.getUserData(a, "token_endpoint"));
            user.setAuthorizationEndpoint(accountManager.getUserData(a, "authorization_endpoint"));
            user.setMicrosubEndpoint(accountManager.getUserData(a, "microsub_endpoint"));
            user.setMicropubEndpoint(accountManager.getUserData(a, "micropub_endpoint"));
            user.setMicropubMediaEndpoint(accountManager.getUserData(a, "micropub_media_endpoint"));
            user.setSyndicationTargets(accountManager.getUserData(a, "syndication_targets"));
            user.setPostTypes(accountManager.getUserData(a, "post_types"));
            user.setAccount(a);
            users.add(user);
        }
        return users;
    }

    /**
     * Return a specific user.
     *
     * @param name
     *   The user to get.
     * @param checkWithoutProtocol
     *   Whether to seek based on the protocol or not.
     *
     * @return User
     */
    public User getUser(String name, boolean checkWithoutProtocol) {
        User user = null;
        List<User> users = this.getAllUsers();
        for (User u: users) {

            if (checkWithoutProtocol) {
                if (u.getMeWithoutProtocol().equals(name)) {
                    user = u;
                }
            }
            else {
                if (u.getMe().equals(name)) {
                    user = u;
                }
            }
        }

        return user;
    }

}
