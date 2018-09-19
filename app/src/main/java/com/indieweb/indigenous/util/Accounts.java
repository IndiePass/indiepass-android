package com.indieweb.indigenous.util;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.support.v7.app.AlertDialog;
import android.widget.Toast;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.indieauth.IndieAuthActivity;
import com.indieweb.indigenous.model.User;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class Accounts {

    private final Context context;

    public Accounts(Context context) {
        this.context = context;
    }

    /**
     * Gets the current user.
     *
     * @return User
     */
    public User getCurrentUser() {
        User user = new User();

        SharedPreferences preferences = context.getSharedPreferences("indigenous", MODE_PRIVATE);
        String accountName = preferences.getString("account", "");
        AccountManager accountManager = AccountManager.get(context);
        Account[] accounts = accountManager.getAccounts();
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
                    user.setTokenEndpoint(accountManager.getUserData(account, "token_endpoint"));
                    user.setAuthorizationEndpoint(accountManager.getUserData(account, "authorization_endpoint"));
                    user.setMicrosubEndpoint(accountManager.getUserData(account, "microsub_endpoint"));
                    user.setMicropubEndpoint(accountManager.getUserData(account, "micropub_endpoint"));
                    user.setMicropubMediaEndpoint(accountManager.getUserData(account, "micropub_media_endpoint"));
                    // TODO we should convert this already into a map
                    user.setSyndicationTargets(accountManager.getUserData(account, "syndication_targets"));
                    // TODO we should convert this already into a map
                    user.setPostTypes(accountManager.getUserData(account, "post_types"));
                    user.setAccount(account);
                }
            }
        }

        return user;
    }

    /**
     * Switch account dialog.
     *
     * @param activity
     *   The current activity
     */
    public void switchAccount(final Activity activity) {
        final List<String> accounts = new ArrayList<>();

        final User currentUser = new Accounts(context).getCurrentUser();
        final Account[] AllAccounts = this.getAllAccounts();
        for (Account account: AllAccounts) {
            accounts.add(account.name);
        }

        final CharSequence[] accountItems = accounts.toArray(new CharSequence[accounts.size()]);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Switch account");

        builder.setPositiveButton(context.getString(R.string.add_new_account),new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog,int id) {
                Intent IndieAuth = new Intent(context, IndieAuthActivity.class);
                context.startActivity(IndieAuth);
            }
        });
        builder.setCancelable(true);
        builder.setItems(accountItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                if (!accounts.get(index).equals(currentUser.getMe())) {
                    SharedPreferences.Editor editor = context.getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                    editor.putString("account", accounts.get(index));
                    editor.apply();
                    Intent Main = new Intent(context, com.indieweb.indigenous.MainActivity.class);
                    context.startActivity(Main);
                    activity.finish();
                }

            }
        });
        builder.show();
    }

    /**
     * Set account dialog.
     *
     * @param activity
     *   The current activity
     */
    public void setAccount(final Activity activity) {
        final List<String> accounts = new ArrayList<>();

        final Account[] AllAccounts = this.getAllAccounts();
        for (Account account: AllAccounts) {
            accounts.add(account.name);
        }

        final CharSequence[] accountItems = accounts.toArray(new CharSequence[accounts.size()]);
        final AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Set account");

        builder.setCancelable(true);
        builder.setItems(accountItems, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int index) {
                Toast.makeText(context, "Account set to " + accounts.get(index), Toast.LENGTH_SHORT).show();
                SharedPreferences.Editor editor = context.getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                editor.putString("account", accounts.get(index));
                editor.apply();
                Intent Main = new Intent(context, com.indieweb.indigenous.MainActivity.class);
                context.startActivity(Main);
                activity.finish();
            }
        });
        builder.show();
    }

    /**
     * Returns all accounts.
     *
     * @return Account[]
     */
    private Account[] getAllAccounts() {
        AccountManager accountManager = AccountManager.get(context);
        return accountManager.getAccounts();
    }

}
