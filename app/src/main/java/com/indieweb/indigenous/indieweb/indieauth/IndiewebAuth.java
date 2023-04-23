package com.indieweb.indigenous.indieweb.indieauth;

import android.content.Context;
import android.widget.RelativeLayout;

import com.indieweb.indigenous.indieweb.micropub.Endpoints;
import com.indieweb.indigenous.indieweb.micropub.MicropubAction;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.AuthBase;

public class IndiewebAuth extends AuthBase {

    public IndiewebAuth(Context context, User user) {
        super(context, user);
    }

    @Override
    public void syncAccount(RelativeLayout layout) {
        new Endpoints(this.getContext(), this.getUser(), layout).refresh();
        new MicropubAction(this.getContext(), this.getUser(), layout).refreshConfig();
    }

    @Override
    public void revokeToken(User user) {
        new IndieAuthAction(getContext(), user).revoke();
    }
}
