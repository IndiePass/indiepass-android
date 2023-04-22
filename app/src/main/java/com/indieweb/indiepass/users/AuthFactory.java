package com.indieweb.indiepass.users;

import android.content.Context;

import androidx.annotation.NonNull;

import com.indieweb.indiepass.indieweb.indieauth.IndiewebAuth;
import com.indieweb.indiepass.mastodon.MastodonAuth;
import com.indieweb.indiepass.model.User;
import com.indieweb.indiepass.pixelfed.PixelfedAuth;
import com.indieweb.indiepass.pleroma.PleromaAuth;

import static com.indieweb.indiepass.users.AuthActivity.MASTODON_ACCOUNT_TYPE;
import static com.indieweb.indiepass.users.AuthActivity.PIXELFED_ACCOUNT_TYPE;
import static com.indieweb.indiepass.users.AuthActivity.PLEROMA_ACCOUNT_TYPE;

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
