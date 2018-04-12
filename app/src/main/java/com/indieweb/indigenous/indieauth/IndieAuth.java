package com.indieweb.indigenous.indieauth;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.MicropubActivity;
import com.indieweb.indigenous.microsub.channel.ChannelActivity;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class IndieAuth extends AppCompatActivity {

    String state;
    WebView webview;
    Button signIn;
    EditText domain;
    TextView info;
    String code;
    String domainInput;
    String ClientId = "https://indigenous.abode.pub";
    String RedirectUri = "https://indigenous.abode.pub";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indieauth);

        // Generate state, use uuid and take first 10 chars.
        state = UUID.randomUUID().toString().substring(0, 10);

        domain = findViewById(R.id.domain);
        webview = findViewById(R.id.webView);
        info = findViewById(R.id.info);
        signIn = findViewById(R.id.signInButton);
        signIn.setOnClickListener(doSignIn);
    }

    /**
     * OnClickListener for the 'Sign in with your domain' button.
     */
    private final View.OnClickListener doSignIn = new View.OnClickListener() {
        public void onClick(View v) {

            domainInput = domain.getText().toString();

            // Check if there's protocol, prefix it with https:// if necessary.
            if (!domainInput.contains("http://") && !domainInput.contains("https://")) {
                domainInput = "https://" + domainInput;
            }

            if (validDomain(domainInput)) {

                info.setVisibility(View.GONE);
                domain.setVisibility(View.GONE);
                signIn.setVisibility(View.GONE);
                webview.setVisibility(View.VISIBLE);

                startOauthDance();
            }
            // TODO fix the toast messages, this also is printed if we're online, bit confusing.
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

        Toast.makeText(getApplicationContext(), "Connecting to domain, one moment", Toast.LENGTH_SHORT).show();

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
        catch (IllegalArgumentException ignored) {
            Toast.makeText(getApplicationContext(), "Could not connect to domain", Toast.LENGTH_LONG).show();
        }
        catch (IOException error) {
            Toast.makeText(getApplicationContext(), "Could not connect to domain", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(getApplicationContext(), "Validating code", Toast.LENGTH_SHORT).show();

                    // Get the code and state.
                    code = uri.getQueryParameter("code");
                    String returnedState = uri.getQueryParameter("state");
                    if (code != null && code.length() > 0 && returnedState != null && returnedState.length() > 0) {
                        validateCode(code, returnedState);
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "No code found in URL to validate", Toast.LENGTH_SHORT).show();
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
        webview.loadUrl(AuthEndPoint + "?response_type=code&redirect_uri=" + RedirectUri + "&client_id=" + ClientId + "&me=" + domainInput + "&scope=create+update+read+follow+channels&state=" + state);
    }

    /**
     * Validates the code.
     *
     * @param code
     *   The code we got back after the oauth dance with the authorization endpoint.
     * @param returnedState
     *   The returned state.
     */
    private void validateCode(final String code, final String returnedState) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        final SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String TokenEndPoint = preferences.getString("token_endpoint", "");

        StringRequest postRequest = new StringRequest(Request.Method.POST, TokenEndPoint,
            new Response.Listener<String>() {
                @Override
                public void onResponse(String response) {

                    String accessToken = "";
                    String errorMessage = "";
                    boolean accessTokenFound = false;

                    try {
                        JSONObject indieAuthResponse = new JSONObject(response);
                        accessToken = indieAuthResponse.getString("access_token");
                        accessTokenFound = true;
                    }
                    catch (JSONException e) {

                        // Catch the json exception. However, we're not done yet.
                        errorMessage = e.getMessage();

                        // Known, and maybe other projects, do not return a json response (yet), so
                        // the access token might be in the body as an URL-encoded query string.
                        // @see https://github.com/idno/Known/issues/1986
                        try {
                            Map<String, String> query_pairs = new LinkedHashMap<>();
                            String[] pairs = response.split("&");
                            for (String pair : pairs) {
                                int idx = pair.indexOf("=");
                                query_pairs.put(URLDecoder.decode(pair.substring(0, idx), "UTF-8"), URLDecoder.decode(pair.substring(idx + 1), "UTF-8"));
                            }
                            accessToken = query_pairs.get("access_token");
                            if (accessToken.length() > 0) {
                                accessTokenFound = true;
                            }

                        }
                        catch (UnsupportedEncodingException e1) {
                            errorMessage += " - " + e1.getMessage();
                        }
                        catch (Exception e2) {
                            errorMessage += " - " + e2.getMessage();
                        }

                    }

                    if (accessTokenFound && returnedState.equals(state)) {
                        SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                        editor.putString("access_token", accessToken);
                        editor.putString("me", domainInput);
                        editor.apply();

                        Toast.makeText(getApplicationContext(), "Authentication successful", Toast.LENGTH_SHORT).show();

                        if (preferences.getString("microsub_endpoint", "").length() > 0) {
                            Intent Channels = new Intent(getBaseContext(), ChannelActivity.class);
                            startActivity(Channels);
                        }
                        else {
                            Intent Micropub = new Intent(getBaseContext(), MicropubActivity.class);
                            startActivity(Micropub);
                        }
                    }
                    else {
                        Toast.makeText(getApplicationContext(), "Authentication failed: " + errorMessage, Toast.LENGTH_LONG).show();

                        // TODO use helper method
                        info.setVisibility(View.VISIBLE);
                        domain.setVisibility(View.VISIBLE);
                        signIn.setVisibility(View.VISIBLE);
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    Toast.makeText(getApplicationContext(), "Authentication failed: " + error.getMessage(), Toast.LENGTH_LONG).show();

                    // TODO use helper method
                    info.setVisibility(View.VISIBLE);
                    domain.setVisibility(View.VISIBLE);
                    signIn.setVisibility(View.VISIBLE);

                    Log.d("indigenous_debug", error.getMessage());
                }
            }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<String, String>();

                params.put("code", code);
                params.put("me", domainInput);
                params.put("redirect_uri", "https://indigenous.abode.pub");
                params.put("client_id", ClientId);
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
