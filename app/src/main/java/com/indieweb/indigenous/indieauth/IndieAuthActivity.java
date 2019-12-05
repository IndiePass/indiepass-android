package com.indieweb.indigenous.indieauth;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;
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
import com.indieweb.indigenous.LaunchActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.HCard;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.micropub.MicropubConfig;
import com.indieweb.indigenous.util.Utility;
import com.indieweb.indigenous.util.mf2.Mf2Parser;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
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
    String authorizationEndpoint = "";
    String tokenEndpoint = "";
    String micropubEndpoint = "";
    String microsubEndpoint = "";
    String micropubMediaEndpoint = "";
    String authorAvatar;
    String authorName;
    String ClientId = "https://indigenous.realize.be";
    String RedirectUri = "https://indigenous.realize.be/indigenous-callback.php";

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

        // Show 'select account' button.
        AccountManager accountManager = AccountManager.get(this);
        Account[] accounts = accountManager.getAccounts();
        if (accounts.length > 0) {
            TextView setAccountInfo = findViewById(R.id.selectAccountButtonInfo);
            setAccountInfo.setVisibility(View.VISIBLE);
            Button selectAccount = findViewById(R.id.selectAccountButton);
            selectAccount.setOnClickListener(this.selectAccount);
            selectAccount.setVisibility(View.VISIBLE);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            Toast.makeText(getApplicationContext(), R.string.validating_code, Toast.LENGTH_SHORT).show();

            // Get the code and state.
            String code = intent.getData().getQueryParameter("code");
            String returnedState = intent.getData().getQueryParameter("state");
            if (code != null && code.length() > 0 && returnedState != null && returnedState.length() > 0) {
                validateCode(code, returnedState);
            }
            else {
                Toast.makeText(getApplicationContext(), R.string.no_code_found, Toast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * OnClickListener for the 'Set account' button.
     */
    private final View.OnClickListener selectAccount = new View.OnClickListener() {
        public void onClick(View v) {
            new Accounts(IndieAuthActivity.this).selectAccount(IndieAuthActivity.this);
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

            changeSignInButton(R.string.connecting);
            if (validDomain(domainInput)) {

                String url = authorizationEndpoint + "?response_type=code&redirect_uri=" + RedirectUri + "&client_id=" + ClientId + "&me=" + domainInput + "&scope=create+update+delete+media+read+follow+channels+mute+block&state=" + state;
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
                changeSignInButton(R.string.sign_in_with_domain);
                Toast.makeText(getApplicationContext(), R.string.missing_rel_links, Toast.LENGTH_LONG).show();
            }

        }
    };

    /**
     * Validates the domain.
     *
     * Checks the response of the URL or parse HTML to discover following rel links:
     *  - authorization_endpoint
     *  - token_endpoint
     *  - micropub
     *  - microsub
     *  - micropub_media
     *
     * @param $domain
     *   The domain to validate.
     *
     * @return boolean
     */
    private boolean validDomain(String $domain) {
        int numberOfAuthEndpoints = 0;
        boolean hasMicropubOrMicrosub = false;

        // This crashes on 7.1.
        if (android.os.Build.VERSION.SDK_INT != 25) {
            Toast.makeText(getApplicationContext(), "Connecting to domain, one moment", Toast.LENGTH_SHORT).show();
        }

        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            org.jsoup.Connection connection = Jsoup.connect($domain);
            org.jsoup.Connection.Response response = connection.execute();

            if (response.hasHeader("Link")) {
                String[] headers = response.header("Link").split(",");
                if (headers.length > 0) {

                    for (String link: headers) {
                        String[] split = link.split(";");
                        String endpoint = split[0].replace("<", "").replace(">", "").trim();
                        String rel = split[1].trim().replace("rel=", "").replace("\"", "");

                        endpoint = Utility.checkAbsoluteUrl(endpoint, $domain);

                        switch (rel) {
                            case "authorization_endpoint":
                                authorizationEndpoint = endpoint;
                                numberOfAuthEndpoints++;
                                break;
                            case "token_endpoint":
                                tokenEndpoint = endpoint;
                                numberOfAuthEndpoints++;
                                break;
                            case "micropub":
                                micropubEndpoint = endpoint;
                                hasMicropubOrMicrosub = true;
                                break;
                            case "microsub":
                                microsubEndpoint = endpoint;
                                hasMicropubOrMicrosub = true;
                                break;
                            case "micropub_media":
                                micropubMediaEndpoint = endpoint;
                                break;
                        }
                    }
                }
            }

            // Get from link tags.
            Document doc = connection.get();
            Elements imports = doc.select("link[href]");
            for (Element link : imports) {
                if (authorizationEndpoint.length() == 0 && link.attr("rel").equals("authorization_endpoint")) {
                    authorizationEndpoint = Utility.checkAbsoluteUrl(link.attr("abs:href"), $domain);
                    numberOfAuthEndpoints++;
                }

                if (tokenEndpoint.length() == 0 && link.attr("rel").equals("token_endpoint")) {
                    tokenEndpoint = Utility.checkAbsoluteUrl(link.attr("abs:href"), $domain);
                    numberOfAuthEndpoints++;
                }

                if (micropubEndpoint.length() == 0 && link.attr("rel").equals("micropub")) {
                    hasMicropubOrMicrosub = true;
                    micropubEndpoint = Utility.checkAbsoluteUrl(link.attr("abs:href"), $domain);
                }

                if (microsubEndpoint.length() == 0 && link.attr("rel").equals("microsub")) {
                    hasMicropubOrMicrosub = true;
                    microsubEndpoint = Utility.checkAbsoluteUrl(link.attr("abs:href"), $domain);
                }

                if (micropubMediaEndpoint.length() == 0 && link.attr("rel").equals("micropub_media")) {
                    micropubMediaEndpoint = Utility.checkAbsoluteUrl(link.attr("abs:href"), $domain);
                }
            }

            String noProtocolUrl = $domain.replace("https://","").replace("http://", "");
            try {

                Mf2Parser parser = new Mf2Parser();
                ArrayList<HCard> cards = parser.parse(doc, new URI($domain));

                for (HCard c : cards) {
                    if (c.getUrl() != null && c.getName() != null) {
                        String HCardURL = c.getUrl().replace("https://","").replace("http://", "");
                        if (HCardURL.equals(noProtocolUrl) || HCardURL.equals(noProtocolUrl + "/")) {
                            authorAvatar = c.getAvatar();
                            authorName = c.getName();
                            break;
                        }
                    }
                }
            }
            catch (Exception ignored) { }

        }
        catch (IllegalArgumentException | IOException e) {
            Toast.makeText(getApplicationContext(), String.format(getString(R.string.domain_connect_error), e.getMessage()), Toast.LENGTH_SHORT).show();
        }
        catch (Exception ignored) { }

        // Return true when we have the auth and token endpoint and micropub or microsub.
        return numberOfAuthEndpoints == 2 && hasMicropubOrMicrosub;
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
                            if (accessToken != null && accessToken.length() > 0) {
                                accessTokenFound = true;
                            }

                        }
                        catch (Exception e1) {
                            errorMessage += " - " + e1.getMessage();
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
                        am.setUserData(account, "micropub_media_endpoint", micropubMediaEndpoint);
                        am.setUserData(account, "author_name", authorName);
                        am.setUserData(account, "author_avatar", authorAvatar);

                        // Set first account.
                        if (numberOfAccounts == 0) {
                            SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                            editor.putString("account", domainInput);
                            editor.apply();
                        }

                        // Get micropub configuration.
                        User user = new User();
                        user.setMicropubEndpoint(micropubEndpoint);
                        user.setAccessToken(accessToken);
                        user.setAccount(account);
                        new MicropubConfig(getApplicationContext(), user).refresh();

                        Toast.makeText(getApplicationContext(), R.string.authentication_success, Toast.LENGTH_SHORT).show();

                        // Start launch activity which will determine where it will go.
                        Intent launch = new Intent(getBaseContext(), LaunchActivity.class);
                        startActivity(launch);
                        finish();
                    }
                    else {
                        Toast.makeText(getApplicationContext(), String.format(getString(R.string.authentication_fail_token), errorMessage), Toast.LENGTH_LONG).show();
                        showForm();
                    }
                }
            },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError error) {
                    try {
                        NetworkResponse networkResponse = error.networkResponse;
                        if (networkResponse != null && networkResponse.statusCode != 0 && networkResponse.data != null) {
                            int code = networkResponse.statusCode;
                            String result = new String(networkResponse.data);
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.authentication_fail), code, result), Toast.LENGTH_LONG).show();
                        }
                        else {
                            Toast.makeText(getApplicationContext(), getString(R.string.network_failure) + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    catch (Exception e) {
                        Toast.makeText(getApplicationContext(), String.format(getString(R.string.network_error), e.getMessage()), Toast.LENGTH_LONG).show();
                    }

                    showForm();

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

    /**
     * Change the sign in button.
     *
     * @param text
     *   The text to change the sign in button to.
     */
    public void changeSignInButton(int text) {
        signIn.setText(text);
    }

    /**
     * Show sign in form.
     */
    public void showForm() {
        info.setVisibility(View.VISIBLE);
        domain.setVisibility(View.VISIBLE);
        signIn.setVisibility(View.VISIBLE);
        changeSignInButton(R.string.sign_in_with_domain);
    }

}
