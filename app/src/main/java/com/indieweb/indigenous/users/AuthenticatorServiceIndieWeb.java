package com.indieweb.indigenous.users;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorServiceIndieWeb extends Service {

    private AuthenticatorIndieWeb authenticator;

    @Override
    public void onCreate() {
        authenticator = new AuthenticatorIndieWeb(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
