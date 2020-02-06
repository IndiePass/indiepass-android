package com.indieweb.indigenous.util;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.provider.Settings;
import android.util.Base64;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.indieweb.indigenous.BuildConfig;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.general.DebugActivity;
import com.indieweb.indigenous.model.Cache;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.MessageDigest;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

public class Utility {

    public static List<String> dateFormatStrings = Arrays.asList("yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ssZ", "yyyy-MM-dd HH:mmZ");

    /**
     * Check if we have an internet connection.
     *
     * @return boolean.
     */
    public static boolean hasConnection(Context context) {
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        assert cm != null;
        return (cm.getActiveNetworkInfo() != null) && cm.getActiveNetworkInfo().isAvailable() && cm.getActiveNetworkInfo().isConnected();
    }

    /**
     * Get cache item.
     *
     * @return Cache|null
     */
    public static Cache getCache(Context context, String account, String type, String channelId, String page) {
        Cache cache = null;

        if (Preferences.getPreference(context, "pref_key_reader_cache", false)) {
            DatabaseHelper db = new DatabaseHelper(context);
            cache = db.getCache(account, type, channelId, page);
        }

        return cache;
    }

    /**
     * Save cache.
     *
     * @param data
     *   The data to cache.
     */
    public static void saveCache(Context context, String account, String type, String data, String channel_id, String page) {
        if (Preferences.getPreference(context, "pref_key_reader_cache", false)) {
            Cache cache = getCache(context, account, type, channel_id, page);
            if (cache.getId() == 0) {
                cache.setAccount(account);
                cache.setType(type);
                cache.setChannelId(channel_id);
                cache.setPage(page);
            }

            cache.setData(data);
            DatabaseHelper db = new DatabaseHelper(context);
            db.saveCache(cache);
        }
    }

