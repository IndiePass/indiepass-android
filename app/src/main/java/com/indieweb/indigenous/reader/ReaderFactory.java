package com.indieweb.indigenous.reader;

import android.content.Context;

import androidx.annotation.NonNull;

import com.indieweb.indigenous.indieweb.microsub.IndieWebReader;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.pixelfed.PixelfedReader;

import static com.indieweb.indigenous.users.AuthActivity.PIXELFED_ACCOUNT_TYPE;

public class ReaderFactory {

    @NonNull
    public static Reader getReader(User user, String channelId, Context context) {
        String type = "indieweb";

        if (channelId != null && channelId.equals("indigenous_pixelfed")) {
            type = "pixelfed";
        }

        if (user.isAuthenticated()) {
            if (user.getAccountType().equals(PIXELFED_ACCOUNT_TYPE)) {
                type = "pixelfed";
            }
        }

        switch (type) {
            case "indieweb":
                return new IndieWebReader(context, user);
            case "pixelfed":
                return new PixelfedReader(context, user);
        }

        return null;
    }

}
