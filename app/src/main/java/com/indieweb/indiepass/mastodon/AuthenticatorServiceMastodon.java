package com.indieweb.indiepass.mastodon;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorServiceMastodon extends Service {

    private AuthenticatorMastodon authenticator;

    @Override
    public void onCreate() {
        authenticator = new AuthenticatorMastodon(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
