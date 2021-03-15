// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.pixelfed;

import android.content.Context;

import com.indieweb.indigenous.GeneralBase;
import com.indieweb.indigenous.MainActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;

import java.util.ArrayList;

public class PixelfedGeneral extends GeneralBase {

    public PixelfedGeneral(Context context, User user) {
        super(context, user);
    }

    @Override
    public boolean supports(String feature) {
        boolean supported = true;

        switch (feature) {
            case FEATURE_CHANNELS_REFRESH:
            case FEATURE_CHANNELS_MANAGE:
            case FEATURE_CHANNELS_SHOW_SOURCES:
            case FEATURE_CHANNELS_HIDE_READ:
            case FEATURE_CHANNELS_READ_LATER:
            case FEATURE_CONTACTS:
            case FEATURE_UPLOAD:
            case FEATURE_POSTS:
                supported = false;
                break;
        }

        return supported;
    }

    @Override
    public void handlePostActionButtonClick() {
        ((MainActivity) this.getContext()).clickOnMenuItem(R.id.createArticle);
    }

    @Override
    public void handleWritePostClick() {
        this.handlePostActionButtonClick();
    }

    @Override
    public boolean hidePostTypes() {
        return true;
    }

    @Override
    public ArrayList<Integer> getProtectedPostTypes() {
        ArrayList<Integer> protectedTypes = new ArrayList<>();
        protectedTypes.add(R.id.createArticle);
        return protectedTypes;
    }

}
