package com.indieweb.indigenous;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.indieweb.indigenous.indieauth.IndieAuthActivity;
import com.indieweb.indigenous.micropub.MicropubActivity;
import com.indieweb.indigenous.microsub.channel.ChannelActivity;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        User user = new Accounts(this).getCurrentUser();
        if (user.isValid()) {
            if (user.getMicrosubEndpoint() != null && user.getMicrosubEndpoint().length() > 0) {
                Intent Channels = new Intent(getBaseContext(), ChannelActivity.class);
                startActivity(Channels);
                this.finish();
            }
            else {
                Intent Micropub = new Intent(getBaseContext(), MicropubActivity.class);
                startActivity(Micropub);
                this.finish();
            }
        }
        else {
            Intent IndieAuth = new Intent(getBaseContext(), IndieAuthActivity.class);
            startActivity(IndieAuth);
            this.finish();
        }
    }
}
