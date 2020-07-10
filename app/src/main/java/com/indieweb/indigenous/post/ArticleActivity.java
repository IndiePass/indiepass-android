package com.indieweb.indigenous.post;

import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;

import com.indieweb.indigenous.R;

public class ArticleActivity extends BaseCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        canAddMedia = true;
        canAddLocation = true;
        postType = "Article";
        addCounter = true;
        setContentView(R.layout.activity_article);
        super.onCreate(savedInstanceState);
        setTitle(post.getMainPostTitle());
    }

    @Override
    public void onPostButtonClick(MenuItem item) {
        boolean hasErrors = false;

        if (saveAsDraft != null && saveAsDraft.isChecked()) {
            saveDraft("article", null);
            return;
        }

        if (post.supports(Post.FEATURE_TITLE) && TextUtils.isEmpty(title.getText())) {
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
