// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.users;

import android.widget.RelativeLayout;

import com.indieweb.indigenous.model.User;

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
