// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.users;

import android.content.Context;

import androidx.annotation.NonNull;

import com.indieweb.indigenous.indieweb.indieauth.IndiewebAuth;
import com.indieweb.indigenous.mastodon.MastodonAuth;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.pixelfed.PixelfedAuth;
import com.indieweb.indigenous.pleroma.PleromaAuth;

import static com.indieweb.indigenous.users.AuthActivity.MASTODON_ACCOUNT_TYPE;
import static com.indieweb.indigenous.users.AuthActivity.PIXELFED_ACCOUNT_TYPE;
import static com.indieweb.indigenous.users.AuthActivity.PLEROMA_ACCOUNT_TYPE;

public class AuthFactory {

    @NonNull
    public static Auth getAuth(User user, Context context) {
        String type = "indieweb";

        if (user.isAuthenticated()) {
            if (user.getAccountType().equals(PIXELFED_ACCOUNT_TYPE)) {
                type = "pixelfed";
            }
            if (user.getAccountType().equals(MASTODON_ACCOUNT_TYPE)) {
                type = "mastodon";
            }
            if (user.getAccountType().equals(PLEROMA_ACCOUNT_TYPE)) {
                type = "pleroma";
            }
        }

        switch (type) {
            case "indieweb":
                return new IndiewebAuth(context, user);
            case "pixelfed":
                return new PixelfedAuth(context, user);
            case "pleroma":
                return new PleromaAuth(context, user);
            case "mastodon":
                return new MastodonAuth(context, user);
        }

        return null;
    }

}
