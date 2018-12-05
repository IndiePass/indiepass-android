package com.indieweb.indigenous;

import android.app.Application;

public class Indigenous extends Application {

    private static Indigenous singleton;

    public static String debug;

    public static Indigenous getInstance(){
        return singleton;
    }

    public void setDebug(String text) {
        debug = text;
    }

    public String getDebug() {
        return debug;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        singleton = this;
    }
}
