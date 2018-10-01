package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Draft;

public class NoteActivity extends BaseCreateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddImage = true;
        canAddLocation = true;
        postType = "Note";
        addCounter = true;
        setContentView(R.layout.activity_note);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            saveDraft("note");
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
