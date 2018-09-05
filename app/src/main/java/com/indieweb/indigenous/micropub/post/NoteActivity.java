package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class NoteActivity extends BasePostActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddImage = true;
        postType = "Note";
        addCounter = true;
        setContentView(R.layout.activity_note);
        super.onCreate(savedInstanceState);


    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        sendBasePost(item);
    }

}
