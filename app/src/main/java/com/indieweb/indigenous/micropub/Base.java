package com.indieweb.indigenous.micropub;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.micropub.post.GalleryAdapter;
import com.indieweb.indigenous.micropub.post.SendPostInterface;
import com.indieweb.indigenous.model.Place;
import com.indieweb.indigenous.model.Syndication;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;
import com.indieweb.indigenous.util.VolleyMultipartRequest;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import static java.lang.Integer.parseInt;

@SuppressLint("Registered")
abstract public class Base extends AppCompatActivity implements SendPostInterface, TextWatcher {

    public boolean isTesting = false;
    boolean hasChanges = false;
    public MultiAutoCompleteTextView body;
    public EditText title;
    public Switch saveAsDraft;
    public DatabaseHelper db;
    public User user;
    public MultiAutoCompleteTextView tags;
    public List<Uri> images = new ArrayList<>();
    public List<String> captions = new ArrayList<>();
    public boolean preparedDraft = false;
    public List<Syndication> syndicationTargets = new ArrayList<>();
    private MenuItem sendItem;
    public LinearLayout imagePreviewGallery;
    private Switch postStatus;
    private int PICK_IMAGE_REQUEST = 1;

    public EditText url;
    public Spinner rsvp;
    public TextView publishDate;
    public String urlPostKey;
    public String autoSubmit = "";
    public String hType = "entry";
    public String postType = "Post";
    public Spinner geocacheLogType;
    public TextView startDate;
    public TextView endDate;
    public boolean finishActivity = true;
    public boolean canAddImage = false;
    public boolean canAddLocation = false;
    public boolean addCounter = false;
    public Map<String, String> bodyParams = new LinkedHashMap<>();

    public Integer draftId;
    String fileUrl;
    public TextView mediaUrl;
    public boolean isMediaRequest = false;
    public boolean isCheckin = false;

