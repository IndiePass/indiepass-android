package com.indieweb.indigenous;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;

import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

import static android.content.Context.MODE_PRIVATE;
import static com.indieweb.indigenous.indieauth.IndieAuthActivity.ACCOUNT_TYPE;
import static com.indieweb.indigenous.indieauth.IndieAuthActivity.TOKEN_TYPE;

class TestUtils {

    static void createAccount(Context context, boolean microsub) {
        String domain = "http://example.com";
        String accessToken = "awesome";

        AccountManager am = AccountManager.get(context);
        Account account = new Account(domain, ACCOUNT_TYPE);
        am.addAccountExplicitly(account, null, null);
        am.setAuthToken(account, TOKEN_TYPE, accessToken);
        am.setUserData(account, "micropub_endpoint", domain + "/micropub");
        am.setUserData(account, "authorization_endpoint", domain + "/auth");
        am.setUserData(account, "micropub_media_endpoint", domain + "/media");
        am.setUserData(account, "token_endpoint", domain + "/token");
        am.setUserData(account, "author_name", "Indigenous");

        // Set first account.
        SharedPreferences.Editor editor = context.getSharedPreferences("indigenous", MODE_PRIVATE).edit();
        editor.putString("account", domain);
        editor.apply();
        editor.commit();
    }

    static void removeAccount(Context context, Activity activity) {
        User user = new Accounts(context).getCurrentUser();
        AccountManager am = AccountManager.get(context);
        am.removeAccount(user.getAccount(), activity, null, null);

        SharedPreferences.Editor editor = context.getSharedPreferences("indigenous", MODE_PRIVATE).edit();
        editor.putString("account", "");
        editor.apply();
        editor.commit();
    }

}
