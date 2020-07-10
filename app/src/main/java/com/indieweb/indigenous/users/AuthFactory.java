package com.indieweb.indigenous.users;

import android.content.Context;

import androidx.annotation.NonNull;

import com.indieweb.indigenous.General;
import com.indieweb.indigenous.indieweb.IndiewebGeneral;
import com.indieweb.indigenous.indieweb.indieauth.IndiewebAuth;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.pixelfed.PixelfedAuth;
import com.indieweb.indigenous.pixelfed.PixelfedGeneral;

import static com.indieweb.indigenous.users.AuthActivity.PIXELFED_ACCOUNT_TYPE;

public class AuthFactory {

    @NonNull
    public static Auth getAuth(User user, Context context) {
        String type = "indieweb";

        if (user.isAuthenticated()) {
            if (user.getAccountType().equals(PIXELFED_ACCOUNT_TYPE)) {
                type = "pixelfed";
            }
        }

        switch (type) {
            case "indieweb":
                return new IndiewebAuth(context, user);
            case "pixelfed":
                return new PixelfedAuth(context, user);
        }

        return null;
    }

}
