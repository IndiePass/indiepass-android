package com.indieweb.indigenous.push;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.BuildConfig;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;

import java.util.HashMap;
import java.util.Map;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.model.PushyDeviceCredentials;
import me.pushy.sdk.util.PushyServiceManager;

public class PushNotificationActivity extends AppCompatActivity {

    TextView info;
    TextView siteApiToken;
    Button buttonPushyMe;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_notification);

        info = findViewById(R.id.pushNotificationsInfo);
        siteApiToken = findViewById(R.id.siteApiToken);
        buttonPushyMe = findViewById(R.id.buttonPushyMe);

        setConfigurationInfo(Preferences.getPreference(getApplicationContext(), "push_notification_type", "none"));
    }

    /**
     * Set configuration info.
     */
    public void setConfigurationInfo(String type) {

        if (type.equals("pushy")) {
            info.setText(R.string.push_notification_configuration_pushy);
            siteApiToken.setVisibility(View.GONE);
            buttonPushyMe.setText(R.string.disable_pushy_me_button);
            buttonPushyMe.setOnClickListener(null);
            buttonPushyMe.setOnClickListener(new disablePushyMeOnClickListener());
        }
        else {
            info.setText(R.string.push_notification_no_configuration);
            siteApiToken.setVisibility(View.VISIBLE);
            buttonPushyMe.setText(R.string.register_device);
            buttonPushyMe.setOnClickListener(null);
            buttonPushyMe.setOnClickListener(new registerDeviceOnPushyMeOnClickListener());
        }

    }

    /**
     * Disable Pushy.me service.
     */
    class disablePushyMeOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(PushNotificationActivity.this);
            builder.setTitle(R.string.disable_pushy_me_title);
            builder.setPositiveButton(getApplicationContext().getString(android.R.string.yes),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    disablePushyMe();
                }
            });
            builder.setNegativeButton(getApplicationContext().getString(android.R.string.no), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    /**
     * Register device on PushyMe
     */
    class registerDeviceOnPushyMeOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            if (TextUtils.isEmpty(siteApiToken.getText())) {
                siteApiToken.setError(getString(R.string.required_field));
                return;
            }

            if (!new Connection(getApplicationContext()).hasConnection()) {
                Toast.makeText(getApplicationContext(), getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
                return;
            }

            Toast.makeText(getApplicationContext(), R.string.push_notifications_registering, Toast.LENGTH_SHORT).show();
            if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                // Request both READ_EXTERNAL_STORAGE and WRITE_EXTERNAL_STORAGE so that the
                // Pushy SDK will be able to persist the device token in the external storage
                ActivityCompat.requestPermissions(PushNotificationActivity.this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
            }

            // If not registered with Pushy.me, check account first.
            if (!Pushy.isRegistered(getApplicationContext())) {
                initiatePushyMeRegistration();
            }
            else {
                // Store the Pushy.me token on the backend. It's possible the previous call failed.
                // The backend also ignores existing devices, so no harm in calling it again.
                PushyDeviceCredentials credentials = Pushy.getDeviceCredentials(getApplicationContext());
                if (credentials != null && credentials.token.length() > 0) {
                    storePushyMeTokenOnBackend(credentials.token);
                }
                else {
                    Toast.makeText(getApplicationContext(), "Pushy.me token not found to register on backend.", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Register device at Pushy.me.
     */
    public void registerDeviceAtPushyMe() {
        String deviceToken = null;

        try {
            // Assign a unique token to this device.
            deviceToken = Pushy.register(getApplicationContext());
        }
        catch (Exception exc) {
            Toast.makeText(getApplicationContext(), "Error registering with Pushy.me: " + exc.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (deviceToken != null && deviceToken.length() > 0) {
            storePushyMeTokenOnBackend(deviceToken);
        }
    }

    /**
     * Enable Pushy.me
     */
    public void enablePushyMe() {
        setConfigurationInfo("pushy");
        PushyServiceManager.start(getApplicationContext());
        Preferences.setPreference(getApplicationContext(), "push_notification_type", "pushy");
    }

    /**
     * Disable Pushy.me
     */
    public void disablePushyMe() {
        setConfigurationInfo("none");
        PushyServiceManager.stop(getApplicationContext());
        Preferences.setPreference(getApplicationContext(), "push_notification_type", "none");
    }

    /**
     * Checks if an account exists on the backend. If ok, we register with Pushy.me as well.
     */
    public void initiatePushyMeRegistration() {

        // Do not run this in case there's no endpoint.
        //noinspection ConstantConditions
        if (BuildConfig.SITE_DEVICE_REGISTRATION_ENDPOINT.length() == 0 || BuildConfig.SITE_ACCOUNT_CHECK_ENDPOINT.length() == 0) {
            return;
        }

        StringRequest getRequest = new StringRequest(Request.Method.POST, BuildConfig.SITE_ACCOUNT_CHECK_ENDPOINT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        registerDeviceAtPushyMe();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = "unknown error";
                        int code = error.networkResponse.statusCode;
                        switch (code) {
                            case 404:
                                message = "Account not found";
                                break;
                            case 400:
                                message = "Account is blocked";
                                break;
                        }
                        Toast.makeText(getApplicationContext(), "Error checking account: " + message, Toast.LENGTH_LONG).show();
                    }
                }
        )
        {

            @Override
            protected Map<String, String> getParams() {
                User user = new Accounts(getApplicationContext()).getCurrentUser();
                Map<String, String> params = new HashMap<>();
                params.put("url", user.getMe());
                params.put("apiToken", siteApiToken.getText().toString());
                return params;
            }

        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);
    }

    /**
     * Registers the Pushy device token to backend.
     *
     * @param deviceToken
     *   The Pushy device token.
     */
    public void storePushyMeTokenOnBackend(final String deviceToken) {

        // Do not run this in case there's no endpoint.
        //noinspection ConstantConditions
        if (BuildConfig.SITE_DEVICE_REGISTRATION_ENDPOINT.length() == 0 || BuildConfig.SITE_ACCOUNT_CHECK_ENDPOINT.length() == 0) {
            return;
        }

        StringRequest getRequest = new StringRequest(Request.Method.POST, BuildConfig.SITE_DEVICE_REGISTRATION_ENDPOINT,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        enablePushyMe();
                        Toast.makeText(getApplicationContext(), "Registration successful", Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = "unknown error";
                        int code = error.networkResponse.statusCode;
                        switch (code) {
                            case 404:
                                message = "Account not found";
                                break;
                            case 400:
                                message = "Account is blocked";
                                break;
                        }
                        Toast.makeText(getApplicationContext(), "Error storing device: " + message, Toast.LENGTH_LONG).show();
                    }
                }
        )
        {

            @Override
            protected Map<String, String> getParams() {
                User user = new Accounts(getApplicationContext()).getCurrentUser();
                Map<String, String> params = new HashMap<>();
                params.put("url", user.getMe());
                params.put("apiToken", siteApiToken.getText().toString());
                params.put("deviceToken", deviceToken);
                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);
    }

}
