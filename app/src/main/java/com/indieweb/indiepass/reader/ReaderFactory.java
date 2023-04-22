package com.indieweb.indiepass.reader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.indieweb.indiepass.indieweb.microsub.IndieWebReader;
import com.indieweb.indiepass.mastodon.MastodonReader;
import com.indieweb.indiepass.model.User;
import com.indieweb.indiepass.pixelfed.PixelfedReader;
import com.indieweb.indiepass.pleroma.PleromaReader;

import static com.indieweb.indiepass.mastodon.MastodonReader.CHANNEL_NAME_MASTODON_ANONYMOUS;
import static com.indieweb.indiepass.pixelfed.PixelfedReader.CHANNEL_NAME_PIXELFED_ANONYMOUS;
import static com.indieweb.indiepass.pleroma.PleromaReader.CHANNEL_NAME_PLEROMA_ANONYMOUS;
import static com.indieweb.indiepass.users.AuthActivity.MASTODON_ACCOUNT_TYPE;
import static com.indieweb.indiepass.users.AuthActivity.PIXELFED_ACCOUNT_TYPE;
import static com.indieweb.indiepass.users.AuthActivity.PLEROMA_ACCOUNT_TYPE;

public class ReaderFactory {

    @NonNull
    public static Reader getReader(User user, String channelId, Context context) {
        String type = "indieweb";

        if (channelId != null) {
            switch (channelId) {
                case CHANNEL_NAME_MASTODON_ANONYMOUS:
                    type = "mastodon";
                    break;
                case CHANNEL_NAME_PIXELFED_ANONYMOUS:
                    type = "pixelfed";
                    break;
                case CHANNEL_NAME_PLEROMA_ANONYMOUS:
                    type = "pleroma";
                    break;
            }
        }

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
                return new IndieWebReader(context, user);
            case "pixelfed":
                return new PixelfedReader(context, user);
            case "pleroma":
                return new PleromaReader(context, user);
            case "mastodon":
                return new MastodonReader(context, user);
        }

        return null;
    }

}
