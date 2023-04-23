package com.indieweb.indiepass.users;

import android.widget.RelativeLayout;

import com.indieweb.indiepass.model.User;

public interface Auth {

    /**
     * Sync an account.
     *
     * @param layout
     *   The current layout
     */
    void syncAccount(RelativeLayout layout);

    /**
     * Revoke a token.
     *
     * @param user
     *   The current user.
     */
    void revokeToken(User user);

}
