package com.indieweb.indigenous.post;

import android.content.Context;

import androidx.annotation.NonNull;

import com.indieweb.indigenous.indieweb.micropub.IndieWebPost;
import com.indieweb.indigenous.mastodon.MastodonPost;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.pixelfed.PixelfedPost;

import static com.indieweb.indigenous.users.AuthActivity.MASTODON_ACCOUNT_TYPE;
import static com.indieweb.indigenous.users.AuthActivity.PIXELFED_ACCOUNT_TYPE;

public class PostFactory {

    @NonNull
    public static Post getPost(User user, Context context) {
        String type = "indieweb";

        if (user.isAuthenticated()) {
            if (user.getAccountType().equals(PIXELFED_ACCOUNT_TYPE)) {
                type = "pixelfed";
            }
            if (user.getAccountType().equals(MASTODON_ACCOUNT_TYPE)) {
                type = "mastodon";
            }
        }

        switch (type) {
            case "indieweb":
                return new IndieWebPost(context, user);
            case "pixelfed":
                return new PixelfedPost(context, user);
            case "mastodon":
                return new MastodonPost(context, user);
        }

        return null;
    }

}