    public LinearLayout locationWrapper;
    public Spinner locationVisibility;
    public AutoCompleteTextView locationName;
    public EditText locationUrl;
    public List<Place> placeItems = new ArrayList<>();
    public TextView locationCoordinates;
    public String coordinates;
    public Button locationQuery;
    public Double latitude = null;
    public Double longitude = null;
    public RelativeLayout progressBar;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_create_menu, menu);

        if (!canAddImage) {
            MenuItem item = menu.findItem(R.id.addImage);
            item.setVisible(false);
        }

        if (!canAddLocation) {
            MenuItem item = menu.findItem(R.id.addLocation);
            item.setVisible(false);
        }

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {

            case android.R.id.home:
                confirmClose(true);
                return true;

            case R.id.addImage:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_OPEN_DOCUMENT);
                if (!isMediaRequest) {
                    intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                startActivityForResult(Intent.createChooser(intent, getString(R.string.select_picture)), PICK_IMAGE_REQUEST);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {

            if (isMediaRequest) {
                images.clear();
                captions.clear();
            }

            if (data.getClipData() != null) {
                int count = data.getClipData().getItemCount();
                if (count > 0) {
                    setChanges(true);
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

                    for (int i = 0; i < count; i++) {
                        try {
                            getContentResolver().takePersistableUriPermission(data.getClipData().getItemAt(i).getUri(), takeFlags);
                        }
                        catch (Exception ignored) {}
                        images.add(data.getClipData().getItemAt(i).getUri());
                        captions.add("");
                    }
                }
            }
            else if (data.getData() != null) {
                images.add(data.getData());
                captions.add("");

                try {
                    setChanges(true);
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                }
                catch (Exception ignored) {}

            }

            prepareImagePreview();
        }
    }

    @Override
    public void onBackPressed() {
        confirmClose(false);
    }

    /**
     * Confirm closing post form.
     *
     * @param topBack
     *   Whether the top back was used or not.
     */
    public void confirmClose(final boolean topBack) {
        if (hasChanges) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.confirm_close);
            builder.setPositiveButton(getApplicationContext().getString(R.string.discard_post),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    setChanges(false);

                    // Top back button.
                    if (topBack) {
                        finish();
                    }
                    else {
                        onBackPressed();
                    }
                }
            });
            builder.setNegativeButton(getApplicationContext().getString(R.string.keep_editing), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
        else {

            // Top back button.
            if (topBack) {
                finish();
            }
            else {
                super.onBackPressed();
            }
        }

    }

    /**
     * Prepare image preview.
     */
    public void prepareImagePreview() {
        imagePreviewGallery.setVisibility(View.VISIBLE);
        GalleryAdapter galleryAdapter = new GalleryAdapter(this, images, captions, isMediaRequest);
        RecyclerView imageRecyclerView = findViewById(R.id.imageRecyclerView);
        imageRecyclerView.setAdapter(galleryAdapter);
    }

    /**
     * Convert bitmap to byte[] array.
     */
    public byte[] getFileDataFromDrawable(Bitmap bitmap, Boolean scale, Uri uri, String mime) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if (scale) {
            int ImageQuality = 80;
            // Default quality. The preference is stored as a string, but cast it to an integer.
            String qualityPreference = Preferences.getPreference(this, "pref_key_image_quality", Integer.toString(ImageQuality));
            if (parseInt(qualityPreference) <= 100 && parseInt(qualityPreference) > 0) {
                ImageQuality = parseInt(qualityPreference);
            }

            switch (mime) {
                case "image/png":
                    bitmap.compress(Bitmap.CompressFormat.PNG, ImageQuality, byteArrayOutputStream);
                    break;
                case "image/jpg":
                default:
                    bitmap.compress(Bitmap.CompressFormat.JPEG, ImageQuality, byteArrayOutputStream);
                    break;
            }
        }
        else {
            ContentResolver cR = this.getContentResolver();
            try {
                InputStream is = cR.openInputStream(uri);
                final byte[] b = new byte[8192];
                for (int r; (r = is.read(b)) != -1;) {
                    byteArrayOutputStream.write(b, 0, r);
                }
            }
            catch (Exception ignored) { }
        }

        return byteArrayOutputStream.toByteArray();
    }

    /**
     * Send post.
     */
    public void sendBasePost(MenuItem item) {
        sendItem = item;

        if (!new Connection(this).hasConnection()) {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        if (sendItem != null) {
            sendItem.setEnabled(false);
        }

        showProgressBar();

        String endpoint = user.getMicropubEndpoint();
        if (isMediaRequest) {
            endpoint = user.getMicropubMediaEndpoint();
        }

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, endpoint,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                        if (finishActivity) {
                            Toast.makeText(getApplicationContext(), getString(R.string.post_success), Toast.LENGTH_SHORT).show();

                            // Remove draft if needed.
                            if (draftId != null && draftId > 0) {
                                db = new DatabaseHelper(getApplicationContext());
                                db.deleteDraft(draftId);
                            }

                            hideProgressBar();
                            finish();
                        }

                        if (isMediaRequest) {
                            fileUrl = response.headers.get("Location");
                            if (fileUrl != null && fileUrl.length() > 0) {
                                Toast.makeText(getApplicationContext(), R.string.media_upload_success, Toast.LENGTH_SHORT).show();
                                mediaUrl.setText(fileUrl);
                                mediaUrl.setVisibility(View.VISIBLE);
                                Utility.copyToClipboard(fileUrl, getString(R.string.clipboard_media_url), getApplicationContext());
                            }
                            else {
                                Toast.makeText(getApplicationContext(), R.string.no_media_url_found, Toast.LENGTH_SHORT).show();
                            }

                            if (sendItem != null) {
                                sendItem.setEnabled(true);
                            }

                            hideProgressBar();
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
                                Toast.makeText(getApplicationContext(), String.format(getString(R.string.post_network_fail), postType, code, result), Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), String.format(getString(R.string.post_fail), error.getMessage()), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(getApplicationContext(), String.format(getString(R.string.post_fail), error.getMessage()), Toast.LENGTH_LONG).show();
                        }

                        if (sendItem != null) {
                            sendItem.setEnabled(true);
                        }

                        hideProgressBar();
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {

                // Send along access token if configured.
                if (Preferences.getPreference(getApplicationContext(), "pref_key_access_token_body", false)) {
                    bodyParams.put("access_token", user.getAccessToken());
                }

                // h type.
                if (!isMediaRequest) {
                    bodyParams.put("h", hType);
                }

                // Title
                if (title != null && !TextUtils.isEmpty(title.getText())) {
                  bodyParams.put("name", title.getText().toString());
                }

                // Content
                if (body != null) {
                    bodyParams.put("content", body.getText().toString());
                }

                // url
                if (url != null && urlPostKey.length() > 0) {
                    bodyParams.put(urlPostKey, url.getText().toString());
                }

                // Tags
                if (tags != null) {
                    List<String> tagsList = new ArrayList<>(Arrays.asList(tags.getText().toString().split(",")));
                    int i = 0;
                    for (String tag: tagsList) {
                        tag = tag.trim();
                        if (tag.length() > 0) {
                            bodyParams.put("category_multiple_["+ i +"]", tag);
                            i++;
                        }
                    }
                }

                // Publish date.
                if (publishDate != null && !TextUtils.isEmpty(publishDate.getText())) {
                    bodyParams.put("published", publishDate.getText().toString());
                }
                else {
                    Date date = Calendar.getInstance().getTime();
                    @SuppressLint("SimpleDateFormat")
                    DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:00Z");
                    df.setTimeZone(TimeZone.getDefault());
                    bodyParams.put("published", df.format(date));
                }

                // Post status.
                postStatus = findViewById(R.id.postStatus);
                if (postStatus != null) {
                    String postStatusValue = "draft";
                    if (postStatus.isChecked()) {
                        postStatusValue = "published";
                    }
                    bodyParams.put("post-status", postStatusValue);
                }

                // Syndication targets.
                if (syndicationTargets.size() > 0) {
                    CheckBox checkbox;
                    for (int j = 0, k = 0; j < syndicationTargets.size(); j++) {
                        checkbox = findViewById(j);
                        if (checkbox != null && checkbox.isChecked()) {
                            bodyParams.put("mp-syndicate-to_multiple_[" + k + "]", syndicationTargets.get(j).getUid());
                            k++;
                        }
                    }
                }

                // Image alt.
                if (captions.size() > 0) {
                    int ia = 0;
                    for (String s: captions) {
                        String caption = "";
                        if (s.length() > 0) {
                            caption = s;
                        }
                        bodyParams.put("mp-photo-alt_multiple_[" + ia + "]", caption);
                        ia++;
                    }
                }

                // Location.
                if (canAddLocation && coordinates != null && coordinates.length() > 0) {
                    String payloadProperty = "location";
                    String geo = coordinates;

                    // Send along location label.
                    if (!TextUtils.isEmpty(locationName.getText())) {
                        geo += ";name=" + locationName.getText().toString();
                    }

                    // Checkin.
                    if (isCheckin) {
                        payloadProperty = "checkin";
                        if (!TextUtils.isEmpty(locationUrl.getText())) {
                            geo += ";url=" + locationUrl.getText().toString();
                        }
                    }

                    bodyParams.put(payloadProperty, "geo:" + geo);

                    if (locationVisibility != null && locationVisibility.getVisibility() == View.VISIBLE) {
                        bodyParams.put("location-visibility", locationVisibility.getSelectedItem().toString());
                    }
                }

                return bodyParams;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");

                // Send access token in header by default.
                if (!Preferences.getPreference(getApplicationContext(), "pref_key_access_token_body", false)) {
                    headers.put("Authorization", "Bearer " + user.getAccessToken());
                }

                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new LinkedHashMap<>();

                if (images.size() > 0) {

                    int ImageSize = 1000;
                    Boolean scale = Preferences.getPreference(getApplicationContext(), "pref_key_image_scale", true);
                    if (scale) {
                        String sizePreference = Preferences.getPreference(getApplicationContext(), "pref_key_image_size", Integer.toString(ImageSize));
                        if (parseInt(sizePreference) > 0) {
                            ImageSize = parseInt(sizePreference);
                        }
                    }

                    ContentResolver cR = getApplicationContext().getContentResolver();

                    int i = 0;
                    for (Uri u : images) {

                        Bitmap bitmap = null;
                        if (scale) {
                            try {
                                bitmap = Glide
                                        .with(getApplicationContext())
                                        .asBitmap()
                                        .load(u)
                                        .apply(new RequestOptions().override(ImageSize, ImageSize))
                                        .submit()
                                        .get();
                            }
                            catch (Exception ignored) {}

                            if (bitmap == null) {
                                continue;
                            }
                        }

                        long imagename = System.currentTimeMillis();
                        String mime = cR.getType(u);

                        String extension = "jpg";
                        if (mime != null) {
                            if (mime.equals("image/png")) {
                                extension = "png";
                            }
                        }

                        String imagePostParam = "photo_multiple_[" + i + "]";
                        if (isMediaRequest) {
                            imagePostParam = "file";
                        }
                        i++;

                        // Put image in body. Send along whether to scale or not.
                        params.put(imagePostParam, new DataPart(imagename + "." + extension, getFileDataFromDrawable(bitmap, scale, u, mime)));
                    }
                }

                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    /**
     * Sets the incoming text as URL and puts focus on either title, or body field.
     *
     * @param incomingUrl
     *   The incoming URL.
     */
    public void setUrlAndFocusOnMessage(String incomingUrl) {
        if (isCheckin) {
            locationUrl.setText(incomingUrl);
        }
        else {
            url.setText(incomingUrl);
            if (title != null) {
                title.requestFocus();
            }
            else if (body != null) {
                body.requestFocus();
            }
        }
    }

    /**
     * Show progress bar.
     */
    public void showProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Hide progress bar.
     */
    public void hideProgressBar() {
        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }
    }

    /**
     * Set changes property
     *
     * @param changes
     *   Whether hasChanges is true or false.
     */
    public void setChanges(boolean changes) {
        hasChanges = changes;
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) {
        setChanges(true);
    }

    // Publish date onclick listener.
    public class publishDateOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Utility.showDateTimePickerDialog(Base.this, publishDate);
        }
    }

}