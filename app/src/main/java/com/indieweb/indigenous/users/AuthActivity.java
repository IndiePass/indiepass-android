package com.indieweb.indigenous.users;

import android.Manifest;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.StrictMode;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import android.view.View;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.LaunchActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.indieweb.micropub.MicropubAction;
import com.indieweb.indigenous.model.HCard;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.HTTPRequest;
import com.indieweb.indigenous.util.Utility;
import com.indieweb.indigenous.util.VolleyRequestListener;
import com.indieweb.indigenous.util.mf2.Mf2Parser;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.net.URI;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

public class AuthActivity extends AccountAuthenticatorActivity implements VolleyRequestListener {

    public final static String INDIEWEB_ACCOUNT_TYPE = "IndieAuth";
    public final static String INDIEWEB_TOKEN_TYPE = "IndieAuth";
    public final static String PIXELFED_ACCOUNT_TYPE = "Pixelfed";
    public final static String PIXELFED_TOKEN_TYPE = "Pixelfed";
    public final static String MASTODON_ACCOUNT_TYPE = "Mastodon";
    public final static String MASTODON_TOKEN_TYPE = "Mastodon";
    public final static String PLEROMA_ACCOUNT_TYPE = "Pleroma";
    public final static String PLEROMA_TOKEN_TYPE = "Pleroma";

    String accountType = "indieweb";
    String requestType = "";
    String state;
    Button signIn;
    ImageButton indieWeb;
    ImageButton pixelfed;
    ImageButton mastodon;
    ImageButton pleroma;
    EditText domain;
    TextView info;
    String domainInput;
    LinearLayout signInContainer;
    CheckBox setAsDefault;
    Document doc;
    String authorizationEndpoint;
    String tokenEndpoint;
    String micropubEndpoint;
    String microsubEndpoint;
    String micropubMediaEndpoint;
    String authorAvatar;
    String authorName;
    String codeVerifier = "";
    RelativeLayout layout;
    String pixelfedClientId;
    String pixelfedClientSecret;
    String mastodonClientId;
    String mastodonClientSecret;
    String pleromaClientId;
    String pleromaClientSecret;
    protected VolleyRequestListener volleyRequestListener;

    final String ClientId = "https://indiepass.marksuth.dev/";
    final String RedirectUri = "https://indiepass.marksuth.dev/android-callback";

    /**
     * Set request listener.
     *
     * @param volleyRequestListener
     *   The volley request listener.
     */
    private void VolleyRequestListener(VolleyRequestListener volleyRequestListener) {
        this.volleyRequestListener = volleyRequestListener;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        // Set listener.
        VolleyRequestListener(this);

        layout = findViewById(R.id.auth_root);

        // This requires user permission from 22 on.
        Dexter.withActivity(AuthActivity.this)
                .withPermission(Manifest.permission.GET_ACCOUNTS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {

                        // Generate state, use uuid and take first 10 chars.
                        state = UUID.randomUUID().toString().substring(0, 10);

                        // Generate a code verifier. concatenate 2 uuids.
                        String temp = UUID.randomUUID().toString() + UUID.randomUUID().toString();
                        codeVerifier = temp.replace("-", "");

                        signInContainer = findViewById(R.id.signInContainer);
                        setAsDefault = findViewById(R.id.setAsDefault);
                        domain = findViewById(R.id.domain);
                        info = findViewById(R.id.info);
                        signIn = findViewById(R.id.signInButton);
                        signIn.setOnClickListener(doSignIn);

                        indieWeb = findViewById(R.id.indieweb);
                        indieWeb.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                accountType = "indieweb";
                                signInContainer.setVisibility(View.VISIBLE);
                                info.setText(getString(R.string.sign_in_indieauth_info));
                            }
                        });

