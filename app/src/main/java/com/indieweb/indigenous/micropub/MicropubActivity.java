package com.indieweb.indigenous.micropub;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.indieweb.indigenous.MainActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.micropub.post.ArticleActivity;
import com.indieweb.indigenous.micropub.post.BookmarkActivity;
import com.indieweb.indigenous.micropub.post.EventActivity;
import com.indieweb.indigenous.micropub.post.LikeActivity;
import com.indieweb.indigenous.micropub.post.NoteActivity;
import com.indieweb.indigenous.micropub.post.ReplyActivity;
import com.indieweb.indigenous.micropub.post.RepostActivity;
import com.indieweb.indigenous.micropub.post.RsvpActivity;
import com.indieweb.indigenous.microsub.channel.ChannelActivity;
import com.indieweb.indigenous.util.Syndications;

public class MicropubActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String incomingText = "";
    String incomingImage = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_micropub);

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

        SharedPreferences preferences = getSharedPreferences("indigenous", MODE_PRIVATE);
        String MicroSubEndpoint = preferences.getString("microsub_endpoint", "");

        if (MicroSubEndpoint.length() > 0) {
            Button goToReader = findViewById(R.id.goToReader);
            goToReader.setVisibility(View.VISIBLE);
            goToReader.setOnClickListener(new goToReaderOnClickListener());
        }

        // TODO this is ugly for now, but will do.
        NavigationView navigationView = findViewById(R.id.postMenu);
        navigationView.setNavigationItemSelectedListener(this);
    }

    // Go to reader.
    class goToReaderOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(getApplicationContext(), ChannelActivity.class);
            startActivity(i);
            finish();
        }
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
        }
        return false;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // TODO create helper method as we have the same in ChannelsActivity
            case R.id.logout:
                new AlertDialog.Builder(this)
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .setTitle("Log out")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                                // Remove shared preferences.
                                SharedPreferences.Editor editor = getSharedPreferences("indigenous", MODE_PRIVATE).edit();
                                editor.clear().apply();

                                Intent main = new Intent(getBaseContext(), MainActivity.class);
                                startActivity(main);
                                finish();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;

            case R.id.refreshSyndications:
                new Syndications(getApplicationContext()).refresh();
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
