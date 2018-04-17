package com.indieweb.indigenous.util;

import android.support.design.widget.Snackbar;
import android.view.View;

public class PopupMessage {

    private String message;
    private View view;
    private Integer duration;

    public PopupMessage(String message, View view) {
        this.message = message;
        this.view = view;
        this.duration = Snackbar.LENGTH_SHORT;
    }

    public PopupMessage(String message, View view, Integer duration) {
        this.message = message;
        this.view = view;
        this.duration = duration;
    }

    public void show() {
        Snackbar mySnackbar = Snackbar.make(this.view, this.message, this.duration);
        mySnackbar.show();
    }


}
