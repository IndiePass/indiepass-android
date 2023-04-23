package com.indieweb.indiepass;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.indieweb.indiepass.users.AuthActivity;
import com.indieweb.indiepass.model.User;
import com.indieweb.indiepass.users.Accounts;

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
