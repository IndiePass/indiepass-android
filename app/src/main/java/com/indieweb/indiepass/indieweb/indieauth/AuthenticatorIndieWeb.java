package com.indieweb.indiepass.indieweb.indieauth;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.indieweb.indiepass.users.AuthActivity;
import com.indieweb.indiepass.users.AuthenticatorBase;

import static com.indieweb.indiepass.users.AuthActivity.INDIEWEB_ACCOUNT_TYPE;
import static com.indieweb.indiepass.users.AuthActivity.INDIEWEB_TOKEN_TYPE;

public class AuthenticatorIndieWeb extends AuthenticatorBase {

    private final Context context;

    public AuthenticatorIndieWeb(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)  {
        final Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra("com.indieweb.indiepass.AccountType", accountType);
        intent.putExtra(INDIEWEB_TOKEN_TYPE, authTokenType);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return INDIEWEB_ACCOUNT_TYPE;
    }

}