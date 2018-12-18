package com.indieweb.indigenous.micropub.post;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.Bitmap;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.os.Looper;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MultiAutoCompleteTextView;
import android.widget.Spinner;
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
import com.bumptech.glide.request.target.SimpleTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.SettingsClient;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.indieweb.indigenous.BuildConfig;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.micropub.MicropubAction;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.model.Syndication;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;
import com.indieweb.indigenous.util.VolleyMultipartRequest;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.single.PermissionListener;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

@SuppressLint("Registered")
abstract public class BaseCreateActivity extends AppCompatActivity implements SendPostInterface, TextWatcher {

    EditText body;
    EditText title;
    Bitmap bitmap;
    CheckBox saveAsDraft;
    DatabaseHelper db;
    User user;
    Uri imageUri;
    MultiAutoCompleteTextView tags;
    private List<Syndication> syndicationTargets = new ArrayList<>();
    private MenuItem sendItem;
    private LinearLayout imageWidget;
    private ImageView imagePreview;
    private CheckBox postStatus;
    private String mime = "image/jpg";
    private int PICK_IMAGE_REQUEST = 1;

    EditText url;
    String urlPostKey;
    String directSend = "";
    String hType = "entry";
    String postType = "Post";
    boolean finishActivity = true;
    boolean canAddImage = false;
    boolean canAddLocation = false;
    boolean addCounter = false;
    Map<String, String> bodyParams = new HashMap<>();

    Integer draftId;
    String fileUrl;
    TextView mediaUrl;
    boolean isMediaRequest = false;

    LinearLayout locationWrapper;
    Spinner locationVisibility;
    private FusedLocationProviderClient mFusedLocationClient;
    private Boolean mRequestingLocationUpdates = false;
    private SettingsClient mSettingsClient;
    private LocationRequest mLocationRequest;
    private LocationSettingsRequest mLocationSettingsRequest;
    private LocationCallback mLocationCallback;
    private Location mCurrentLocation;
    private static final int REQUEST_CHECK_SETTINGS = 100;
    private static final long UPDATE_INTERVAL_IN_MILLISECONDS = 10000;
    private static final long FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS = 5000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get current user.
        user = new Accounts(this).getCurrentUser();

        // Syndication targets.
        LinearLayout syndicationLayout = findViewById(R.id.syndicationTargets);
        String syndicationTargetsString = user.getSyndicationTargets();
        if (syndicationLayout != null && syndicationTargetsString.length() > 0) {
            JSONObject object;
            try {
                JSONArray itemList = new JSONArray(syndicationTargetsString);

                if (itemList.length() > 0) {
                    TextView syn = new TextView(this);
                    syn.setText(R.string.syndicate_to);
                    syn.setPadding(20, 10, 0, 0);
                    syn.setTextSize(16);
                    syndicationLayout.addView(syn);
                }

                for (int i = 0; i < itemList.length(); i++) {
                    object = itemList.getJSONObject(i);
                    Syndication syndication = new Syndication();
                    syndication.setUid(object.getString("uid"));
                    syndication.setName(object.getString("name"));
                    syndicationTargets.add(syndication);

                    CheckBox ch = new CheckBox(this);
                    ch.setText(syndication.getName());
                    ch.setId(i);
                    syndicationLayout.addView(ch);
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error parsing syndication targets: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }


        // Get a couple elements for requirement checks or pre-population.
        title = findViewById(R.id.title);
        body = findViewById(R.id.body);
        imagePreview = findViewById(R.id.imagePreview);
        imageWidget = findViewById(R.id.imageWidget);
        url = findViewById(R.id.url);
        tags = findViewById(R.id.tags);
        saveAsDraft = findViewById(R.id.saveAsDraft);
        locationWrapper = findViewById(R.id.locationWrapper);
        locationVisibility = findViewById(R.id.locationVisibility);

        // Autocomplete of tags.
        if (tags != null && Preferences.getPreference(this, "pref_key_tags_list", false)) {
            tags.addTextChangedListener(BaseCreateActivity.this);
            new MicropubAction(getApplicationContext(), user).getTagsList(tags);
        }

        if (isMediaRequest) {
            mediaUrl = findViewById(R.id.mediaUrl);
        }

        // Add counter.
        if (addCounter) {
            TextInputLayout textInputLayout = findViewById(R.id.textInputLayout);
            textInputLayout.setCounterEnabled(true);
        }

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action)) {
                try {
                    if (extras.containsKey(Intent.EXTRA_TEXT)) {
                        String urlIncoming = extras.get(Intent.EXTRA_TEXT).toString();
                        if (urlIncoming != null && urlIncoming.length() > 0 && url != null) {
                            setUrlAndFocusOnMessage(urlIncoming);
                            if (directSend.length() > 0) {
                                if (Preferences.getPreference(this, directSend, false)) {
                                    sendBasePost(null);
                                }
                            }
                        }
                    }
                }
                catch (NullPointerException ignored) { }
            }
            else {
                String incomingText = extras.getString("incomingText");
                if (incomingText != null && incomingText.length() > 0 && (body != null || url != null)) {
                    if (url != null) {
                        setUrlAndFocusOnMessage(incomingText);
                    }
                    else {
                        body.setText(incomingText);
                    }
                }
            }

