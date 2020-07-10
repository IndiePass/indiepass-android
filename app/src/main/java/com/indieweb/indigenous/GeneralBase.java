package com.indieweb.indigenous;

import android.content.Context;

import com.indieweb.indigenous.model.User;

abstract public class GeneralBase implements General {

    private Context context;
    private User user;

    public GeneralBase(Context context, User user) {
        this.context = context;
        this.user = user;
    }

    public Context getContext() {
        return context;
    }

    public User getUser() {
        return user;
    }

    @Override
    public boolean supports(String feature) {
        return true;
    }

    @Override
    public void handlePostActionButtonClick() {
        ((MainActivity) this.getContext()).openDrawer(R.id.nav_create);
    }

    @Override
    public void handleWritePostClick() {
        ((MainActivity) this.getContext()).toggleGroupItems(false);
    }
}
