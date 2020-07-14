package com.indieweb.indigenous.users;

import android.widget.RelativeLayout;

import com.indieweb.indigenous.model.User;

public interface Auth {

    void syncAccount(RelativeLayout layout);

    void revokeToken(User user);

}
