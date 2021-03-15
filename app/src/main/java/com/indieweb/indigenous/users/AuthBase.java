// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.users;

import android.content.Context;

import com.indieweb.indigenous.model.User;

abstract public class AuthBase implements Auth {

    private final Context context;
    private final User user;

    public AuthBase(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    public Context getContext() {
        return context;
    }

    public User getUser() {
        return user;
    }

}
