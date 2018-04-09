package com.indieweb.indigenous;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.indieauth.IndieAuth;
import com.indieweb.indigenous.micropub.MicropubActivity;
import com.indieweb.indigenous.microsub.channel.ChannelActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String AccessToken = preferences.getString("access_token", "");
        String MicroSubEndpoint = preferences.getString("microsub_endpoint", "");

        if (AccessToken.length() > 0) {
            if (MicroSubEndpoint.length() > 0) {
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
            Intent IndieAuth = new Intent(getBaseContext(), IndieAuth.class);
            startActivity(IndieAuth);
            this.finish();
        }
    }
}