                        mastodon = findViewById(R.id.mastodon);
                        mastodon.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                accountType = "mastodon";
                                info.setText(getString(R.string.sign_in_mastodon_info));
                                signInContainer.setVisibility(View.VISIBLE);
                            }
                        });

                        pleroma = findViewById(R.id.pleroma);
                        pleroma.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                accountType = "pleroma";
                                info.setText(getString(R.string.sign_in_pleroma_info));
                                signInContainer.setVisibility(View.VISIBLE);
                            }
                        });

                        pixelfed = findViewById(R.id.pixelfed);
                        pixelfed.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                accountType = "pixelfed";
                                info.setText(getString(R.string.sign_in_pixelfed_info));
                                signInContainer.setVisibility(View.VISIBLE);
                            }
                        });

                        // Show 'select account' button. Always show as soon as there is one
                        // account, in case there are problems with the default account.
                        if (new Accounts(AuthActivity.this).getCount() > 0) {
                            LinearLayout selectContainer = findViewById(R.id.selectContainer);
                            selectContainer.setVisibility(View.VISIBLE);
                            Button selectAccount = findViewById(R.id.selectAccountButton);
                            selectAccount.setOnClickListener(selectAccountListener);
                            setAsDefault.setVisibility(View.VISIBLE);
                        }
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            Utility.openSettings(getApplicationContext());
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).check();

        Utility.setNightTheme(getApplicationContext());
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        if (Intent.ACTION_VIEW.equals(intent.getAction())) {

            Snackbar.make(layout, getString(R.string.validating_code), Snackbar.LENGTH_SHORT).show();

            // Get the code and state.
            String code = intent.getData().getQueryParameter("code");
            String returnedState = intent.getData().getQueryParameter("state");
            if (accountType.equals("indieweb") && code != null && code.length() > 0 && returnedState != null && returnedState.length() > 0) {
                validateIndieWebCode(code, returnedState);
            }
            else if (accountType.equals("pixelfed") && code != null && code.length() > 0) {
                validatePixelfedCode(code);
            }
            else if (accountType.equals("mastodon") && code != null && code.length() > 0) {
                validateMastodonCode(code);
            }
            else if (accountType.equals("pleroma") && code != null && code.length() > 0) {
                validatePleromaCode(code);
            }
            else {
                final Snackbar snack = Snackbar.make(layout, getString(R.string.no_code_found), Snackbar.LENGTH_INDEFINITE);
                snack.setAction(getString(R.string.close), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    }
                );
                snack.show();
            }
        }
    }

    /**
     * OnClickListener for the 'Set account' button.
     */
    public final View.OnClickListener selectAccountListener = new View.OnClickListener() {
        public void onClick(View v) {
            new Accounts(AuthActivity.this).selectAccount(AuthActivity.this, layout);
        }
    };

    /**
     * OnClickListener for the 'Sign in' button.
     */
    private final View.OnClickListener doSignIn = new View.OnClickListener() {
        public void onClick(View v) {

            if (!Utility.hasConnection(getApplicationContext())) {
                Snackbar.make(layout, getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
                return;
            }

            domainInput = domain.getText().toString();

            // Check if there's no protocol, prefix it with https:// if necessary.
            if (!domainInput.contains("http://") && !domainInput.contains("https://")) {
                domainInput = "https://" + domainInput;
            }

            if (accountType.equals("indieweb")) {
                registerWithIndieWeb();
            }

            if (accountType.equals("pixelfed")) {
                registerWithPixelfed();
            }

            if (accountType.equals("mastodon")) {
                registerWithMastodon();
            }

            if (accountType.equals("pleroma")) {
                registerWithPleroma();
            }
        }
    };

    /**
     * Change the sign-in button.
     *
     * @param text
     *   The text to change the sign-in button to.
     */
    public void changeSignInButton(int text) {
        signIn.setText(text);
    }

    /**
     * Show sign in form and reset variables.
     */
    public void showForm() {
        info.setVisibility(View.VISIBLE);
        domain.setVisibility(View.VISIBLE);
        signIn.setVisibility(View.VISIBLE);
        changeSignInButton(R.string.sign_in);
    }


    @Override
    public void OnSuccessRequest(String response) {
        if (requestType.equals("pixelfedAppRegister")) {
            handlePixelfedAppRegister(response);
        }

        if (requestType.equals("pixelfedAccessToken")) {
            handlePixelfedAccessToken(response);
        }

        if (requestType.equals("mastodonAppRegister")) {
            handleMastodonAppRegister(response);
        }

        if (requestType.equals("mastodonAccessToken")) {
            handleMastodonAccessToken(response);
        }

        if (requestType.equals("pleromaAppRegister")) {
            handlePleromaAppRegister(response);
        }

        if (requestType.equals("pleromaAccessToken")) {
            handlePleromaAccessToken(response);
        }

    }

    @Override
    public void OnFailureRequest(VolleyError error) {
        showForm();
        String message = getString(R.string.request_failed_unknown);
        try {
            message = Utility.parseNetworkError(error, getApplicationContext(), R.string.request_failed, R.string.request_failed_unknown);
        }
        catch (Exception ignored) {}
        Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
    }

    /**
     * Start the authorization.
     *
     * @param uri
     *   The URI to open.
     */
    public void startAuthorization(Uri uri) {
        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
        intentBuilder.setToolbarColor(ContextCompat.getColor(AuthActivity.this, R.color.colorPrimary));
        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(AuthActivity.this, R.color.colorPrimaryDark));
        CustomTabsIntent customTabsIntent = intentBuilder.build();
        customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        customTabsIntent.launchUrl(AuthActivity.this, uri);
    }

    /**
     * Set default account.
     */
    public void setDefaultAccount() {
        int numberOfAccounts = new Accounts(AuthActivity.this).getCount();
        if (numberOfAccounts == 0 || (setAsDefault.getVisibility() == View.VISIBLE && setAsDefault.isChecked())) {
            SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
            editor.putString("account", getAccountName());
            editor.apply();
        }
    }

    /**
     * Creates a unique account name based on domain input.
     *
     * Use this function when creating a new account.
     *
     * @return String
     */
    public String getAccountName() {
        String accountName = domainInput;

        accountName += "@" + state.replace("-", "");

        return accountName;
    }

    /**
     * Register with IndieWeb.
     */
    private void registerWithIndieWeb() {

        // Reset variables.
        authorizationEndpoint = "";
        tokenEndpoint = "";
        micropubEndpoint = "";
        microsubEndpoint = "";
        micropubMediaEndpoint = "";
        authorName = "";
        authorAvatar = "";

        changeSignInButton(R.string.connecting);
        if (validIndieWebDomain(domainInput)) {
            String codeChallenge = Utility.sha256(codeVerifier);
            String url = authorizationEndpoint + "?code_challenge_method=S256&code_challenge=" + codeChallenge + "&response_type=code&redirect_uri=" + RedirectUri + "&client_id=" + ClientId + "&me=" + domainInput + "&scope=create+update+delete+media+read+follow+channels+mute+block&state=" + state;
            Uri uri = Uri.parse(url);
            startAuthorization(uri);
        }
        else {
            changeSignInButton(R.string.sign_in);
            final Snackbar snack = Snackbar.make(layout, getString(R.string.missing_rel_links), Snackbar.LENGTH_INDEFINITE);
            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    }
            );
            snack.show();
        }
    }

    /**
     * Validates the domain for IndieWeb.
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
    private boolean validIndieWebDomain(String $domain) {
        int numberOfAuthEndpoints = 0;
        boolean hasMicropubOrMicrosub = false;

        // This crashes on 7.1.
        if (android.os.Build.VERSION.SDK_INT != 25) {
            Snackbar.make(layout, getString(R.string.connection_to_domain), Snackbar.LENGTH_SHORT).show();
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
            doc = connection.get();
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

        }
        catch (IllegalArgumentException | IOException e) {
            final Snackbar snack = Snackbar.make(layout, String.format(getString(R.string.domain_connect_error), e.getMessage()), Snackbar.LENGTH_INDEFINITE);
            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    }
            );
            snack.show();
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
    private void validateIndieWebCode(final String code, final String returnedState) {
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

                            // Check profile key.
                            if (indieAuthResponse.has("profile")) {
                                JSONObject profile = indieAuthResponse.getJSONObject("profile");
                                if (profile.has("name")) {
                                    authorName = profile.getString("name");
                                }
                                if (profile.has("photo")) {
                                    authorAvatar = profile.getString("photo");
                                }
                            }
                        }
                        catch (JSONException e) {

                            // Catch the json exception. However, we're not done yet.
                            errorMessage = e.getMessage();

                            // Known, and maybe other projects, do not return a json response (yet),
                            // so the access token might be in the body as a URL-encoded query
                            // string.
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

                            // If author name or avatar are still empty, try parsing the HTML.
                            if (authorName.length() == 0 || authorAvatar.length() == 0) {
                                String noProtocolUrl = domainInput.replace("https://","").replace("http://", "");
                                try {

                                    Mf2Parser parser = new Mf2Parser();
                                    ArrayList<HCard> cards = parser.parse(doc, new URI(domainInput));

                                    for (HCard c : cards) {
                                        if (c.getUrl() != null && c.getName() != null) {
                                            String HCardURL = c.getUrl().replace("https://","").replace("http://", "");
                                            if (HCardURL.equals(noProtocolUrl) || HCardURL.equals(noProtocolUrl + "/")) {
                                                if (authorName.length() == 0) {
                                                    authorName = c.getName();
                                                }
                                                if (authorAvatar.length() == 0) {
                                                    authorAvatar = c.getAvatar();
                                                }
                                                break;
                                            }
                                        }
                                    }
                                }
                                catch (Exception ignored) { }
                            }

                            AccountManager am = AccountManager.get(getApplicationContext());

                            // Create new account.
                            Account account = new Account(getAccountName(), INDIEWEB_ACCOUNT_TYPE);
                            am.addAccountExplicitly(account, null, null);
                            am.setAuthToken(account, INDIEWEB_TOKEN_TYPE, accessToken);
                            am.setUserData(account, "micropub_endpoint", micropubEndpoint);
                            am.setUserData(account, "microsub_endpoint", microsubEndpoint);
                            am.setUserData(account, "authorization_endpoint", authorizationEndpoint);
                            am.setUserData(account, "token_endpoint", tokenEndpoint);
                            am.setUserData(account, "micropub_media_endpoint", micropubMediaEndpoint);
                            am.setUserData(account, "author_name", authorName);
                            am.setUserData(account, "author_avatar", authorAvatar);

                            // Set default account.
                            setDefaultAccount();

                            // Get micropub configuration.
                            User user = new User();
                            user.setMicropubEndpoint(micropubEndpoint);
                            user.setAccessToken(accessToken);
                            user.setAccount(account);
                            new MicropubAction(getApplicationContext(), user, layout).refreshConfig();

                            Snackbar.make(layout, getString(R.string.authentication_success), Snackbar.LENGTH_SHORT).show();

                            // Start launch activity which will determine where it will go.
                            new Handler().postDelayed(new Runnable() {
                                @Override
                                public void run() {
                                    Intent launch = new Intent(getBaseContext(), LaunchActivity.class);
                                    startActivity(launch);
                                    finish();
                                }
                            }, 700);
                        }
                        else {
                            final Snackbar snack = Snackbar.make(layout, String.format(getString(R.string.authentication_fail_token), errorMessage), Snackbar.LENGTH_INDEFINITE);
                            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                                        @Override
                                        public void onClick(View v) {
                                            snack.dismiss();
                                        }
                                    }
                            );
                            snack.show();
                            showForm();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = Utility.parseNetworkError(error, getApplicationContext(), R.string.authentication_fail, R.string.network_error);
                        final Snackbar snack = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE);
                        snack.setAction(getString(R.string.close), new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        snack.dismiss();
                                    }
                                }
                        );
                        snack.show();
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
                params.put("code_verifier", codeVerifier);

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
     * Register with a Pixelfed instance.
     */
    private void registerWithPixelfed() {
        domainInput = Utility.stripEndingSlash(domainInput);

        if (URLUtil.isValidUrl( domainInput)) {
            changeSignInButton(R.string.connecting);
            registerPixelfedApp();
        }
        else {
            changeSignInButton(R.string.sign_in);
            final Snackbar snack = Snackbar.make(layout, getString(R.string.invalid_url), Snackbar.LENGTH_INDEFINITE);
            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    }
            );
            snack.show();
        }
    }

    /**
     * Register Pixelfed application.
     */
    private void registerPixelfedApp() {
        requestType = "pixelfedAppRegister";
        Map<String, String> params = new HashMap<>();
        params.put("client_name", "Indigenous");
        params.put("website", ClientId);
        params.put("redirect_uris", RedirectUri);
        params.put("scopes", "read write follow push");

        String appUrl = domainInput + "/api/v1/apps";
        HTTPRequest r = new HTTPRequest(this.volleyRequestListener, null, getApplicationContext());
        r.doPostRequest(appUrl, params);
    }

    /**
     * Handle the register response.
     *
     * @param response
     *   The current response.
     */
    private void handlePixelfedAppRegister(String response) {
        try {
            JSONObject object = new JSONObject(response);
            pixelfedClientId = object.getString("client_id");
            pixelfedClientSecret = object.getString("client_secret");
            authorizePixelfed();
        }
        catch (JSONException e) {
            Snackbar.make(layout, getString(R.string.error_parsing_app_registration_response), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Authorize with pixelfed.
     */
    private void authorizePixelfed() {
        String url = domainInput + "/oauth/authorize?response_type=code&redirect_uri=" + RedirectUri + "&client_id=" + pixelfedClientId + "&scope=read+write+follow+push";
        Uri uri = Uri.parse(url);
        startAuthorization(uri);
    }

    /**
     * Validate pixelfed code and get access token.
     *
     * @param code
     *   The code from the authorize call.
     */
    private void validatePixelfedCode(String code) {
        requestType = "pixelfedAccessToken";
        Map<String, String> params = new HashMap<>();
        params.put("client_id", pixelfedClientId);
        params.put("client_secret", pixelfedClientSecret);
        params.put("redirect_uri", RedirectUri);
        params.put("scope", "read write follow push");
        params.put("code", code);
        params.put("grant_type", "authorization_code");

        String appUrl = domainInput + "/oauth/token";
        HTTPRequest r = new HTTPRequest(this.volleyRequestListener, null, getApplicationContext());
        r.doPostRequest(appUrl, params);
    }

    /**
     * Handle the access token response.
     *
     * @param response
     *   The current response
     */
    private void handlePixelfedAccessToken(String response) {
        String error = getString(R.string.request_failed_unknown);
        String accessToken = "";
        try {
            JSONObject object = new JSONObject(response);
            accessToken = object.getString("access_token");
        }
        catch (JSONException e) {
            error = e.getMessage();
        }

        if (accessToken.length() > 0) {

            AccountManager am = AccountManager.get(getApplicationContext());

            // Create new account.
            Account account = new Account(getAccountName(), PIXELFED_ACCOUNT_TYPE);
            am.addAccountExplicitly(account, null, null);
            am.setAuthToken(account, PIXELFED_TOKEN_TYPE, accessToken);
            am.setUserData(account, "client_id", pixelfedClientId);
            am.setUserData(account, "client_secret", pixelfedClientSecret);

            // Set default account.
            setDefaultAccount();

            Snackbar.make(layout, getString(R.string.authentication_success), Snackbar.LENGTH_SHORT).show();

            requestType = "pixelfedInitialSync";
            syncPixelfed(new Accounts(AuthActivity.this).getUser(getAccountName(), true, false));
        }
        else {
            final Snackbar snack = Snackbar.make(layout, String.format(getString(R.string.authentication_fail_token), error), Snackbar.LENGTH_INDEFINITE);
            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    }
            );
            snack.show();
            showForm();
        }
    }

    /**
     * Sync pixelfed
     *
     * @param user
     *   The current user
     */
    private void syncPixelfed(User user) {
        Auth auth = AuthFactory.getAuth(user, AuthActivity.this);
        auth.syncAccount(layout);

        // Start launch activity which will determine where it will go.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent launch = new Intent(getBaseContext(), LaunchActivity.class);
                startActivity(launch);
                finish();
            }
        }, 1000);
    }

    /**
     * Register with a Mastodon instance.
     */
    private void registerWithMastodon() {
        domainInput = Utility.stripEndingSlash(domainInput);

        if (URLUtil.isValidUrl( domainInput)) {
            changeSignInButton(R.string.connecting);
            registerMastodonApp();
        }
        else {
            changeSignInButton(R.string.sign_in);
            final Snackbar snack = Snackbar.make(layout, getString(R.string.invalid_url), Snackbar.LENGTH_INDEFINITE);
            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    }
            );
            snack.show();
        }
    }


    /**
     * Register mastodon application.
     */
    private void registerMastodonApp() {
        requestType = "mastodonAppRegister";
        Map<String, String> params = new HashMap<>();
        params.put("client_name", "Indigenous");
        params.put("website", ClientId);
        params.put("redirect_uris", RedirectUri);
        params.put("scopes", "read write follow push");

        String appUrl = domainInput + "/api/v1/apps";
        HTTPRequest r = new HTTPRequest(this.volleyRequestListener, null, getApplicationContext());
        r.doPostRequest(appUrl, params);
    }

    /**
     * Handle the register response
     *
     * @param response
     *   The current response.
     */
    private void handleMastodonAppRegister(String response) {
        try {
            JSONObject object = new JSONObject(response);
            mastodonClientId = object.getString("client_id");
            mastodonClientSecret = object.getString("client_secret");
            authorizeMastodon();
        }
        catch (JSONException e) {
            Snackbar.make(layout, getString(R.string.error_parsing_app_registration_response), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Authorize with mastodon.
     */
    private void authorizeMastodon() {
        String url = domainInput + "/oauth/authorize?response_type=code&redirect_uri=" + RedirectUri + "&client_id=" + mastodonClientId + "&scope=read+write+follow+push";
        Uri uri = Uri.parse(url);
        startAuthorization(uri);
    }

    /**
     * Validate mastodon code and get access token.
     *
     * @param code
     *   The code from the authorize call.
     */
    private void validateMastodonCode(String code) {
        requestType = "mastodonAccessToken";
        Map<String, String> params = new HashMap<>();
        params.put("client_id", mastodonClientId);
        params.put("client_secret", mastodonClientSecret);
        params.put("redirect_uri", RedirectUri);
        params.put("scope", "read write follow push");
        params.put("code", code);
        params.put("grant_type", "authorization_code");

        String appUrl = domainInput + "/oauth/token";
        HTTPRequest r = new HTTPRequest(this.volleyRequestListener, null, getApplicationContext());
        r.doPostRequest(appUrl, params);
    }

    /**
     * Handle the access token response.
     *
     * @param response
     *   The current response
     */
    private void handleMastodonAccessToken(String response) {
        String error = getString(R.string.request_failed_unknown);
        String accessToken = "";
        try {
            JSONObject object = new JSONObject(response);
            accessToken = object.getString("access_token");
        }
        catch (JSONException e) {
            error = e.getMessage();
        }

        if (accessToken.length() > 0) {

            AccountManager am = AccountManager.get(getApplicationContext());

            // Create new account.
            Account account = new Account(getAccountName(), MASTODON_ACCOUNT_TYPE);
            am.addAccountExplicitly(account, null, null);
            am.setAuthToken(account, MASTODON_TOKEN_TYPE, accessToken);
            am.setUserData(account, "client_id", mastodonClientId);
            am.setUserData(account, "client_secret", mastodonClientSecret);

            // Set default account.
            setDefaultAccount();

            Snackbar.make(layout, getString(R.string.authentication_success), Snackbar.LENGTH_SHORT).show();

            requestType = "mastodonInitialSync";
            syncMastodon(new Accounts(AuthActivity.this).getUser(getAccountName(), true, false));
        }
        else {
            final Snackbar snack = Snackbar.make(layout, String.format(getString(R.string.authentication_fail_token), error), Snackbar.LENGTH_INDEFINITE);
            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    }
            );
            snack.show();
            showForm();
        }
    }

    /**
     * Sync mastodon
     *
     * @param user
     *   The current user
     */
    private void syncMastodon(User user) {
        Auth auth = AuthFactory.getAuth(user, AuthActivity.this);
        auth.syncAccount(layout);

        // Start launch activity which will determine where it will go.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent launch = new Intent(getBaseContext(), LaunchActivity.class);
                startActivity(launch);
                finish();
            }
        }, 1000);
    }

    /**
     * Register with a Pleroma instance.
     */
    private void registerWithPleroma() {
        domainInput = Utility.stripEndingSlash(domainInput);

        if (URLUtil.isValidUrl( domainInput)) {
            changeSignInButton(R.string.connecting);
            registerPleromaApp();
        }
        else {
            changeSignInButton(R.string.sign_in);
            final Snackbar snack = Snackbar.make(layout, getString(R.string.invalid_url), Snackbar.LENGTH_INDEFINITE);
            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    }
            );
            snack.show();
        }
    }

    /**
     * Register pleroma application.
     */
    private void registerPleromaApp() {
        requestType = "pleromaAppRegister";
        Map<String, String> params = new HashMap<>();
        params.put("client_name", "Indigenous");
        params.put("website", ClientId);
        params.put("redirect_uris", RedirectUri);
        params.put("scopes", "read write follow push");

        String appUrl = domainInput + "/api/v1/apps";
        HTTPRequest r = new HTTPRequest(this.volleyRequestListener, null, getApplicationContext());
        r.doPostRequest(appUrl, params);
    }

    /**
     * Handle the register response
     *
     * @param response
     *   The current response.
     */
    private void handlePleromaAppRegister(String response) {
        try {
            JSONObject object = new JSONObject(response);
            pleromaClientId = object.getString("client_id");
            pleromaClientSecret = object.getString("client_secret");
            authorizePleroma();
        }
        catch (JSONException e) {
            Snackbar.make(layout, getString(R.string.error_parsing_app_registration_response), Snackbar.LENGTH_LONG).show();
        }
    }

    /**
     * Authorize with pleroma.
     */
    private void authorizePleroma() {
        String url = domainInput + "/oauth/authorize?response_type=code&redirect_uri=" + RedirectUri + "&client_id=" + pleromaClientId + "&scope=read+write+follow+push";
        Uri uri = Uri.parse(url);
        startAuthorization(uri);
    }

    /**
     * Validate pleroma code and get access token.
     *
     * @param code
     *   The code from the authorize call.
     */
    private void validatePleromaCode(String code) {
        requestType = "pleromaAccessToken";
        Map<String, String> params = new HashMap<>();
        params.put("client_id", pleromaClientId);
        params.put("client_secret", pleromaClientSecret);
        params.put("redirect_uri", RedirectUri);
        params.put("scope", "read write follow push");
        params.put("code", code);
        params.put("grant_type", "authorization_code");

        String appUrl = domainInput + "/oauth/token";
        HTTPRequest r = new HTTPRequest(this.volleyRequestListener, null, getApplicationContext());
        r.doPostRequest(appUrl, params);
    }

    /**
     * Handle the access token response.
     *
     * @param response
     *   The current response
     */
    private void handlePleromaAccessToken(String response) {
        String error = getString(R.string.request_failed_unknown);
        String accessToken = "";
        try {
            JSONObject object = new JSONObject(response);
            accessToken = object.getString("access_token");
        }
        catch (JSONException e) {
            error = e.getMessage();
        }

        if (accessToken.length() > 0) {

            AccountManager am = AccountManager.get(getApplicationContext());

            // Create new account.
            Account account = new Account(getAccountName(), PLEROMA_ACCOUNT_TYPE);
            am.addAccountExplicitly(account, null, null);
            am.setAuthToken(account, PLEROMA_TOKEN_TYPE, accessToken);
            am.setUserData(account, "client_id", pleromaClientId);
            am.setUserData(account, "client_secret", pleromaClientSecret);

            // Set default account.
            setDefaultAccount();

            Snackbar.make(layout, getString(R.string.authentication_success), Snackbar.LENGTH_SHORT).show();

            requestType = "pleromaInitialSync";
            syncPleroma(new Accounts(AuthActivity.this).getUser(getAccountName(), true, false));
        }
        else {
            final Snackbar snack = Snackbar.make(layout, String.format(getString(R.string.authentication_fail_token), error), Snackbar.LENGTH_INDEFINITE);
            snack.setAction(getString(R.string.close), new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            snack.dismiss();
                        }
                    }
            );
            snack.show();
            showForm();
        }
    }

    /**
     * Sync pleroma
     *
     * @param user
     *   The current user
     */
    private void syncPleroma(User user) {
        Auth auth = AuthFactory.getAuth(user, AuthActivity.this);
        auth.syncAccount(layout);

        // Start launch activity which will determine where it will go.
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent launch = new Intent(getBaseContext(), LaunchActivity.class);
                startActivity(launch);
                finish();
            }
        }, 1000);
    }

}
