package com.indieweb.indiepass.post;

import android.content.Context;

import androidx.annotation.NonNull;

import com.indieweb.indiepass.indieweb.micropub.IndieWebPost;
import com.indieweb.indiepass.mastodon.MastodonPost;
import com.indieweb.indiepass.model.User;
import com.indieweb.indiepass.pixelfed.PixelfedPost;
import com.indieweb.indiepass.pleroma.PleromaPost;

import static com.indieweb.indiepass.users.AuthActivity.MASTODON_ACCOUNT_TYPE;
import static com.indieweb.indiepass.users.AuthActivity.PIXELFED_ACCOUNT_TYPE;
import static com.indieweb.indiepass.users.AuthActivity.PLEROMA_ACCOUNT_TYPE;

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
            if (user.getAccountType().equals(PLEROMA_ACCOUNT_TYPE)) {
                type = "pleroma";
            }
        }

        switch (type) {
            case "indieweb":
                return new IndieWebPost(context, user);
            case "pixelfed":
                return new PixelfedPost(context, user);
            case "pleroma":
                return new PleromaPost(context, user);
            case "mastodon":
                return new MastodonPost(context, user);
        }

        return null;
    }

}
