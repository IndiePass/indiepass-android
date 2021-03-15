// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.pixelfed;

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
