package com.indieweb.indigenous;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NoteActivity extends AppCompatActivity {

    Button createPost;
    EditText note;
    EditText tags;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_note);

        createPost = findViewById(R.id.createNoteButton);
        createPost.setOnClickListener(doCreatePost);
    }

    /**
     * OnClickListener for the 'create post' button.
     */
    private final View.OnClickListener doCreatePost = new View.OnClickListener() {
        public void onClick(View v) {

            RequestQueue queue = Volley.newRequestQueue(getApplicationContext());

            note = findViewById(R.id.noteText);
            tags = findViewById(R.id.noteTags);
            SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
            String MicropubEndPoint = preferences.getString("micropub_endpoint", "");

            StringRequest postRequest = new StringRequest(Request.Method.POST, MicropubEndPoint,
                    new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {

                            Toast.makeText(getApplicationContext(), "Post success", Toast.LENGTH_LONG).show();

                            Intent TimeLine = new Intent(getBaseContext(), TimeLineActivity.class);
                            startActivity(TimeLine);

                            // response
                            Log.d("indigenous_debug", response);
                        }
                    },
                    new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Toast.makeText(getApplicationContext(), "Note posting failed", Toast.LENGTH_LONG).show();
                            Log.d("indigenous_debug", error.getMessage());
                        }
                    }
            )
            {
                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();

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
                    HashMap<String, String> headers = new HashMap<String, String>();
                    headers.put("Accept", "application/json");

                    // Add access token to header.
                    SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
                    String AccessToken = preferences.getString("access_token", "");
                    headers.put("Authorization", "Bearer " + AccessToken);

                    return headers;
                }
            };

            queue.add(postRequest);

        }
    };

}
