// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.indieweb.indieauth;

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
