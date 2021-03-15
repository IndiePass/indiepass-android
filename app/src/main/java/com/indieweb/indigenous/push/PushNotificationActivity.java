// SPDX-FileCopyrightText: 2018 Kristof De Jaeger <swentel@realize.be>
// SPDX-License-Identifier: GPL-3.0-only

package com.indieweb.indigenous.push;

import android.Manifest;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.TextView;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.BuildConfig;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.Accounts;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import java.util.HashMap;
import java.util.Map;

import me.pushy.sdk.Pushy;
import me.pushy.sdk.model.PushyDeviceCredentials;
import me.pushy.sdk.util.PushyServiceManager;

public class PushNotificationActivity extends AppCompatActivity {

    TextView info;
    TextView siteApiToken;
    Button buttonPushyMe;
    Button buttonMqqt;
    ScrollView layout;
    Spinner mqttServerType;
    TextView mqttHost;
    TextView mqttPort;
    TextView mqttUsername;
    TextView mqttPassword;
    TextView mqttTopic;
    public static final String MQTT5 = "MQTT 5.0";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_push_notification);

        info = findViewById(R.id.pushNotificationsInfo);
        siteApiToken = findViewById(R.id.siteApiToken);
        buttonPushyMe = findViewById(R.id.buttonPushyMe);
        buttonMqqt = findViewById(R.id.buttonMQtt);
        layout = findViewById(R.id.push_notification_root);
        mqttServerType = findViewById(R.id.mqttServerType);
        mqttHost = findViewById(R.id.mqttHost);
        mqttPort = findViewById(R.id.mqttPort);
        mqttUsername = findViewById(R.id.mqttUsername);
        mqttPassword = findViewById(R.id.mqttPassword);
        mqttTopic = findViewById(R.id.mqttTopic);

        siteApiToken.setText(Preferences.getPreference(getApplicationContext(), "siteApiToken", ""));
        mqttHost.setText(Preferences.getPreference(getApplicationContext(), "mqttHost", ""));
        mqttPort.setText(Preferences.getPreference(getApplicationContext(), "mqttPort", ""));
        mqttUsername.setText(Preferences.getPreference(getApplicationContext(), "mqttUsername", ""));
        mqttPassword.setText(Preferences.getPreference(getApplicationContext(), "mqttPassword", ""));
        mqttTopic.setText(Preferences.getPreference(getApplicationContext(), "mqttTopic", ""));
        if (Preferences.getPreference(getApplicationContext(), "mqttServerType", "").equals(MQTT5)) {
            mqttServerType.setSelection(1);
        }

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
            buttonMqqt.setText(R.string.register_mqtt);
            buttonMqqt.setOnClickListener(null);
            buttonMqqt.setOnClickListener(new registerDeviceOnMQTTServerClickListener());
        }
        else if (type.equals("mqtt")) {
            info.setText(R.string.push_notification_configuration_mqtt);
            siteApiToken.setVisibility(View.VISIBLE);
            buttonMqqt.setText(R.string.disable_mqtt_button);
            buttonMqqt.setOnClickListener(null);
            buttonMqqt.setOnClickListener(new disableMqttOnClickListener());
            buttonPushyMe.setText(R.string.register_device);
            buttonPushyMe.setOnClickListener(null);
            buttonPushyMe.setOnClickListener(new registerDeviceOnPushyMeOnClickListener());
        }
        else {
            info.setText(R.string.push_notification_no_configuration);
            siteApiToken.setVisibility(View.VISIBLE);
            buttonPushyMe.setText(R.string.register_device);
            buttonPushyMe.setOnClickListener(null);
            buttonPushyMe.setOnClickListener(new registerDeviceOnPushyMeOnClickListener());
            buttonMqqt.setText(R.string.register_mqtt);
            buttonMqqt.setOnClickListener(null);
            buttonMqqt.setOnClickListener(new registerDeviceOnMQTTServerClickListener());
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

    class disableMqttOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(PushNotificationActivity.this);
            builder.setTitle(R.string.disable_mqtt_title);
            builder.setPositiveButton(getApplicationContext().getString(android.R.string.yes),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    disableMqtt();
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

            if (!Utility.hasConnection(getApplicationContext())) {
                Snackbar.make(layout, getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Save API token.
            Preferences.setPreference(getApplicationContext(), "siteApiToken", siteApiToken.getText().toString());

            Snackbar.make(layout, getString(R.string.push_notifications_registering), Snackbar.LENGTH_SHORT).show();
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
                    Snackbar.make(layout, getString(R.string.pushy_me_token_not_found), Snackbar.LENGTH_SHORT).show();
                }
            }
        }
    }

    /**
     * Register device on custom MQTT server.
     */
    class registerDeviceOnMQTTServerClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {

            boolean hasErrors = false;
            if (TextUtils.isEmpty(mqttHost.getText())) {
                hasErrors = true;
                mqttHost.setError(getString(R.string.required_field));
            }

            if (TextUtils.isEmpty(mqttPort.getText())) {
                hasErrors = true;
                mqttPort.setError(getString(R.string.required_field));
            }

            if (TextUtils.isEmpty(mqttUsername.getText())) {
                hasErrors = true;
                mqttUsername.setError(getString(R.string.required_field));
            }

            if (TextUtils.isEmpty(mqttPassword.getText())) {
                hasErrors = true;
                mqttPassword.setError(getString(R.string.required_field));
            }

            if (TextUtils.isEmpty(mqttTopic.getText())) {
                hasErrors = true;
                mqttTopic.setError(getString(R.string.required_field));
            }

            if (hasErrors) {
                return;
            }

            if (!Utility.hasConnection(getApplicationContext())) {
                Snackbar.make(layout, getString(R.string.no_connection), Snackbar.LENGTH_SHORT).show();
                return;
            }

            // Save all values.
            Preferences.setPreference(getApplicationContext(), "mqttServerType", mqttServerType.getSelectedItem().toString());
            Preferences.setPreference(getApplicationContext(), "mqttHost", mqttHost.getText().toString());
            Preferences.setPreference(getApplicationContext(), "mqttPort", mqttPort.getText().toString());
            Preferences.setPreference(getApplicationContext(), "mqttUsername", mqttUsername.getText().toString());
            Preferences.setPreference(getApplicationContext(), "mqttPassword", mqttPassword.getText().toString());
            Preferences.setPreference(getApplicationContext(), "mqttTopic", mqttTopic.getText().toString());

            Snackbar.make(layout, getString(R.string.push_notifications_registering), Snackbar.LENGTH_SHORT).show();
            enableMqtt();
        }
    }

    /**
     * Enable Pushy.me
     */
    public void enablePushyMe() {
        if (Preferences.getPreference(getApplicationContext(), "push_notification_type", "none").equals("mqtt")) {
            stopMqttService();
        }
        setConfigurationInfo("pushy");
        PushyServiceManager.start(getApplicationContext());
        Preferences.setPreference(getApplicationContext(), "push_notification_type", "pushy");
    }

    /**
     * Disable Pushy.me
     */
    public void disablePushyMe() {
        stopPushyMeService();
        setConfigurationInfo("none");
        Preferences.setPreference(getApplicationContext(), "push_notification_type", "none");
    }

    /**
     * Stop Pushy.me service.
     */
    public void stopPushyMeService() {
        PushyServiceManager.stop(getApplicationContext());
    }

    /**
     * Enable MQTT
     */
    public void enableMqtt() {
        if (Preferences.getPreference(getApplicationContext(), "push_notification_type", "none").equals("pushy")) {
            stopPushyMeService();
        }
        setConfigurationInfo("mqtt");
        Preferences.setPreference(getApplicationContext(), "push_notification_type", "mqtt");
    }

    /**
     * Disable MQTT
     */
    public void disableMqtt() {
        stopMqttService();
        setConfigurationInfo("none");
        Preferences.setPreference(getApplicationContext(), "push_notification_type", "none");
    }

    /**
     * Stop MQTT service.
     */
    public void stopMqttService() {
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
                        String message = getString(R.string.indigenous_unknown_error);
                        int code = error.networkResponse.statusCode;
                        switch (code) {
                            case 404:
                                message = getString(R.string.indigenous_account_not_found);
                                break;
                            case 400:
                                message = getString(R.string.indigenous_account_blocked);
                                break;
                        }

                        Snackbar.make(layout, String.format(getString(R.string.indigenous_account_error), message), Snackbar.LENGTH_LONG).show();
                    }
                }
        )
        {

            @Override
            protected Map<String, String> getParams() {
                User user = new Accounts(getApplicationContext()).getDefaultUser();
                Map<String, String> params = new HashMap<>();
                params.put("url", user.getBaseUrl());
                params.put("apiToken", siteApiToken.getText().toString());
                return params;
            }

        };

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        queue.add(getRequest);
    }

    /**
     * Register device at Pushy.me.
     */
    public void registerDeviceAtPushyMe() {
        new RegisterForPushNotificationsAsync().execute();
    }

    /**
     * Async registration class for Pushy.me
     */
    private class RegisterForPushNotificationsAsync extends AsyncTask<Void, Void, Exception> {
        protected Exception doInBackground(Void... params) {
            try {
                // Assign a unique token to this device.
                String deviceToken = Pushy.register(getApplicationContext());

                // Log it for debugging purposes
                if (deviceToken.length() > 0) {
                    storePushyMeTokenOnBackend(deviceToken);
                }

            }
            catch (Exception exc) {
                // Return exc to onPostExecute.
                return exc;
            }

            // Success
            return null;
        }

        @Override
        protected void onPostExecute(Exception exc) {
            // Failed?
            if (exc != null) {
                Snackbar.make(layout, exc.toString(), Snackbar.LENGTH_LONG).show();
            }
        }
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
                        Snackbar.make(layout, getString(R.string.indigenous_registration_success), Snackbar.LENGTH_SHORT).show();

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String message = "unknown error";
                        int code = error.networkResponse.statusCode;
                        switch (code) {
                            case 404:
                                message = getString(R.string.indigenous_account_not_found);
                                break;
                            case 400:
                                message = getString(R.string.indigenous_account_blocked);
                                break;
                        }
                        Snackbar.make(layout, String.format(getString(R.string.indigenous_device_store_error), message), Snackbar.LENGTH_LONG).show();
                    }
                }
        )
        {

            @Override
            protected Map<String, String> getParams() {
                User user = new Accounts(getApplicationContext()).getDefaultUser();
                Map<String, String> params = new HashMap<>();
                params.put("url", user.getBaseUrl());
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