            if (canAddImage) {
                String incomingImage = extras.getString("incomingImage");
                if (incomingImage != null && incomingImage.length() > 0 && imagePreview != null && imageWidget != null) {
                    imageUri = Uri.parse(incomingImage);
                    prepareImagePreviewAndBitmap();
                }
            }

            // Draft support.
            draftId = extras.getInt("draftId");
            if (draftId > 0) {
                prepareDraft(draftId);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.post_action_menu, menu);

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
            case R.id.addImage:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
                return true;

            case R.id.addLocation:

                if (!mRequestingLocationUpdates) {
                    Dexter.withActivity(this)
                            .withPermission(Manifest.permission.ACCESS_FINE_LOCATION)
                            .withListener(new PermissionListener() {
                                @Override
                                public void onPermissionGranted(PermissionGrantedResponse response) {
                                    mRequestingLocationUpdates = true;
                                    startLocationUpdates();
                                }

                                @Override
                                public void onPermissionDenied(PermissionDeniedResponse response) {
                                    if (response.isPermanentlyDenied()) {
                                        openSettings();
                                    }
                                }

                                @Override
                                public void onPermissionRationaleShouldBeShown(com.karumi.dexter.listener.PermissionRequest permission, PermissionToken token) {
                                    token.continuePermissionRequest();
                                }

                            }).check();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            imageUri = data.getData();
            prepareImagePreviewAndBitmap();
        }

