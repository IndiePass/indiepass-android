package com.indieweb.indigenous.reader;

import android.content.Context;
import androidx.annotation.NonNull;
import com.indieweb.indigenous.indieweb.microsub.IndieWebReader;
import com.indieweb.indigenous.mastodon.MastodonReader;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.pixelfed.PixelfedReader;
import com.indieweb.indigenous.pleroma.PleromaReader;

import static com.indieweb.indigenous.mastodon.MastodonReader.CHANNEL_NAME_MASTODON_ANONYMOUS;
import static com.indieweb.indigenous.pixelfed.PixelfedReader.CHANNEL_NAME_PIXELFED_ANONYMOUS;
import static com.indieweb.indigenous.pleroma.PleromaReader.CHANNEL_NAME_PLEROMA_ANONYMOUS;
import static com.indieweb.indigenous.users.AuthActivity.*;

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
