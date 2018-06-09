package com.indieweb.indigenous.micropub.post;

import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.util.Log;
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

public class NoteActivity extends AppCompatActivity {

    EditText note;
    EditText tags;
    CardView card;
    ImageView image;
    Uri imageUri;
    Bitmap bitmap;
    String mime = "image/jpg";
    LinearLayout syndicationLayout;
    private List<Syndication> Syndications = new ArrayList<>();
    private MenuItem sendItem;
    User user;

    private int PICK_NOTE_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        image = findViewById(R.id.imageView);
        card = findViewById(R.id.imageCard);
        user = new Accounts(this).getCurrentUser();

        // TODO make helper function.
        syndicationLayout = findViewById(R.id.syndicate);
        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String syndicationsString = preferences.getString("syndications", "");
        if (syndicationsString.length() > 0) {
            JSONObject object;
            try {
                JSONObject s = new JSONObject(syndicationsString);
                JSONArray itemList = s.getJSONArray("syndicate-to");
                for (int i = 0; i < itemList.length(); i++) {
                    object = itemList.getJSONObject(i);
                    Syndication syndication = new Syndication();
                    syndication.setUid(object.getString("uid"));
                    syndication.setName(object.getString("name"));
                    Syndications.add(syndication);

                    CheckBox ch = new CheckBox(this);
                    ch.setText(syndication.getName());
                    ch.setId(i);
                    syndicationLayout.addView(ch);
                }

            }
            catch (JSONException e) {
                Log.d("indigenous_debug", e.getMessage());
            }
        }

        // Check incoming text or image.
        note = findViewById(R.id.noteText);
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String incomingText = extras.getString("incomingText");
            if (incomingText != null && incomingText.length() > 0) {
                note.setText(incomingText);
            }
            String incomingImage = extras.getString("incomingImage");
            if (incomingImage != null && incomingImage.length() > 0) {
                try {
                    card.setVisibility(View.VISIBLE);
                    ContentResolver cR = this.getContentResolver();
                    imageUri = Uri.parse(incomingImage);
                    mime = cR.getType(imageUri);
                    bitmap = scaleDown(MediaStore.Images.Media.getBitmap(cR, imageUri), 1000, false);
                    image.setImageBitmap(bitmap);
                }
                catch (IOException ignored) {}
            }
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.action_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.send:
                sendItem = item;
                send();
                return true;

            case R.id.addImage:
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_NOTE_IMAGE_REQUEST);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_NOTE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Image selected", Toast.LENGTH_SHORT).show();
            imageUri = data.getData();
            try {
                card.setVisibility(View.VISIBLE);

                ContentResolver cR = this.getContentResolver();
                mime = cR.getType(imageUri);
                bitmap = scaleDown(MediaStore.Images.Media.getBitmap(cR, imageUri), 1000, false);
                image.setImageBitmap(bitmap);
            }
            catch (IOException ignored) {}
        }
        else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

   /**
    * Convert bitmap to byte[] array.
    */
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {

        // Default quality. The preference is stored as a string, but cast it to an integer.
        Integer ImageQuality = 80;
        String qualityPreference = Preferences.getPreference(getApplicationContext(), "pref_key_image_quality", ImageQuality.toString());
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
     * Scale down to max image size.
     *
     * TODO move to util, as we have the same in article.
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
    public static Bitmap scaleDown(Bitmap realImage, float maxImageSize, boolean filter) {
        float ratio = Math.min(
                maxImageSize / realImage.getWidth(),
                maxImageSize / realImage.getHeight());
        int width = Math.round(ratio * realImage.getWidth());
        int height = Math.round(ratio * realImage.getHeight());
        return Bitmap.createScaledBitmap(realImage, width, height, filter);
    }

    /**
     * Send note.
     */
    public void send() {

        sendItem.setEnabled(false);

        RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

        tags = findViewById(R.id.noteTags);

        Toast.makeText(getApplicationContext(), "Sending, please wait", Toast.LENGTH_SHORT).show();

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
                                Toast.makeText(getApplicationContext(), "Note posting failed. Status code: " + code + "; message: " + result, Toast.LENGTH_LONG).show();
                            }
                            else {
                                Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                            }
                        }
                        catch (Exception e) {
                            Toast.makeText(getApplicationContext(), "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                        }
                        sendItem.setEnabled(true);
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Post access token too, Wordpress scans for the token in the body.
                params.put("access_token", user.getAccessToken());

                // Content and entry.
                params.put("h", "entry");
                params.put("content", note.getText().toString());

                // Tags.
                // TODO make sure the UI is ok
                List<String> tagsList = new ArrayList<>(Arrays.asList(tags.getText().toString().split(",")));
                int i = 0;
                for (String tag: tagsList) {
                    tag = tag.trim();
                    if (tag.length() > 0) {
                        params.put("category["+ i +"]", tag);
                        i++;
                    }
                }

                // Syndications.
                if (Syndications.size() > 0) {
                    CheckBox checkbox;
                    for (int j = 0; j < Syndications.size(); j++) {

                        checkbox = findViewById(j);
                        if (checkbox.isChecked()) {
                            params.put("mp-syndicate-to[" + j + "]", Syndications.get(j).getUid());
                        }
                    }
                }

                return params;
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
                long imagename = System.currentTimeMillis();
                String extension = "jpg";
                if (mime.equals("image/png")) {
                    extension = "png";
                }
                if (bitmap != null) {
                    params.put("photo[0]", new DataPart(imagename + "." + extension, getFileDataFromDrawable(bitmap)));
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

}
