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

public class NoteActivity extends AppCompatActivity {

    Button createPost;
    EditText note;
    EditText tags;
    ImageView image;
    Uri imageUri;
    Bitmap bitmap;

    private int PICK_IMAGE_REQUEST = 1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        createPost = findViewById(R.id.createNoteButton);
        createPost.setOnClickListener(doCreatePost);

        image = findViewById(R.id.imageView);
        image.setOnClickListener(selectImage);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK) {
            Toast.makeText(getApplicationContext(), "Image selected", Toast.LENGTH_SHORT).show();
            imageUri = data.getData();
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), imageUri);
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
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
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
    private final View.OnClickListener doCreatePost = new View.OnClickListener() {
        public void onClick(View v) {

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            Toast.makeText(getApplicationContext(), "Starting upload, do not push again!", Toast.LENGTH_LONG).show();
            createPost.setEnabled(false);

            note = findViewById(R.id.noteText);
            tags = findViewById(R.id.noteTags);
            SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
            String MicropubEndPoint = preferences.getString("micropub_endpoint", "");

            VolleyMultipartRequest request = new VolleyMultipartRequest(Request.Method.POST, MicropubEndPoint,
                    new Response.Listener<NetworkResponse>() {
                        @Override
                        public void onResponse(NetworkResponse response) {

                            Toast.makeText(getApplicationContext(), "Post success", Toast.LENGTH_LONG).show();

                            Intent TimeLine = new Intent(getBaseContext(), TimeLineActivity.class);
                            startActivity(TimeLine);

                            // response
                            Log.d("indigenous_debug", response.toString());

                            createPost.setEnabled(true);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Note posting failed", Toast.LENGTH_LONG).show();
                            Log.d("indigenous_debug", error.getMessage());
                            createPost.setEnabled(true);
                        }
                    }
            )
            {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<>();

                    // Content and entry.
                    params.put("h", "entry");
                    params.put("content", note.getText().toString());

                    // Tags.
                    // TODO make sure the UI is ok
                    List<String> tagsList = new ArrayList<>(Arrays.asList(tags.getText().toString().split(",")));
                    int i = 0;
                    for (String tag: tagsList) {
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
                    params.put("photo[0]", new DataPart(imagename + ".png", getFileDataFromDrawable(bitmap)));
                    return params;
                }
            };

            queue.add(request);

        }
    };

}
