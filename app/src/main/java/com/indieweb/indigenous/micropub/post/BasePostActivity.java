package com.indieweb.indigenous.micropub.post;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.Syndication;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.VolleyMultipartRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.lang.Integer.parseInt;

@SuppressLint("Registered")
abstract public class BasePostActivity extends AppCompatActivity implements SendPostInterface {

    private User user;
    private List<Syndication> syndicationTargets = new ArrayList<>();
    private MenuItem sendItem;
    private CardView imageCard;
    private ImageView imagePreview;
    private EditText title;
    private EditText body;
    private EditText tags;
    private Uri imageUri;
    private Bitmap bitmap;
    private String mime = "image/jpg";
    private int PICK_IMAGE_REQUEST = 1;

    EditText url;
    String urlPostKey;
    String directSend = "";
    String hType = "entry";
    String postType = "Post";
    boolean canAddImage = false;
    boolean addCounter = false;
    Map<String, String> bodyParams = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get current user.
        user = new Accounts(this).getCurrentUser();

        // Syndication targets.
        LinearLayout syndicationLayout = findViewById(R.id.syndicationTargets);
        String syndicationTargetsString = user.getSyndicationTargets();
        if (syndicationTargetsString.length() > 0) {
            JSONObject object;
            try {
                JSONObject s = new JSONObject(syndicationTargetsString);
                JSONArray itemList = s.getJSONArray("syndicate-to");
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
                Toast.makeText(this, "Error parsing syndications: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }


        // Check incoming text or image.
        body = findViewById(R.id.body);
        imagePreview = findViewById(R.id.imagePreview);
        imageCard = findViewById(R.id.imageCard);
        url = findViewById(R.id.url);

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
                catch (NullPointerException ignored) {}
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
                if (incomingImage != null && incomingImage.length() > 0 && imagePreview != null && imageCard != null) {
                    try {
                        imageCard.setVisibility(View.VISIBLE);
                        ContentResolver cR = this.getContentResolver();
                        imageUri = Uri.parse(incomingImage);
                        mime = cR.getType(imageUri);
                        bitmap = scaleDown(MediaStore.Images.Media.getBitmap(cR, imageUri), 1000, false);
                        imagePreview.setImageBitmap(bitmap);
                    }
                    catch (IOException ignored) {}
                }
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
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Image selected", Toast.LENGTH_SHORT).show();
            imageUri = data.getData();
            try {
                imageCard.setVisibility(View.VISIBLE);
                ContentResolver cR = this.getContentResolver();
                mime = cR.getType(imageUri);
                bitmap = scaleDown(MediaStore.Images.Media.getBitmap(cR, imageUri), 1000, false);
                imagePreview.setImageBitmap(bitmap);
            }
            catch (IOException ignored) {}
        }
        else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Scale down to max image size.
     *
     * @param realImage
     *   The image.
     * @param maxImageSize
     *   The maximum image size.
     * @param filter
     *   Bitmap filter.
     *
     * @return Bitmap
     */
    public Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    /**
     * Convert bitmap to byte[] array.
     */
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {

        // Default quality. The preference is stored as a string, but cast it to an integer.
        Integer ImageQuality = 80;
        String qualityPreference = Preferences.getPreference(this, "pref_key_image_quality", ImageQuality.toString());
        if (parseInt(qualityPreference) <= 100 && parseInt(qualityPreference) > 0) {
            ImageQuality = parseInt(qualityPreference);
        }

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        switch (mime) {
            case "image/png":
                bitmap.compress(Bitmap.CompressFormat.PNG, ImageQuality, byteArrayOutputStream);
                break;
            case "image/jpg":
            default:
                bitmap.compress(Bitmap.CompressFormat.JPEG, ImageQuality, byteArrayOutputStream);
                break;
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

        Toast.makeText(getApplicationContext(), "Sending, please wait", Toast.LENGTH_SHORT).show();

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());
        VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, user.getMicropubEndpoint(),
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Toast.makeText(getApplicationContext(), "Post success", Toast.LENGTH_LONG).show();
                        finish();
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
                title = findViewById(R.id.title);
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
                tags = findViewById(R.id.tags);
                if (tags != null) {
                    List<String> tagsList = new ArrayList<>(Arrays.asList(tags.getText().toString().split(",")));
                    int i = 0;
                    for (String tag: tagsList) {
                        tag = tag.trim();
                        if (tag.length() > 0) {
                            bodyParams.put("category["+ i +"]", tag);
                            i++;
                        }
                    }
                }

                // SyndicationTargets.
                if (syndicationTargets.size() > 0) {
                    CheckBox checkbox;
                    for (int j = 0; j < syndicationTargets.size(); j++) {

                        checkbox = findViewById(j);
                        if (checkbox != null && checkbox.isChecked()) {
                            bodyParams.put("mp-syndicate-to[" + j + "]", syndicationTargets.get(j).getUid());
                        }
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
                    if (bitmap != null) {
                        params.put("photo[0]", new DataPart(imagename + "." + extension, getFileDataFromDrawable(bitmap)));
                    }
                }
                return params;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                0,
                -1,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT));
        queue.add(request);
    }

    /**
     * Sets the incoming text as URL and puts focus on body field.
     *
     * @param incomingUrl
     *   The incoming URL.
     */
    public void setUrlAndFocusOnMessage(String incomingUrl) {
        url.setText(incomingUrl);
        if (body != null) {
            body.requestFocus();
        }
    }

}