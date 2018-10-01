package com.indieweb.indigenous.micropub.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.Toast;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Draft;

public class ArticleActivity extends BaseCreateActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddImage = true;
        canAddLocation = true;
        postType = "Article";
        addCounter = true;
        setContentView(R.layout.activity_article);
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        boolean hasErrors = false;

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            saveDraft("article");
            return;
        }

        if (TextUtils.isEmpty(title.getText())) {
            hasErrors = true;
            title.setError(getString(R.string.required_field));
        }

        if (TextUtils.isEmpty(body.getText())) {
            hasErrors = true;
            body.setError(getString(R.string.required_field));
        }

        if (!hasErrors) {
            sendBasePost(item);
        }
    }

}
