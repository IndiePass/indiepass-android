// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.indieweb.indigenous.users.AuthActivity;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.Accounts;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user = new Accounts(this).getDefaultUser();
        if (user.isValid()) {
            Intent MainActivity = new Intent(getBaseContext(), MainActivity.class);
            startActivity(MainActivity);
        }
        else {
            Intent IndieAuth = new Intent(getBaseContext(), AuthActivity.class);
            startActivity(IndieAuth);
        }
        this.finish();
    }
}