        if (requestCode == REQUEST_CHECK_SETTINGS && resultCode == RESULT_OK) {
            startLocationUpdates();
        }
    }

    /**
     * Prepare image preview and bitmap.
     */
    public void prepareImagePreviewAndBitmap() {
        imageWidget.setVisibility(View.VISIBLE);
        ContentResolver cR = this.getContentResolver();
        mime = cR.getType(imageUri);

        Integer ImageSize = 1000;
        if (Preferences.getPreference(getApplicationContext(), "pref_key_image_scale", true)) {
            String sizePreference = Preferences.getPreference(this, "pref_key_image_size", ImageSize.toString());
            if (parseInt(sizePreference) > 0) {
                ImageSize = parseInt(sizePreference);
            }
        }

        Glide
            .with(getApplicationContext())
            .asBitmap()
            .load(imageUri)
            .into(new SimpleTarget<Bitmap>(ImageSize, ImageSize) {
                @Override
                public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> transition) {
                    imagePreview.setImageBitmap(resource);
                    bitmap = resource;
                }
            });
    }

    /**
     * Convert bitmap to byte[] array.
     */
    public byte[] getFileDataFromDrawable(Bitmap bitmap, Boolean scale) {

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();

        if (scale) {
            Integer ImageQuality = 80;
            // Default quality. The preference is stored as a string, but cast it to an integer.
            String qualityPreference = Preferences.getPreference(this, "pref_key_image_quality", ImageQuality.toString());
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
                InputStream is = cR.openInputStream(imageUri);
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
     * Prepares the activity with the draft.
     *
     * @param draftId
     *   The draft id.
     */
    public void prepareDraft(Integer draftId) {
        db = new DatabaseHelper(this);

        Draft draft = db.getDraft(draftId);
        if (draft.getId() > 0) {

            // Set as checked again to avoid confusion.
            saveAsDraft.setChecked(true);

            // Name.
            if (title != null && draft.getName().length() > 0) {
                title.setText(draft.getName());
            }

            // Body.
            if (body != null && draft.getBody().length() > 0) {
                body.setText(draft.getBody());
            }

            // Tags.
            if (tags != null && draft.getTags().length() > 0) {
                tags.setText(draft.getTags());
            }

            // Url.
            if (url != null && draft.getUrl().length() > 0) {
                url.setText(draft.getUrl());
            }

            // Image
            if (canAddImage && draft.getImage().length() > 0) {
                imageUri = Uri.parse(draft.getImage());
                prepareImagePreviewAndBitmap();
            }

        }
    }

    /**
     * Send post.
     */
    public void sendBasePost(MenuItem item) {
        // TODO move this to MicropubActionCreate
        sendItem = item;

        if (!new Connection(this).hasConnection()) {
            Toast.makeText(this, getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        if (sendItem != null) {
            sendItem.setEnabled(false);
        }

        Toast.makeText(getApplicationContext(), "Sending, please wait", Toast.LENGTH_SHORT).show();

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
                            Toast.makeText(getApplicationContext(), "Post success", Toast.LENGTH_SHORT).show();

                            // Remove draft if needed.
                            // TODO notify draft adapter
                            if (draftId != null && draftId > 0) {
                                db = new DatabaseHelper(getApplicationContext());
                                db.deleteDraft(draftId);
                            }

                            finish();
                        }

                        if (isMediaRequest) {
                            fileUrl = response.headers.get("Location");
                            if (fileUrl != null && fileUrl.length() > 0) {
                                Toast.makeText(getApplicationContext(), "Media upload success", Toast.LENGTH_SHORT).show();
                                mediaUrl.setText(fileUrl);
                                mediaUrl.setVisibility(View.VISIBLE);
                                new Utility(getApplicationContext()).copyToClipboard(fileUrl, "Media url");
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "No file url found", Toast.LENGTH_SHORT).show();
                            }

                            if (sendItem != null) {
                                sendItem.setEnabled(true);
                            }
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
                                Toast.makeText(getApplicationContext(), postType + " posting failed. Status code: " + code + "; message: " + result, Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        if (sendItem != null) {
                            sendItem.setEnabled(true);
                        }
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {

                // Send along access token too, Wordpress scans for the token in the body.
                bodyParams.put("access_token", user.getAccessToken());

                // h type.
                bodyParams.put("h", hType);

                // Title
                if (title != null) {
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

                // Location.
                if (canAddLocation && mCurrentLocation != null) {
                    String coordinates = mCurrentLocation.getLatitude() + "," + mCurrentLocation.getLongitude();
                    coordinates += "," + mCurrentLocation.getAltitude();
                    bodyParams.put("location", "geo:" + coordinates);
                    if (locationVisibility != null && locationWrapper.getVisibility() == View.VISIBLE) {
                        bodyParams.put("location-visibility", locationVisibility.getSelectedItem().toString());
                    }
                }

                return bodyParams;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }

            @Override
            protected Map<String, DataPart> getByteData() {
                Map<String, DataPart> params = new HashMap<>();

                if (bitmap != null) {
                    long imagename = System.currentTimeMillis();

                    String extension = "jpg";
                    if (mime.equals("image/png")) {
                        extension = "png";
                    }

                    String imagePostParam = "photo";
                    if (isMediaRequest) {
                        imagePostParam = "file";
                    }

                    // Put image in body. Send along whether to scale or not.
                    Boolean scale = Preferences.getPreference(getApplicationContext(), "pref_key_image_scale", true);
                    params.put(imagePostParam, new DataPart(imagename + "." + extension, getFileDataFromDrawable(bitmap, scale)));
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
        url.setText(incomingUrl);
        if (title != null) {
            title.requestFocus();
        }
        else if (body != null) {
            body.requestFocus();
        }
    }

    /**
     * Save draft.
     */
    public void saveDraft(String type) {

        Draft draft = new Draft();

        if (draftId != null && draftId > 0) {
            draft.setId(draftId);
        }

        draft.setType(type);
        draft.setAccount(user.getMeWithoutProtocol());

        if (title != null && !TextUtils.isEmpty(title.getText())) {
            draft.setName(title.getText().toString());
        }

        if (body != null && !TextUtils.isEmpty(body.getText())) {
            draft.setBody(body.getText().toString());
        }

        if (tags != null && !TextUtils.isEmpty(tags.getText())) {
            draft.setTags(tags.getText().toString());
        }

        if (url != null && !TextUtils.isEmpty(url.getText())) {
            draft.setUrl(url.getText().toString());
        }

        if (imageUri != null) {
            draft.setImage(imageUri.toString());
        }

        db = new DatabaseHelper(this);
        db.saveDraft(draft);

        Toast.makeText(this, getString(R.string.draft_saved), Toast.LENGTH_SHORT).show();
        finish();
    }

    /**
     * Start location updates.
     */
    public void startLocationUpdates() {

        initLocationLibraries();
        if (mSettingsClient == null) {
            return;
        }

        mSettingsClient
            .checkLocationSettings(mLocationSettingsRequest)
            .addOnSuccessListener(this, new OnSuccessListener<LocationSettingsResponse>() {
                @SuppressLint("MissingPermission")
                @Override
                public void onSuccess(LocationSettingsResponse locationSettingsResponse) {

                    Toast.makeText(getApplicationContext(), getString(R.string.getting_location), Toast.LENGTH_SHORT).show();

                    //noinspection MissingPermission
                    mFusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

                    updateLocationUI();
                }
            })
            .addOnFailureListener(this, new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    int statusCode = ((ApiException) e).getStatusCode();
                    switch (statusCode) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                // Show the dialog by calling startResolutionForResult(),
                                // and check the result in onActivityResult().
                                ResolvableApiException rae = (ResolvableApiException) e;
                                rae.startResolutionForResult(BaseCreateActivity.this, REQUEST_CHECK_SETTINGS);
                            }
                            catch (IntentSender.SendIntentException sie) {
                                Toast.makeText(getApplicationContext(), "PendingIntent unable to execute request.", Toast.LENGTH_SHORT).show();
                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:
                            String errorMessage = "Location settings are inadequate, and cannot be fixed here. Fix in Settings.";
                            Toast.makeText(getApplicationContext(), errorMessage, Toast.LENGTH_LONG).show();
                    }

                    updateLocationUI();
                }
            });
    }

    /**
     * Update the UI displaying the location data.
     */
    private void updateLocationUI() {
        if (mCurrentLocation != null) {
            Toast.makeText(getApplicationContext(), "Location found: latitude: " + mCurrentLocation.getLatitude() + " - longitude: " + mCurrentLocation.getLongitude() + " - altitude: " + mCurrentLocation.getAltitude(), Toast.LENGTH_LONG).show();
            if (Preferences.getPreference(getApplicationContext(), "pref_key_location_visibility", false)) {
                locationWrapper.setVisibility(View.VISIBLE);
            }
            stopLocationUpdates();
        }
    }

    /**
     * Stop location updates.
     */
    public void stopLocationUpdates() {
        mFusedLocationClient.removeLocationUpdates(mLocationCallback);
    }

    /**
     * Initiate the location libraries and services.
     */
    private void initLocationLibraries() {
        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        mSettingsClient = LocationServices.getSettingsClient(this);

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                super.onLocationResult(locationResult);
                // location is received
                mCurrentLocation = locationResult.getLastLocation();
                updateLocationUI();
            }
        };

        mRequestingLocationUpdates = false;

        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setFastestInterval(FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);

        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        mLocationSettingsRequest = builder.build();
    }

    /**
     * Open settings screen.
     */
    private void openSettings() {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", BuildConfig.APPLICATION_ID, null);
        intent.setData(uri);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
    }

    @Override
    public void beforeTextChanged(CharSequence s, int start, int count, int after) { }

    @Override
    public void onTextChanged(CharSequence s, int start, int before, int count) { }

    @Override
    public void afterTextChanged(Editable s) { }
}