    /**
     * Open settings screen.
     */
    public static void openSettings(Context context) {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    /**
     * Show debug info activity.
     *
     * @param context
     *   The current context.
     * @param debug
     *   The debug string.
     */
    public static void showDebugInfo(Context context, String debug) {
        Intent i = new Intent(context, DebugActivity.class);
        Indigenous app = Indigenous.getInstance();
        app.setDebug(debug);
        context.startActivity(i);
    }

    /**
     * Copy to clipboard.
     *
     * @param copyText
     *   The text to copy to clipboard.
     * @param label
     *   The clipboard label
     * @param context
     *   The current context.
     */
    @SuppressWarnings("deprecation")
    public static void copyToClipboard(String copyText, String label, Context context) {
        int sdk = android.os.Build.VERSION.SDK_INT;
        if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
            android.text.ClipboardManager clipboard = (android.text.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                clipboard.setText(copyText);
            }
        }
        else {
            android.content.ClipboardManager clipboard = (android.content.ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            android.content.ClipData clip = android.content.ClipData.newPlainText(label, copyText);
            if (clipboard != null) {
                clipboard.setPrimaryClip(clip);
            }
        }
    }

    /**
     * Get a filename.
     *
     * @param u
     *   The uri
     * @param context
     *   The current context
     *
     * @return string
     */
    public static String getFilename(Uri u, Context context) {
        String filename = "";

        try {
            Cursor returnCursor = context.getContentResolver().query(u, null, null, null, null);
            if (returnCursor != null) {
                int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                returnCursor.moveToFirst();
                filename = returnCursor.getString(nameIndex);
                returnCursor.close();
            }
        }
        catch (NullPointerException ignored) {}

        return filename;
    }

    /**
     * Get filename extension.
     *
     * @param u
     *   The uri
     * @param context
     *   The current context
     * @param defaultExtension
     *   The default extension
     *
     * @return string
     */
    public static String getExtension(Uri u, Context context, String defaultExtension) {
        String extension;
        String filename = getFilename(u, context);
        if (filename.length() > 0) {
            if (filename.indexOf(".") > 0) {
                extension = filename.substring(filename.lastIndexOf(".") + 1);
            }
            else {
                extension = defaultExtension;
            }
        }
        else {
            extension = defaultExtension;
        }

        return extension;
    }

    /**
     * Trim a char sequence.
     *
     * @param text
     *   The text to trim.
     *
     * @return text
     */
    public static CharSequence trim(CharSequence text) {
        try {
            if (text.length() > 0) {
                while (text.charAt(text.length() - 1) == '\n') {
                    text = text.subSequence(0, text.length() - 1);
                }
            }
        }
        catch (Exception ignored) {}

        return text;
    }

    /**
     * Shows a DateTimePicker dialog.
     */
    public static void showDateTimePickerDialog(final Context context, final TextView t) {
        final Date[] value = {new Date()};
        final Calendar cal = Calendar.getInstance();
        cal.setTime(value[0]);
        new DatePickerDialog(context,
                new DatePickerDialog.OnDateSetListener() {
                    @Override public void onDateSet(DatePicker view, int y, int m, int d) {
                        cal.set(Calendar.YEAR, y);
                        cal.set(Calendar.MONTH, m);
                        cal.set(Calendar.DAY_OF_MONTH, d);

                        // now show the time picker
                        new TimePickerDialog(context,
                                new TimePickerDialog.OnTimeSetListener() {
                                    @Override public void onTimeSet(TimePicker view, int h, int min) {
                                        cal.set(Calendar.HOUR_OF_DAY, h);
                                        cal.set(Calendar.MINUTE, min);
                                        value[0] = cal.getTime();

                                        @SuppressLint("SimpleDateFormat")
                                        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00Z");
                                        String result;
                                        try {
                                            result = df.format(value[0]);
                                            t.setText(result);
                                        } catch (Exception ignored) { }

                                    }
                                }, cal.get(Calendar.HOUR_OF_DAY),
                                cal.get(Calendar.MINUTE), true).show();
                    }
                }, cal.get(Calendar.YEAR), cal.get(Calendar.MONTH),
                cal.get(Calendar.DAY_OF_MONTH)).show();
    }

    /**
     * Parse network error
     *
     * @param error
     *   The VolleyError
     * @param context
     *   The current context
     * @param network_fail
     *   The string in case of network fail.
     * @param fail
     *   The string in case of general fail.
     */
    public static String parseNetworkError(VolleyError error, Context context, int network_fail, int fail) {
        String returnMessage = context.getString(fail);
        try {
            NetworkResponse networkResponse = error.networkResponse;
            if (networkResponse != null && networkResponse.statusCode != 0 && networkResponse.data != null) {
                int code = networkResponse.statusCode;
                String result = new String(networkResponse.data).trim();
                returnMessage = String.format(context.getString(network_fail), code, result);
            }
        }
        catch (Exception ignored) { }

        return returnMessage;
    }

    /**
     * Make sure url is an absolute URL.
     *
     * @param url
     *   The url to test.
     * @param domain
     *   The domain to prefix the url with.
     *
     * @return string
     */
    public static String checkAbsoluteUrl(String url, String domain) {
        String returnUrl;

        if (!url.startsWith("http://") && !url.startsWith("https://")) {

            try {
                URL baseUrl = new URL(domain);
                URI uri = baseUrl.toURI();
                URI newUri = uri.resolve(domain + "/" + url);
                returnUrl = newUri.normalize().toURL().toString();
            }
            catch (MalformedURLException | URISyntaxException e) {
                // This shouldn't happen. We concatenate although it will still likely fail.
                returnUrl = domain + url;
            }

        }
        else {
            returnUrl = url;
        }

        return returnUrl;
    }

    /**
     * Generate sha256
     *
     * @param string
     *   The string to hash.
     *
     * @return base64 encoded string
     */
    public static String sha256(String string) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            md.update(string.getBytes());
            byte[] byteData = md.digest();
            String encoded = Base64.encodeToString(byteData, Base64.NO_WRAP);
            return encoded.trim().replace("=", "").replace("+", "-").replace("/", "_");
        }
        catch (Exception e) {
            return string;
        }
    }

}
