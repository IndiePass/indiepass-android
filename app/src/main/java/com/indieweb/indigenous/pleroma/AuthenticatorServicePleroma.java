package com.indieweb.indigenous.pleroma;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

public class AuthenticatorServicePleroma extends Service {

    private AuthenticatorPleroma authenticator;

    @Override
    public void onCreate() {
        authenticator = new AuthenticatorPleroma(this);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return authenticator.getIBinder();
    }
}
