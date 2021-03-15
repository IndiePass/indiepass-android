// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.pleroma;

import android.accounts.AccountAuthenticatorResponse;
import android.accounts.AccountManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.indieweb.indigenous.users.AuthActivity;
import com.indieweb.indigenous.users.AuthenticatorBase;

import static com.indieweb.indigenous.users.AuthActivity.PLEROMA_ACCOUNT_TYPE;
import static com.indieweb.indigenous.users.AuthActivity.PLEROMA_TOKEN_TYPE;

public class AuthenticatorPleroma extends AuthenticatorBase {

    private final Context context;

    public AuthenticatorPleroma(Context context) {
        super(context);
        this.context = context;
    }

    @Override
    public Bundle addAccount(AccountAuthenticatorResponse response, String accountType, String authTokenType, String[] requiredFeatures, Bundle options)  {
        final Intent intent = new Intent(context, AuthActivity.class);
        intent.putExtra("com.indieweb.indigenous.AccountType", accountType);
        intent.putExtra(PLEROMA_TOKEN_TYPE, authTokenType);
        final Bundle bundle = new Bundle();
        bundle.putParcelable(AccountManager.KEY_INTENT, intent);
        return bundle;
    }

    @Override
    public String getAuthTokenLabel(String authTokenType) {
        return PLEROMA_ACCOUNT_TYPE;
    }

}
