package com.indieweb.indiepass.mastodon;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.indieweb.indiepass.users.AuthActivity;
import com.indieweb.indiepass.users.AuthenticatorBase;

import static com.indieweb.indiepass.users.AuthActivity.MASTODON_ACCOUNT_TYPE;
import static com.indieweb.indiepass.users.AuthActivity.MASTODON_TOKEN_TYPE;

public class AuthenticatorMastodon extends AuthenticatorBase {

    private final Context context;

    public AuthenticatorMastodon(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)  {
        final Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra("com.indieweb.indiepass.AccountType", accountType);
        intent.putExtra(MASTODON_TOKEN_TYPE, authTokenType);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return MASTODON_ACCOUNT_TYPE;
    }

}