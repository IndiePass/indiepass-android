package com.indieweb.indigenous.post;

import android.content.Context;
import android.widget.MultiAutoCompleteTextView;

import com.indieweb.indigenous.model.User;

abstract public class PostBase implements Post {

    private Context context;
    private User user;

    public PostBase(Context context, User user) {
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
    public boolean supportsPostParam(String name) {
        return true;
    }

    @Override
    public boolean useMediaEndpoint() {
        return true;
    }

    @Override
    public String getPostParamName(String name) {
        return name;
    }

    @Override
    public void prepareTagsAutocomplete(MultiAutoCompleteTextView tags) { }

    @Override
    public void prepareContactsAutocomplete(MultiAutoCompleteTextView body) { }
}
