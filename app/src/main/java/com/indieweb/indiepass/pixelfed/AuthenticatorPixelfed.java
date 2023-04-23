package com.indieweb.indiepass.pixelfed;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.indieweb.indiepass.users.AuthActivity;
import com.indieweb.indiepass.users.AuthenticatorBase;

import static com.indieweb.indiepass.users.AuthActivity.PIXELFED_ACCOUNT_TYPE;
import static com.indieweb.indiepass.users.AuthActivity.PIXELFED_TOKEN_TYPE;

public class AuthenticatorPixelfed extends AuthenticatorBase {

    private final Context context;

    public AuthenticatorPixelfed(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)  {
        final Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra("com.indieweb.indiepass.AccountType", accountType);
        intent.putExtra(PIXELFED_TOKEN_TYPE, authTokenType);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return PIXELFED_ACCOUNT_TYPE;
    }

}