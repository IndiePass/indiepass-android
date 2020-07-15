package com.indieweb.indigenous.reader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.indieweb.indigenous.indieweb.microsub.IndieWebReader;
import com.indieweb.indigenous.mastodon.MastodonReader;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.pixelfed.PixelfedReader;

import static com.indieweb.indigenous.pixelfed.PixelfedReader.CHANNEL_NAME_ANONYMOUS;
import static com.indieweb.indigenous.users.AuthActivity.MASTODON_ACCOUNT_TYPE;
import static com.indieweb.indigenous.users.AuthActivity.PIXELFED_ACCOUNT_TYPE;

public class ReaderFactory {

    @NonNull
    public static Reader getReader(User user, String channelId, Context context) {
        String type = "indieweb";

        if (channelId != null && channelId.equals(CHANNEL_NAME_ANONYMOUS)) {
            type = "pixelfed";
        }

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
                return new IndieWebReader(context, user);
            case "pixelfed":
                return new PixelfedReader(context, user);
            case "mastodon":
                return new MastodonReader(context, user);
        }

        return null;
    }

}
