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
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.style.URLSpan;
import android.util.Base64;
import android.view.View;
import android.widget.DatePicker;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.browser.customtabs.CustomTabsIntent;
import androidx.core.content.ContextCompat;

import com.android.volley.NetworkResponse;
import com.android.volley.VolleyError;
import com.indieweb.indigenous.BuildConfig;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.general.DebugActivity;
import com.indieweb.indigenous.model.Cache;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.TimelineItem;
import com.indieweb.indigenous.reader.Reader;
import com.indieweb.indigenous.reader.TimelineActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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

    public static final List<String> dateFormatStrings = Arrays.asList("yyyy-MM-dd'T'HH:mm:ssZ", "yyyy-MM-dd'T'HH:mm:ss", "yyyy-MM-dd HH:mm:ssZ", "yyyy-MM-dd HH:mmZ");

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
     * Set the night theme.
     *
     * @param context
     *   The current context.
     */
    public static void setNightTheme(Context context) {
        if (Preferences.getPreference(context, "night_mode", false)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
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
     * Strip ending slash.
     *
     * @param string
     *   The string to strip the ending slash of.
     *
     * @return string
     */
    public static String stripEndingSlash(String string) {
        if (string.endsWith("/")) {
            string = string.substring(0, string.length() - 1);
        }
        return string;
    }

    /**
     * Strip start slash.
     *
     * @param string
     *   The string to strip the starting slash of.
     *
     * @return string
     */
    public static String stripStartSlash(String string) {
        if (string.startsWith("/")) {
            string = string.substring(1);
        }
        return string;
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
     * Notify channels the counter is changed.
     *
     * @param channelId
     *   The channel id.
     * @param count
     *   How many items to count up or down.
     * @param isSource
     *   whether to check on course.
     */
    public static void notifyChannels(String channelId, int count, boolean isSource) {
        try {
            Indigenous app = Indigenous.getInstance();
            app.setRefreshChannels(true);
            for (Channel c: app.getChannelsList()) {
                String uid;
                if (isSource) {
                    uid = c.getSourceId();
                }
                else {
                    uid = c.getUid();
                }
                if (uid.equals(channelId)) {
                    if (c.getUnread() != -1) {
                        c.setUnread(c.getUnread() + count);
                        break;
                    }
                }
            }
        }
        catch (Exception ignored) { }
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

    /**
     * Returns a string from either an array or string property.
     *
     * @param property
     *   The property on the object to get a value from.
     * @param object
     *   The json object.
     *
     * @return value
     */
    public static String getSingleJsonValueFromArrayOrString(String property, JSONObject object) {
        String value = "";

        try {
            Object temp = object.get(property);
            if (temp instanceof JSONArray) {
                value = object.getJSONArray(property).get(0).toString();
            }
            else {
                value = object.getString(property);
            }
        }
        catch (JSONException ignored) { }

        return value;
    }

    /**
     * Returns the reference content.
     *
     *  @param object
     *   A JSON object.
     * @param url
     *   The url to find in references
     * @param item
     *   The current timeline item.
     * @param swapAuthor
     *   Whether to swap the author or not.
     * @param checkRecursive
     *   Whether to check further recursive.
     * @param level
     *   The recursive level
     * @param context
     *   The current context.
     */
    public static void checkReference(JSONObject object, String url, TimelineItem item, boolean swapAuthor, boolean checkRecursive, int level, Context context) {

        if (object.has("refs")) {
            try {
                JSONObject references = object.getJSONObject("refs");
                if (references.has(url)) {
                    JSONObject ref = references.getJSONObject(url);

                    // Content.
                    if (ref.has("content")) {
                        JSONObject content = ref.getJSONObject("content");
                        if (content.has("text")) {
                            if (level == 1 && item.getReference().length() > 0) {
                                //Log.d("indigenous_debug", "swap on level 1: " + item.getReference());
                                item.setSwapReference(false);
                                item.setTextContent(item.getReference());
                            }
                            //Log.d("indigenous_debug", "content: " + content.getString("text"));
                            item.setReference(content.getString("text"));
                        }
                    }
                    else if (ref.has("summary")) {
                        if (level == 1 && item.getReference().length() > 0) {
                            //Log.d("indigenous_debug", "swap on level 1: " + item.getReference());
                            item.setSwapReference(false);
                            item.setTextContent(item.getReference());
                        }
                        item.setReference(ref.getString("summary"));
                    }

                    // Photo.
                    if (ref.has("photo")) {
                        JSONArray photos = ref.getJSONArray("photo");
                        for (int p = 0; p < photos.length(); p++) {
                            item.addPhoto(photos.getString(p));
                        }
                    }

                    // Video.
                    if (ref.has("video")) {
                        String video = ref.getJSONArray("video").getString(0);
                        item.setVideo(video);
                    }

                    // Swap actor and author.
                    if (swapAuthor && Preferences.getPreference(context, "pref_key_timeline_author_original", false) && ref.has("author")) {
                        String authorName = "";
                        JSONObject author = ref.getJSONObject("author");
                        if (author.has("name")) {
                            authorName = author.getString("name");
                        }
                        String authorUrl = "";
                        if (author.has("url")) {
                            authorUrl = author.getString("url");
                            item.setAuthorUrl(authorUrl);
                        }
                        if (authorName.equals("null") && authorUrl.length() > 0) {
                            authorName = authorUrl;
                        }

                        if (author.has("photo")) {
                            String authorPhoto = author.getString("photo");
                            if (!authorPhoto.equals("null") && authorPhoto.length() > 0) {
                                item.setAuthorPhoto(authorPhoto);
                            }
                        }

                        if (authorName.length() > 0) {
                            item.setActor(item.getAuthorName());
                            item.setAuthorName(authorName);
                        }
                    }

                    if (checkRecursive && ref.has("quotation-of")) {
                        //Log.d("indigenous_debug", "going recursive");
                        String secondType = "quotation-of";
                        String value = getSingleJsonValueFromArrayOrString(secondType, ref);
                        if (value.length() > 0) {
                            checkReference(ref, value, item, false, false, 1, context);
                        }
                    }
                }
            }
            catch (JSONException ignored) { }
        }
    }

    /**
     * Link clickable.
     *
     * @param strBuilder
     *   A string builder.
     * @param span
     *   The span with url.
     * @param context
     *   The current context
     * @param reader
     *   The current reader
     * @param item
     *   The current timeline item.
     */
    public static void makeLinkClickable(SpannableStringBuilder strBuilder, final URLSpan span, final Context context, Reader reader, final TimelineItem item) {
        int start = strBuilder.getSpanStart(span);
        int end = strBuilder.getSpanEnd(span);
        int flags = strBuilder.getSpanFlags(span);

        String tag = "";
        if (reader != null) {
            tag = reader.getTag(span.getURL(), item);
        }

        final String finalTag = tag;
        ClickableSpan clickable = new ClickableSpan() {
            public void onClick(@NonNull View view) {
                if (finalTag != null && finalTag.length() > 0) {
                    Intent intent = new Intent(context, TimelineActivity.class);
                    intent.putExtra("channelId", item.getChannelId());
                    intent.putExtra("channelName", item.getChannelName());
                    intent.putExtra("tag", finalTag);
                    context.startActivity(intent);
                }
                else {
                    try {
                        CustomTabsIntent.Builder intentBuilder = new CustomTabsIntent.Builder();
                        intentBuilder.setToolbarColor(ContextCompat.getColor(context, R.color.colorPrimary));
                        intentBuilder.setSecondaryToolbarColor(ContextCompat.getColor(context, R.color.colorPrimaryDark));
                        CustomTabsIntent customTabsIntent = intentBuilder.build();
                        customTabsIntent.intent.setFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                        customTabsIntent.intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        customTabsIntent.launchUrl(context, Uri.parse(span.getURL()));
                    }
                    catch (Exception ignored) { }
                }

            }
        };
        strBuilder.setSpan(clickable, start, end, flags);
        strBuilder.removeSpan(span);
    }

}
