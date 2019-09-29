package com.indieweb.indigenous;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;

import com.indieweb.indigenous.indieauth.IndieAuthActivity;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

public class LaunchActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user = new Accounts(this).getCurrentUser();
        if (user.isValid()) {
            Intent MainActivity = new Intent(getBaseContext(), MainActivity.class);
            startActivity(MainActivity);
            this.finish();
        }
        else {
            Intent IndieAuth = new Intent(getBaseContext(), IndieAuthActivity.class);
            startActivity(IndieAuth);
            this.finish();
        }
    }
}
