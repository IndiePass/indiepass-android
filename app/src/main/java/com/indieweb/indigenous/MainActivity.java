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

    WebView webview;
    Button signIn;
    EditText domain;
    String code;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String AccessToken = preferences.getString("access_token", "");
        String MicroPubEndpoint = preferences.getString("micropub_endpoint", "");

        if (AccessToken.length() > 0 && MicroPubEndpoint.length() > 0) {
            Intent TimeLine = new Intent(getBaseContext(), TimeLineActivity.class);
            startActivity(TimeLine);
        }
        else {
            signIn = findViewById(R.id.signInButton);
            signIn.setOnClickListener(doSignIn);
        }
    }

    /**
     * OnClickListener for the 'Sign in with your domain' button.
     */
    private final View.OnClickListener doSignIn = new View.OnClickListener() {
        public void onClick(View v) {

            domain = findViewById(R.id.domain);
            if (validDomain(domain.getText().toString())) {

                webview = findViewById(R.id.webView);
                domain.setVisibility(View.INVISIBLE);
                signIn.setVisibility(View.INVISIBLE);
                webview.setVisibility(View.VISIBLE);

                startOauthDance();
            }
            else {
                Toast.makeText(getApplicationContext(), "We did not find the necessary rel links on your domain (authorization_endpoint, token_endpoint, micropub, microsub)", Toast.LENGTH_LONG).show();
            }

        }
    };

    /**
     * Validates the domain.
     *
     * We will parse the frontpage to discover following rel links:
     *  - authorization_endpoint
     *  - token_endpoint
     *  - micropub
     *  - microsub
     *
     * @param $domain
     *   The domain to validate.
     *
     * @return boolean
     */
    private boolean validDomain(String $domain) {
        int found = 0;

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            // TODO the links can also be exposed in the response headers.
            Document doc = Jsoup.connect($domain).get();
            Elements imports = doc.select("link[href]");
            for (Element link : imports) {
                if (link.attr("rel").equals("micropub")) {
                    Log.d("indigenous_debug", link.attr("abs:href"));
                    SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                    editor.putString("micropub_endpoint", link.attr("abs:href"));
                    editor.apply();
                    found++;
                }

                // Microsub is optional for now, so we don't increment the counter here.
                if (link.attr("rel").equals("microsub")) {
                    Log.d("indigenous_debug", link.attr("abs:href"));
                    SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                    editor.putString("microsub_endpoint", link.attr("abs:href"));
                    editor.apply();
                }

                if (link.attr("rel").equals("authorization_endpoint")) {
                    Log.d("indigenous_debug", link.attr("abs:href"));
                    SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                    editor.putString("authorization_endpoint", link.attr("abs:href"));
                    editor.apply();
                    found++;
                }

                if (link.attr("rel").equals("token_endpoint")) {
                    Log.d("indigenous_debug", link.attr("abs:href"));
                    SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                    editor.putString("token_endpoint", link.attr("abs:href"));
                    editor.apply();
                    found++;
                }

            }

        }
        catch (IOException error) {
            Toast.makeText(getApplicationContext(), "Error on getting domain", Toast.LENGTH_LONG).show();
            Log.d("indigenous_debug", error.getMessage());
        }

        // If we have 3 endpoints, let's go.
        return found == 3;
    }

    /**
     * Authenticates with the IndieAuth endpoint.
     *
     * This method uses a webview to go to the authorization endpoint to start the oauth dance. If
     * we get back to indigenous.abode.pub, we'll intercept and check if we have a code and validate.
     */
    @SuppressLint("SetJavaScriptEnabled")
    private void startOauthDance() {

        webview.setWebViewClient(new WebViewClient() {

            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {

                Uri uri = Uri.parse(url);
                if (uri.getHost().contains("indigenous.abode.pub")) {

                    // Clear the webview.
                    webview.setVisibility(View.INVISIBLE);
                    webview.loadUrl("about:blank");

                    // Get the code if available.
                    code = uri.getQueryParameter("code");
                    if (code != null && code.length() > 0) {
                        validateCode(code);
                    }

                    return true;
                }

                return false;
            }
        });

        // Enable javascript.
        webview.getSettings().setJavaScriptEnabled(true);
        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String AuthEndPoint = preferences.getString("authorization_endpoint", "");
        webview.loadUrl(AuthEndPoint + "?redirect_uri=https://indigenous.abode.pub&client_id=" + domain.getText() + "&me=" + domain.getText() + "&scope=create+update+read+follow+channels");
    }

    /**
     * Validates the code.
     *
     * @param code
     *   The code we got back after the oauth dance with the authorization endpoint.
     */
    private void validateCode(final String code) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String TokenEndPoint = preferences.getString("token_endpoint", "");

        StringRequest postRequest = new StringRequest(Request.Method.POST, TokenEndPoint,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    try {
                        JSONObject indieAuthResponse = new JSONObject(response);
                        String accessToken = indieAuthResponse.getString("access_token");
                        Log.d("indigenous_debug", accessToken);

                        SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                        editor.putString("access_token", accessToken);
                        editor.putString("me", domain.getText().toString());
                        editor.apply();

                        Toast.makeText(getApplicationContext(), "Authentication succesfull", Toast.LENGTH_LONG).show();

                        Intent TimeLine = new Intent(getBaseContext(), TimeLineActivity.class);
                        startActivity(TimeLine);

                    }
                    catch (JSONException e) {
                        Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_LONG).show();
                        Log.d("indigenous_debug", e.getMessage());
                    }

                    // response
                    Log.d("indigenous_debug", response);
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Authentication failed", Toast.LENGTH_LONG).show();
                    Log.d("indigenous_debug", error.getMessage());
                }
            }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("code", code);
                params.put("me", domain.getText().toString());
                params.put("redirect_uri", "https://indigenous.abode.pub");
                params.put("client_id", domain.getText().toString());
                params.put("grant_type", "authorization_code");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                HashMap<String, String> headers = new HashMap<String, String>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        queue.add(postRequest);
    }

}
