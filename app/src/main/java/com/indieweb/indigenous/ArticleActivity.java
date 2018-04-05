package com.indieweb.indigenous;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ArticleActivity extends AppCompatActivity {

    Button createArticle;
    EditText title;
    EditText article;
    EditText tags;
    ImageView image;
    Uri imageUri;
    Bitmap bitmap;

    private int PICK_ARTICLE_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_article);

        createArticle = findViewById(R.id.createArticleButton);
        createArticle.setOnClickListener(doCreateArticle);

        image = findViewById(R.id.imageView);
        image.setOnClickListener(selectImage);

        // Set incoming in content.
        article = findViewById(R.id.articleText);
        Bundle extras = getIntent().getExtras();
        assert extras != null;
        String incoming = extras.getString("incoming");
        if (incoming != null && incoming.length() > 0) {
            article.setText(incoming);
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_ARTICLE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Image selected", Toast.LENGTH_SHORT).show();
            imageUri = data.getData();
            try {
                // TODO hardcoded to 1000x750 - fix this.
                bitmap = Bitmap.createScaledBitmap(MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri), 1000, 750, false);
                image.setImageBitmap(bitmap);
            }
            catch (IOException ignored) {}
        }
        else {
            Toast.makeText(getApplicationContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * OnClickListener for the 'select image' button.
     */
    private final View.OnClickListener selectImage = new View.OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_ARTICLE_IMAGE_REQUEST);
        }
    };

   /**
    * Convert bitmap to byte[] array.
    *
    * 0 means worse quality
    * 100 means best quality
    */
    public byte[] getFileDataFromDrawable(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG, 80, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }

    /**
     * OnClickListener for the 'create post' button.
     */
    private final View.OnClickListener doCreateArticle = new View.OnClickListener() {
        public void onClick(View v) {

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            createArticle.setEnabled(false);

            title = findViewById(R.id.articleTitle);
            tags = findViewById(R.id.articleTags);
            SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
            String MicropubEndPoint = preferences.getString("micropub_endpoint", "");

            VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, MicropubEndPoint,
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {

                            Toast.makeText(getApplicationContext(), "Post success", Toast.LENGTH_LONG).show();

                            Intent Channels = new Intent(getBaseContext(), ChannelsActivity.class);
                            startActivity(Channels);

                            createArticle.setEnabled(true);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Note posting failed", Toast.LENGTH_LONG).show();
                            Log.d("indigenous_debug", error.getMessage());
                            createArticle.setEnabled(true);
                        }
                    }
            )
            {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    // name, content and entry.
                    params.put("h", "entry");
                    params.put("name", title.getText().toString());
                    params.put("content", article.getText().toString());

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

                    return params;
                }

                @Override
                public Map<String, String> getHeaders() throws AuthFailureError {
                    HashMap<String, String> headers = new HashMap<>();
                    headers.put("Accept", "application/json");

                    // Add access token to header.
                    SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
                    String AccessToken = preferences.getString("access_token", "");
                    headers.put("Authorization", "Bearer " + AccessToken);

                    return headers;
                }

                @Override
                protected Map<String, DataPart> getByteData() {
                    Map<String, DataPart> params = new HashMap<>();
                    long imagename = System.currentTimeMillis();
                    if (bitmap != null) {
                        params.put("photo[0]", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
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
    };

}
