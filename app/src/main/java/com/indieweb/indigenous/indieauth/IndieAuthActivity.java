package com.indieweb.indigenous.indieauth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.support.customtabs.CustomTabsIntent;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.MainActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.MicropubConfig;

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

public class IndieAuthActivity extends AccountAuthenticatorActivity {

    public final static String ACCOUNT_TYPE = "IndieAuth";
    public final static String TOKEN_TYPE = "IndieAuth";

    String state;
    Button signIn;
    EditText domain;
    TextView info;
    String domainInput;
    String authorizationEndpoint;
    String tokenEndpoint;
    String micropubEndpoint;
    String microsubEndpoint;
    String ClientId = "https://indigenous.abode.pub/android/";
    String RedirectUri = "https://indigenous.abode.pub/android/login/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_indieauth);

        // Generate state, use uuid and take first 10 chars.
        state = UUID.randomUUID().toString().substring(0, 10);

        domain = findViewById(R.id.domain);
        info = findViewById(R.id.info);
        signIn = findViewById(R.id.signInButton);
        signIn.setOnClickListener(doSignIn);

        // Show 'set account' button.
        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String accountName = preferences.getString("account", "");
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();
        if (accountName.length() == 0 && accounts.length > 0) {
            TextView setAccountInfo = findViewById(R.id.setAccountButtonInfo);
            setAccountInfo.setVisibility(View.VISIBLE);
            Button setAccount = findViewById(R.id.setAccountButton);
            setAccount.setOnClickListener(doSetAccount);
            setAccount.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            Toast.makeText(getApplicationContext(), "Validating code", Toast.LENGTH_SHORT).show();

            // Get the code and state.
            String code = intent.getData().getQueryParameter("code");
            String returnedState = intent.getData().getQueryParameter("state");
            if (code != null && code.length() > 0 && returnedState != null && returnedState.length() > 0) {
                validateCode(code, returnedState);
            }
            else {
                Toast.makeText(getApplicationContext(), "No code found in URL to validate", Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * OnClickListener for the 'Set account' button.
     */
    private final View.OnClickListener doSetAccount = new View.OnClickListener() {
        public void onClick(View v) {
            new Accounts(IndieAuthActivity.this).setAccount(IndieAuthActivity.this);
        }
    };

    /**
     * OnClickListener for the 'Sign in with your domain' button.
     */
    private final View.OnClickListener doSignIn = new View.OnClickListener() {
        public void onClick(View v) {

            if (!new Connection(getApplicationContext()).hasConnection()) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                return;
            }

            domainInput = domain.getText().toString();

            // Check if there's protocol, prefix it with https:// if necessary.
            if (!domainInput.contains("http://") && !domainInput.contains("https://")) {
                domainInput = "https://" + domainInput;
            }

            if (validDomain(domainInput)) {

                String url = authorizationEndpoint + "?response_type=code&redirect_uri=" + RedirectUri + "&client_id=" + ClientId + "&me=" + domainInput + "&scope=create+update+read+follow+channels&state=" + state;
                Uri uri = Uri.parse(url);

                CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                intentBuilder.setToolbarColor(ContextCompat.getColor(IndieAuthActivity.this, R.color.colorPrimary));
                intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(IndieAuthActivity.this, R.color.colorPrimaryDark));
                CustomTabsIntent customTabsIntent = intentBuilder.build();
                customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                customTabsIntent.launchUrl(IndieAuthActivity.this, uri);

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

        Toast.makeText(getApplicationContext(), "Connecting to domain, one moment", Toast.LENGTH_SHORT).show();

        try {
            Document doc = Jsoup.connect($domain).get();
            Elements imports = doc.select("link[href]");
            for (Element link : imports) {
                if (link.attr("rel").equals("authorization_endpoint")) {
                    authorizationEndpoint = link.attr("abs:href");
                    found++;
                }

                if (link.attr("rel").equals("token_endpoint")) {
                    tokenEndpoint = link.attr("abs:href");
                    found++;
                }

                if (link.attr("rel").equals("micropub")) {
                    micropubEndpoint = link.attr("abs:href");
                    found++;
                }

                // Microsub is optional, so we don't increment the counter here.
                if (link.attr("rel").equals("microsub")) {
                    microsubEndpoint = link.attr("abs:href");
                }
            }

        }
        catch (IllegalArgumentException ignored) {
            Toast.makeText(getApplicationContext(), "Could not connect to domain", Toast.LENGTH_SHORT).show();
        }
        catch (IOException error) {
            Toast.makeText(getApplicationContext(), "Could not connect to domain", Toast.LENGTH_SHORT).show();
        }

        // If we have 3 endpoints, let's go.
        return found == 3;
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

        StringRequest postRequest = new StringRequest(Request.Method.POST, tokenEndpoint,
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

                        AccountManager am = AccountManager.get(getApplicationContext());
                        int numberOfAccounts = am.getAccounts().length;

                        // Create new account.
                        Account account = new Account(domainInput, ACCOUNT_TYPE);
                        am.addAccountExplicitly(account, null, null);
                        am.setAuthToken(account, TOKEN_TYPE, accessToken);
                        am.setUserData(account, "micropub_endpoint", micropubEndpoint);
                        am.setUserData(account, "microsub_endpoint", microsubEndpoint);
                        am.setUserData(account, "authorization_endpoint", authorizationEndpoint);
                        am.setUserData(account, "token_endpoint", tokenEndpoint);

                        // Set first account.
                        if (numberOfAccounts == 0) {
                            SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                            editor.putString("account", domainInput);
                            editor.apply();
                        }

                        // Refresh syndication targets and media endpoint.
                        User user = new User();
                        user.setMicropubEndpoint(micropubEndpoint);
                        user.setAccessToken(accessToken);
                        user.setAccount(account);
                        new MicropubConfig(getApplicationContext(), user).refresh();

                        Toast.makeText(getApplicationContext(), "Authentication successful", Toast.LENGTH_SHORT).show();

                        // Start main activity which will determine where it will go.
                        Intent main = new Intent(getBaseContext(), MainActivity.class);
                        startActivity(main);
                        finish();
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
                    try {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.statusCode != 0 && networkResponse.data != null) {
                            Integer code = networkResponse.statusCode;
                            String result = new String(networkResponse.data);
                            Toast.makeText(getApplicationContext(), "Authentication failed: Status code: " + code + "; message: " + result, Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), "Authentication failed due to network failure: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    catch (Exception e) {
                        Toast.makeText(getApplicationContext(), "Network error: " + e.getMessage(), Toast.LENGTH_LONG).show();
                    }

                    // TODO use helper method
                    info.setVisibility(View.VISIBLE);
                    domain.setVisibility(View.VISIBLE);
                    signIn.setVisibility(View.VISIBLE);

                }
            }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                params.put("code", code);
                params.put("me", domainInput);
                params.put("redirect_uri", RedirectUri);
                params.put("client_id", ClientId);
                params.put("grant_type", "authorization_code");

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        queue.add(postRequest);
    }

}
