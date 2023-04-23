package com.indieweb.indiepass.pixelfed;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorServicePixelfed extends Service {

    private AuthenticatorPixelfed authenticator;

    @Override
    public void onCreate() {
        authenticator = new AuthenticatorPixelfed(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
