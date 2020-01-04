package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.BaseCreate;

public class NoteActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddMedia = true;
        canAddLocation = true;
        postType = "Note";
        addCounter = true;
        setContentView(R.layout.activity_note);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            saveDraft("note", null);
            return;
        }

        if (TextUtils.isEmpty(body.getText())) {
            body.setError(getString(R.string.required_field));
        }
        else {
            sendBasePost(item);
        }
    }

}
