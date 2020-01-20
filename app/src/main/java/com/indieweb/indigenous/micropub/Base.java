package com.indieweb.indigenous.micropub;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
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

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.micropub.post.AudioGalleryAdapter;
import com.indieweb.indigenous.micropub.post.ImageGalleryAdapter;
import com.indieweb.indigenous.micropub.post.SendPostInterface;
import com.indieweb.indigenous.micropub.post.VideoGalleryAdapter;
import com.indieweb.indigenous.model.Place;
import com.indieweb.indigenous.model.Syndication;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;
import com.indieweb.indigenous.util.VolleyRequestListener;
import com.indieweb.indigenous.util.VolleyMultipartRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

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
abstract public class Base extends AppCompatActivity implements SendPostInterface, TextWatcher, VolleyRequestListener {

    public boolean isTesting = false;
    boolean hasChanges = false;
    public MultiAutoCompleteTextView body;
    public EditText title;
    public Switch saveAsDraft;
    public DatabaseHelper db;
    public User user;
    public MultiAutoCompleteTextView tags;
    public List<Uri> image = new ArrayList<>();
    public List<Uri> video = new ArrayList<>();
    public List<Uri> audio = new ArrayList<>();
    public int mediaCount = 0;
    public int mediaUploadedCount = 0;
    public Map<Uri, String> mediaUrls = new HashMap<>();
    public boolean uploadMediaDone = false;
    public boolean uploadMediaError = false;
    public List<String> caption = new ArrayList<>();
    public static String EMPTY_CAPTION = "_EMPTY_";
    public boolean preparedDraft = false;
    public List<Syndication> syndicationTargets = new ArrayList<>();
    private MenuItem sendItem;
    public LinearLayout mediaPreviewGallery;
    private Switch postStatus;
    private int PICK_IMAGE_REQUEST = 1;
    private int PICK_VIDEO_REQUEST = 2;
    private int PICK_AUDIO_REQUEST = 3;
    private VolleyRequestListener volleyRequestListener;

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
    public boolean canAddMedia = false;
    public boolean canAddLocation = false;
    public boolean addCounter = false;
    public Map<String, String> bodyParams = new LinkedHashMap<>();
    public RelativeLayout layout;

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
    public Location mCurrentLocation;

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_create_menu, menu);

        if (!canAddMedia) {
            MenuItem item = menu.findItem(R.id.addFile);
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
                Intent ii = new Intent();
                ii.setType("image/*");
                ii.setAction(Intent.ACTION_OPEN_DOCUMENT);
                if (!isMediaRequest) {
                    ii.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                startActivityForResult(Intent.createChooser(ii, getString(R.string.select_picture)), PICK_IMAGE_REQUEST);
                return true;

            case R.id.addVideo:
                Intent iv = new Intent();
                iv.setType("video/*");
                iv.setAction(Intent.ACTION_OPEN_DOCUMENT);
                if (!isMediaRequest) {
                    iv.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                startActivityForResult(Intent.createChooser(iv, getString(R.string.select_video)), PICK_VIDEO_REQUEST);
                return true;

            case R.id.addAudio:
                Intent ia = new Intent();
                ia.setType("audio/*");
                ia.setAction(Intent.ACTION_OPEN_DOCUMENT);
                if (!isMediaRequest) {
                    ia.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                }
                startActivityForResult(Intent.createChooser(ia, getString(R.string.select_audio)), PICK_AUDIO_REQUEST);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if ((requestCode == PICK_IMAGE_REQUEST || requestCode == PICK_VIDEO_REQUEST || requestCode == PICK_AUDIO_REQUEST) && resultCode == RESULT_OK) {

            if (isMediaRequest) {
                if (requestCode == PICK_IMAGE_REQUEST) {
                    caption.clear();
                    image.clear();
                    hideMediaPreview(false, true, true);
                }

                if (requestCode == PICK_VIDEO_REQUEST) {
                    video.clear();
                    hideMediaPreview(true, false, true);
                }

                if (requestCode == PICK_AUDIO_REQUEST) {
                    audio.clear();
                    hideMediaPreview(true, true, false);
                }
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

                        if (requestCode == PICK_IMAGE_REQUEST) {
                            image.add(data.getClipData().getItemAt(i).getUri());
                            caption.add("");
                        }

                        if (requestCode == PICK_VIDEO_REQUEST) {
                            video.add(data.getClipData().getItemAt(i).getUri());
                        }

                        if (requestCode == PICK_AUDIO_REQUEST) {
                            audio.add(data.getClipData().getItemAt(i).getUri());
                        }
                    }
                }
            }
            else if (data.getData() != null) {

                if (requestCode == PICK_IMAGE_REQUEST) {
                    image.add(data.getData());
                    caption.add("");
                }

                if (requestCode == PICK_VIDEO_REQUEST) {
                    video.add(data.getData());
                }

                if (requestCode == PICK_AUDIO_REQUEST) {
                    audio.add(data.getData());
                }

                try {
                    setChanges(true);
                    final int takeFlags = data.getFlags()
                            & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                            | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    getContentResolver().takePersistableUriPermission(data.getData(), takeFlags);
                }
                catch (Exception ignored) {}

            }

            if (requestCode == PICK_IMAGE_REQUEST) {
                prepareImagePreview();
            }

            if (requestCode == PICK_VIDEO_REQUEST) {
                prepareVideoPreview();
            }

            if (requestCode == PICK_AUDIO_REQUEST) {
                prepareAudioPreview();
            }

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
        mediaPreviewGallery.setVisibility(View.VISIBLE);
        ImageGalleryAdapter galleryAdapter = new ImageGalleryAdapter(this, image, caption, isMediaRequest);
        RecyclerView imageRecyclerView = findViewById(R.id.imageRecyclerView);
        imageRecyclerView.setVisibility(View.VISIBLE);
        imageRecyclerView.setAdapter(galleryAdapter);
    }

    /**
     * Prepare video preview.
     */
    public void prepareVideoPreview() {
        mediaPreviewGallery.setVisibility(View.VISIBLE);
        VideoGalleryAdapter galleryAdapter = new VideoGalleryAdapter(this, video, isMediaRequest);
        RecyclerView videoRecyclerView = findViewById(R.id.videoRecyclerView);
        videoRecyclerView.setVisibility(View.VISIBLE);
        videoRecyclerView.setAdapter(galleryAdapter);
    }

    /**
     * Prepare audio preview.
     */
    public void prepareAudioPreview() {
        mediaPreviewGallery.setVisibility(View.VISIBLE);
        AudioGalleryAdapter galleryAdapter = new AudioGalleryAdapter(this, audio, isMediaRequest);
        RecyclerView audioRecyclerView = findViewById(R.id.audioRecyclerView);
        audioRecyclerView.setVisibility(View.VISIBLE);
        audioRecyclerView.setAdapter(galleryAdapter);
    }

    /**
     * Hide other preview elements.
     *
     * @param hideImage
     *   Whether to hide image or not.
     * @param hideVideo
     *   Whether to hide video or not.
     * @param hideAudio
     *   Whether to hide audio or not.
     */
    public void hideMediaPreview(boolean hideImage, boolean hideVideo, boolean hideAudio) {
        if (hideImage) {
            RecyclerView imageRecyclerView = findViewById(R.id.imageRecyclerView);
            imageRecyclerView.setVisibility(View.GONE);
        }

        if (hideVideo) {
            RecyclerView videoRecyclerView = findViewById(R.id.videoRecyclerView);
            videoRecyclerView.setVisibility(View.GONE);
        }

        if (hideAudio) {
            RecyclerView audioRecyclerView = findViewById(R.id.audioRecyclerView);
            audioRecyclerView.setVisibility(View.GONE);
        }
    }

    /**
     * Get file data from uri or bitmap.
     */
    public byte[] getFileData(Bitmap bitmap, Boolean scale, Uri uri, String mime) {

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
     *
     * This is used for all posts and the single media endpoint activity. In case the media endpoint
     * must be used for attached media on a post, sendMediaPost() will be called as well to first
     * upload all media before coming back to this one.
     */
    public void sendBasePost(MenuItem item) {
        sendItem = item;

        if (!Utility.hasConnection(getApplicationContext())) {
            Snackbar snackbar = Snackbar.make(layout, getString(R.string.no_connection), Snackbar.LENGTH_LONG);

            if (saveAsDraft != null) {
                 snackbar.setAction(getString(R.string.save_as_draft), new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                saveAsDraft.setChecked(true);
                                try {
                                    findViewById(R.id.send).callOnClick();
                                }
                                catch (Exception ignored) {
                                    Snackbar.make(layout, getString(R.string.draft_checked), Snackbar.LENGTH_SHORT).show();
                                }
                            }
                        }
                );
            }

            snackbar.show();
            return;
        }

        showProgressBar();

        // Use media endpoint to upload media attached to the post.
        if (Preferences.getPreference(getApplicationContext(), "pref_key_upload_media_endpoint", false) && image.size() > 0 && !uploadMediaDone) {
            mediaCount = image.size() + video.size() + audio.size();
            sendMediaPost();
            return;
        }

        // Get the endpoint. The single media endpoint also uses this method.
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

                            Intent returnIntent = new Intent();
                            setResult(RESULT_OK, returnIntent);

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
                                Snackbar.make(layout, getString(R.string.media_upload_success), Snackbar.LENGTH_SHORT).show();
                                mediaUrl.setText(fileUrl);
                                mediaUrl.setVisibility(View.VISIBLE);
                                Utility.copyToClipboard(fileUrl, getString(R.string.clipboard_media_url), getApplicationContext());
                            }
                            else {
                                Snackbar.make(layout, getString(R.string.no_media_url_found), Snackbar.LENGTH_SHORT).show();
                            }

                            hideProgressBar();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        hideProgressBar();
                        String message = Utility.parseNetworkError(error, getApplicationContext(), R.string.post_network_fail, R.string.post_fail);
                        Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();

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
                if (caption.size() > 0) {
                    int ia = 0;
                    for (String s: caption) {
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

                // Media urls.
                if (Preferences.getPreference(getApplicationContext(), "pref_key_upload_media_endpoint", false) && uploadMediaDone && mediaUrls.size() > 0) {
                    int mi = 0;
                    for (Uri u: image) {
                        bodyParams.put("photo_multiple_[" + mi + "]", mediaUrls.get(u));
                        mi++;
                    }

                    int mv = 0;
                    for (Uri u: video) {
                        bodyParams.put("video_multiple_[" + mv + "]", mediaUrls.get(u));
                        mv++;
                    }

                    int ma = 0;
                    for (Uri u: audio) {
                        bodyParams.put("audio_multiple_[" + ma + "]", mediaUrls.get(u));
                        ma++;
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

                // Images.
                if (image.size() > 0 && !Preferences.getPreference(getApplicationContext(), "pref_key_upload_media_endpoint", false)) {

                    int ImageSize = 1000;
                    Boolean scale = Preferences.getPreference(getApplicationContext(), "pref_key_image_scale", true);
                    if (scale) {
                        String sizePreference = Preferences.getPreference(getApplicationContext(), "pref_key_image_size", Integer.toString(ImageSize));
                        if (parseInt(sizePreference) > 0) {
                            ImageSize = parseInt(sizePreference);
                        }
                    }

                    ContentResolver cR = getApplicationContext().getContentResolver();

                    int im = 0;
                    for (Uri u : image) {

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

                        String imagePostParam = "photo_multiple_[" + im + "]";
                        if (isMediaRequest) {
                            imagePostParam = "file";
                        }
                        im++;

                        // Put image in body. Send along whether to scale or not.
                        params.put(imagePostParam, new DataPart(imagename + "." + extension, getFileData(bitmap, scale, u, mime)));
                    }
                }

                // Videos
                if (video.size() > 0 && !Preferences.getPreference(getApplicationContext(), "pref_key_upload_media_endpoint", false)) {

                    int iv = 0;
                    for (Uri v : video) {

                        long videoname = System.currentTimeMillis();
                        String extension = Utility.getExtension(v, getApplicationContext(), "mp4");

                        String videoPostParam = "video_multiple_[" + iv + "]";
                        if (isMediaRequest) {
                            videoPostParam = "file";
                        }

                        params.put(videoPostParam, new DataPart(videoname + "." + extension, getFileData(null, false, v, null)));

                        iv++;
                    }
                }

                // Audio
                if (audio.size() > 0 && !Preferences.getPreference(getApplicationContext(), "pref_key_upload_media_endpoint", false)) {

                    int ia = 0;
                    for (Uri a : audio) {

                        long audioname = System.currentTimeMillis();
                        String extension = Utility.getExtension(a, getApplicationContext(), "mp3");

                        String audioPostParam = "audio_multiple_[" + ia + "]";
                        if (isMediaRequest) {
                            audioPostParam = "file";
                        }

                        params.put(audioPostParam, new DataPart(audioname + "." + extension, getFileData(null, false, a, null)));

                        ia++;
                    }
                }

                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    /**
     * Send media post.
     */
    public void sendMediaPost() {

        mediaUrls.clear();
        mediaUploadedCount = 0;
        uploadMediaDone = false;
        uploadMediaError = false;

        String endpoint = user.getMicropubMediaEndpoint();
        if (endpoint.length() == 0) {
            Snackbar.make(layout, getString(R.string.no_micropub_media_endpoint), Snackbar.LENGTH_LONG).show();
            hideProgressBar();
            return;
        }

        // Set listener.
        VolleyRequestListener(this);

        for (Uri u : image) {
            sendMediaRequest(u, endpoint, true, false);
        }

        for (Uri u : video) {
            sendMediaRequest(u, endpoint, false, true);
        }

        for (Uri u : audio) {
            sendMediaRequest(u, endpoint, false, false);
        }

    }

    /**
     * Send media request.
     *
     * @param u
     *   The media uri
     * @param endpoint
     *   The media endpoint
     * @param image
     *   Whether this is an image or not
     * @param video
     *   Whether this is a video or not
     */
    public void sendMediaRequest(final Uri u, String endpoint, final boolean image, final boolean video) {
        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, endpoint,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {

                        String fileUrl = response.headers.get("Location");
                        if (fileUrl != null && fileUrl.length() > 0) {
                            mediaUrls.put(u, fileUrl);
                            mediaUploadedCount++;
                            volleyRequestListener.OnSuccessRequest(null);
                        }
                        else {
                            uploadMediaError = true;
                            volleyRequestListener.OnFailureRequest(null);
                            Snackbar.make(layout, getString(R.string.no_media_url_found), Snackbar.LENGTH_SHORT).show();
                        }

                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        uploadMediaError = true;
                        volleyRequestListener.OnFailureRequest(error);
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

                if (image) {
                    int ImageSize = 1000;
                    Boolean scale = Preferences.getPreference(getApplicationContext(), "pref_key_image_scale", true);
                    if (scale) {
                        String sizePreference = Preferences.getPreference(getApplicationContext(), "pref_key_image_size", Integer.toString(ImageSize));
                        if (parseInt(sizePreference) > 0) {
                            ImageSize = parseInt(sizePreference);
                        }
                    }

                    ContentResolver cR = getApplicationContext().getContentResolver();

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
                            uploadMediaError = true;
                            Snackbar.make(layout, getString(R.string.bitmap_error), Snackbar.LENGTH_SHORT).show();
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

                    // Put image in body. Send along whether to scale or not.
                    params.put("file", new DataPart(imagename + "." + extension, getFileData(bitmap, scale, u, mime)));
                }
                else {
                    long filename = System.currentTimeMillis();

                    String extension = Utility.getExtension(u, getApplicationContext(), "mp4");
                    if (!video) {
                        extension = Utility.getExtension(u, getApplicationContext(), "mp3");
                    }

                    params.put("file", new DataPart(filename + "." + extension, getFileData(null, false, u, null)));
                }

                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(0, -1, DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    /**
     * Set listener.
     *
     * @param volleyRequestListener
     *   The volley request listener.
     */
    public void VolleyRequestListener(VolleyRequestListener volleyRequestListener) {
        this.volleyRequestListener = volleyRequestListener;
    }


    @Override
    public void OnSuccessRequest(String response) {

        // In case everything is fine, send base post.
        if (mediaUploadedCount == mediaCount && !uploadMediaError) {
            uploadMediaDone = true;
            sendBasePost(sendItem);
        }

        // There's a possibility no url was found, we need to stop then as well.
        if (uploadMediaError) {
            hideProgressBar();
        }

    }

    @Override
    public void OnFailureRequest(VolleyError error) {
        hideProgressBar();
        String message = Utility.parseNetworkError(error, getApplicationContext(), R.string.media_network_fail, R.string.media_fail);
        Snackbar.make(layout, message, Snackbar.LENGTH_SHORT).show();
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
            if (url != null) {
                url.setText(incomingUrl);
            }

            if (title != null) {
                title.requestFocus();
            }
            else if (body != null) {
                body.requestFocus();
            }
        }
    }

    /**
     * Show progress bar and disable send menu item.
     */
    public void showProgressBar() {
        if (progressBar != null && (progressBar.getVisibility() != View.VISIBLE)) {
            progressBar.setVisibility(View.VISIBLE);
        }

        if (sendItem != null) {
            sendItem.setEnabled(false);
        }
    }

    /**
     * Hide progress bar and enable send menu item.
     */
    public void hideProgressBar() {

        if (progressBar != null) {
            progressBar.setVisibility(View.GONE);
        }

        if (sendItem != null) {
            sendItem.setEnabled(true);
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

    /**
     * Toggle location visibilities.
     */
    public void toggleLocationVisibilities(Boolean toggleWrapper) {

        if (toggleWrapper) {
            locationWrapper.setVisibility(View.VISIBLE);
            locationCoordinates.setVisibility(View.VISIBLE);
        }

        boolean showLocationVisibility = Preferences.getPreference(getApplicationContext(), "pref_key_location_visibility", false);
        boolean showLocationName = Preferences.getPreference(getApplicationContext(), "pref_key_location_label", false);
        boolean showLocationQueryButton = Preferences.getPreference(getApplicationContext(), "pref_key_location_label_query", false);
        if (isCheckin || showLocationVisibility || showLocationName || showLocationQueryButton) {
            if (showLocationName || isCheckin) {
                locationName.setVisibility(View.VISIBLE);
            }
            if (showLocationVisibility) {
                locationVisibility.setVisibility(View.VISIBLE);
            }
            if (showLocationQueryButton) {
                locationQuery.setVisibility(View.VISIBLE);
                locationQuery.setOnClickListener(new OnLocationLabelQueryListener());
            }
            if (isCheckin) {
                locationUrl.setVisibility(View.VISIBLE);
            }
        }
    }

    // Location query listener.
    public class OnLocationLabelQueryListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {

            // Get geo from the endpoint.
            String MicropubEndpoint = user.getMicropubEndpoint();
            if (MicropubEndpoint.contains("?")) {
                MicropubEndpoint += "&q=geo";
            }
            else {
                MicropubEndpoint += "?q=geo";
            }

            if (mCurrentLocation != null) {
                MicropubEndpoint += "&lat=" + mCurrentLocation.getLatitude() + "&lon=" + mCurrentLocation.getLongitude();
            }
            else if (latitude != null && longitude != null) {
                MicropubEndpoint += "&lat=" + latitude.toString() + "&lon=" + longitude.toString();
            }

            Snackbar.make(layout, getString(R.string.location_get), Snackbar.LENGTH_SHORT).show();
            StringRequest getRequest = new StringRequest(Request.Method.GET, MicropubEndpoint,
                    new Response.Listener<String>() {
                        @SuppressLint("ClickableViewAccessibility")
                        @Override
                        public void onResponse(String response) {
                            placeItems.clear();
                            String label = "";
                            String url = "";
                            String visibility = "";
                            try {
                                JSONObject geoResponse = new JSONObject(response);

                                // Geo property.
                                if (geoResponse.has("geo")) {
                                    JSONObject geoObject = geoResponse.getJSONObject("geo");
                                    if (geoObject.has("label")) {
                                        label = geoObject.getString("label");
                                    }
                                    if (geoObject.has("url")) {
                                        url = geoObject.getString("url");
                                    }
                                    if (geoObject.has("visibility")) {
                                        visibility = geoObject.getString("visibility");
                                    }
                                }

                                // Places property.
                                if (geoResponse.has("places")) {
                                    JSONObject placeObject;
                                    JSONArray placesList = geoResponse.getJSONArray("places");
                                    for (int i = 0; i < placesList.length(); i++) {
                                        Place place = new Place();
                                        placeObject = placesList.getJSONObject(i);

                                        if (placeObject.has("label")) {
                                            place.setName(placeObject.getString("label"));

                                            if (placeObject.has("longitude")) {
                                                place.setLongitude(placeObject.getString("longitude"));
                                            }

                                            if (placeObject.has("latitude")) {
                                                place.setLatitude(placeObject.getString("latitude"));
                                            }

                                            if (placeObject.has("url")) {
                                                place.setUrl(placeObject.getString("url"));
                                            }

                                            placeItems.add(place);
                                        }
                                    }
                                }
                            }
                            catch (JSONException e) {
                                Snackbar.make(layout, getString(R.string.location_error) + e.getMessage(), Snackbar.LENGTH_SHORT).show();
                            }

                            if (label.length() > 0 || placeItems.size() > 0) {
                                if (placeItems.size() > 0) {
                                    Snackbar.make(layout, getString(R.string.places_found), Snackbar.LENGTH_SHORT).show();

                                    // Add default as a place as well.
                                    if (label.length() > 0) {
                                        Place place = new Place();
                                        place.setName(label);
                                        if (url.length() > 0) {
                                            place.setUrl(url);
                                        }
                                        placeItems.add(place);
                                    }

                                    locationName.setThreshold(1);
                                    final ArrayAdapter placesAdapter = new ArrayAdapter<>(getApplicationContext(), R.layout.popup_item, placeItems);
                                    locationName.setAdapter(placesAdapter);
                                    locationName.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                                        @Override
                                        public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
                                            Place selected = (Place) arg0.getAdapter().getItem(arg2);
                                            locationName.setText(selected.getName());
                                            // Set this, even if it's empty as the user might have
                                            // selected another place first which had a URL.
                                            locationUrl.setText(selected.getUrl());
                                        }
                                    });
                                    locationName.setOnTouchListener(new View.OnTouchListener() {
                                        @Override
                                        public boolean onTouch(View paramView, MotionEvent paramMotionEvent) {
                                            if (!locationName.getText().toString().equals("")) {
                                                placesAdapter.getFilter().filter(null);
                                            }
                                            locationName.showDropDown();
                                            return false;
                                        }
                                    });
                                }
                                else {
                                    locationName.setText(label);
                                    if (url.length() > 0) {
                                        locationUrl.setText(url);
                                    }
                                }

                                // Visibility.
                                if (visibility.length() > 0) {
                                    int selection = 0;
                                    if (visibility.equals("private")) {
                                        selection = 1;
                                    }
                                    else if (visibility.equals("protected")) {
                                        selection = 2;
                                    }
                                    locationVisibility.setSelection(selection);
                                }
                            }
                            else {
                                Snackbar.make(layout, getString(R.string.location_no_results), Snackbar.LENGTH_SHORT).show();
                            }

                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {}
                    }
            )
            {
                @Override
                public Map<String, String> getHeaders() {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Accept", "application/json");
                    headers.put("Authorization", "Bearer " + user.getAccessToken());
                    return headers;
                }
            };

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
            queue.add(getRequest);
        }
    }

}