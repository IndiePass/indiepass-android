package com.indieweb.indigenous.micropub;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.indieauth.IndieAuthActivity;
import com.indieweb.indigenous.micropub.post.ArticleActivity;
import com.indieweb.indigenous.micropub.post.BookmarkActivity;
import com.indieweb.indigenous.micropub.post.EventActivity;
import com.indieweb.indigenous.micropub.post.IssueActivity;
import com.indieweb.indigenous.micropub.post.LikeActivity;
import com.indieweb.indigenous.micropub.post.NoteActivity;
import com.indieweb.indigenous.micropub.post.ReplyActivity;
import com.indieweb.indigenous.micropub.post.RepostActivity;
import com.indieweb.indigenous.micropub.post.RsvpActivity;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

public class MicropubActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String incomingText = "";
    String incomingImage = "";
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_micropub);

        user = new Accounts(this).getCurrentUser();
        if (!user.isValid()) {
            Toast.makeText(MicropubActivity.this, getString(R.string.no_user), Toast.LENGTH_SHORT).show();
            Intent a = new Intent(getBaseContext(), IndieAuthActivity.class);
            startActivity(a);
            finish();
            return;
        }

        this.setTitle(user.getMeWithoutProtocol());

        // Listen to incoming data.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras != null) {
            if (Intent.ACTION_SEND.equals(action)) {
                try {
                    if (extras.containsKey(Intent.EXTRA_TEXT)) {
                        incomingText = extras.get(Intent.EXTRA_TEXT).toString();
                    }
                    if (extras.containsKey(Intent.EXTRA_STREAM)) {
                        incomingImage = extras.get(Intent.EXTRA_STREAM).toString();
                    }
                }
                catch (NullPointerException ignored) {}
            }
        }

        TextView createTitle = findViewById(R.id.createTitle);

        // Add title and preview.
        if (incomingText.length() > 0 || incomingImage.length() > 0) {
            createTitle.setVisibility(View.VISIBLE);

            if (incomingText.length() > 0) {
                TextView preview = findViewById(R.id.previewText);
                preview.setVisibility(View.VISIBLE);
                preview.setText(incomingText);
            }

            if (incomingImage.length() > 0) {
                CardView card = findViewById(R.id.previewCard);
                card.setVisibility(View.VISIBLE);
                ImageView previewImage = findViewById(R.id.previewImage);
                Glide.with(getApplicationContext())
                        .load(incomingImage)
                        .into(previewImage);
            }
        }

        NavigationView navigationView = findViewById(R.id.postMenu);
        navigationView.setNavigationItemSelectedListener(this);


    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        // TODO create helper method, we have the same in ChannelActivity
        switch (item.getItemId()) {
            case R.id.createArticle:
                Intent CreateArticle = new Intent(getBaseContext(), ArticleActivity.class);
                if (incomingText != null && incomingText.length() > 0) {
                    CreateArticle.putExtra("incomingText", incomingText);
                }
                if (incomingImage != null && incomingImage.length() > 0) {
                    CreateArticle.putExtra("incomingImage", incomingImage);
                }
                startActivity(CreateArticle);
                break;
            case R.id.createNote:
                Intent CreateNote = new Intent(getBaseContext(), NoteActivity.class);
                if (incomingText != null && incomingText.length() > 0) {
                    CreateNote.putExtra("incomingText", incomingText);
                }
                if (incomingImage != null && incomingImage.length() > 0) {
                    CreateNote.putExtra("incomingImage", incomingImage);
                }
                startActivity(CreateNote);
                break;
            case R.id.createLike:
                Intent CreateLike = new Intent(getBaseContext(), LikeActivity.class);
                if (incomingText.length() > 0) {
                    CreateLike.putExtra("incomingText", incomingText);
                }
                startActivity(CreateLike);
                break;
            case R.id.createReply:
                Intent CreateReply = new Intent(getBaseContext(), ReplyActivity.class);
                if (incomingText.length() > 0) {
                    CreateReply.putExtra("incomingText", incomingText);
                }
                startActivity(CreateReply);
                break;
            case R.id.createBookmark:
                Intent CreateBookmark = new Intent(getBaseContext(), BookmarkActivity.class);
                if (incomingText.length() > 0) {
                    CreateBookmark.putExtra("incomingText", incomingText);
                }
                startActivity(CreateBookmark);
                break;
            case R.id.createRepost:
                Intent CreateRepost = new Intent(getBaseContext(), RepostActivity.class);
                if (incomingText.length() > 0) {
                    CreateRepost.putExtra("incomingText", incomingText);
                }
                startActivity(CreateRepost);
                break;
            case R.id.createEvent:
                Intent CreateEvent = new Intent(getBaseContext(), EventActivity.class);
                if (incomingText.length() > 0) {
                    CreateEvent.putExtra("incomingText", incomingText);
                }
                startActivity(CreateEvent);
                break;
            case R.id.createRSVP:
                Intent CreateRSVP = new Intent(getBaseContext(), RsvpActivity.class);
                if (incomingText.length() > 0) {
                    CreateRSVP.putExtra("incomingText", incomingText);
                }
                startActivity(CreateRSVP);
                break;
            case R.id.createIssue:
                Intent CreateIssue = new Intent(getBaseContext(), IssueActivity.class);
                if (incomingText.length() > 0) {
                    CreateIssue.putExtra("incomingText", incomingText);
                }
                startActivity(CreateIssue);
                break;
        }
        return false;
    }

}
