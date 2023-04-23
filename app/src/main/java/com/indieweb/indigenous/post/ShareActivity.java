package com.indieweb.indigenous.post;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import com.google.android.material.navigation.NavigationView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ScrollView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.General;
import com.indieweb.indigenous.GeneralFactory;
import com.indieweb.indigenous.LaunchActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.Accounts;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ShareActivity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    String incomingText = "";
    String incomingTitle = "";
    String incomingImage = "";
    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_share);

        user = new Accounts(this).getDefaultUser();
        if (!user.isValid()) {
            ScrollView layout = findViewById(R.id.share_root);
            Snackbar.make(layout, getString(R.string.no_user), Snackbar.LENGTH_LONG).show();
            return;
        }

        General general = GeneralFactory.getGeneral(user, null, ShareActivity.this);

        // Listen to incoming data.
        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        String action = intent.getAction();
        if (extras != null) {
            if (Intent.ACTION_SEND.equals(action)) {
                try {

                    if (extras.containsKey(Intent.EXTRA_SUBJECT)) {
                        incomingTitle = extras.get(Intent.EXTRA_SUBJECT).toString();
                    }
                    else if (extras.containsKey(Intent.EXTRA_TITLE)) {
                        incomingTitle = extras.get(Intent.EXTRA_TITLE).toString();
                    }

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
        if (incomingText.length() > 0 || incomingTitle.length() > 0 || incomingImage.length() > 0) {
            createTitle.setVisibility(View.VISIBLE);

            if (incomingTitle.length() > 0) {
                TextView previewTitle = findViewById(R.id.previewTitle);
                previewTitle.setVisibility(View.VISIBLE);
                previewTitle.setText(incomingTitle);
            }

            if (incomingText.length() > 0) {
                TextView previewText = findViewById(R.id.previewText);
                previewText.setVisibility(View.VISIBLE);
                previewText.setText(incomingText);
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

        Button goToMainApp = findViewById(R.id.goToMainApp);
        goToMainApp.setOnClickListener(new goToMainAppOnClickListener());

        NavigationView navigationView = findViewById(R.id.postMenu);
        navigationView.setNavigationItemSelectedListener(this);

        // Hide Media if micropub media endpoint is empty.
        String micropubMediaEndpoint = user.getMicropubMediaEndpoint();
        if (micropubMediaEndpoint == null || micropubMediaEndpoint.length() == 0) {
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.getItem(9);
            if (item != null) {
                item.setVisible(false);
            }
        }

        // Hide post types if configured.
        if (user.isAuthenticated() && general.hidePostTypes()) {

            ArrayList<Integer> protectedTypes = new ArrayList<>(general.getProtectedPostTypes());

            String postTypes = user.getPostTypes();

            ArrayList<String> postTypeList = new ArrayList<>();
            if (postTypes != null && postTypes.length() > 0) {
                try {
                    JSONObject object;
                    JSONArray itemList = new JSONArray(postTypes);

                    for (int i = 0; i < itemList.length(); i++) {
                        object = itemList.getJSONObject(i);
                        String type = object.getString("type");
                        postTypeList.add(type);
                    }

                }
                catch (JSONException ignored) { }
            }

            // Loop over menu items.
            Menu menu = navigationView.getMenu();
            for (int i = 0; i < menu.size(); i++){
                String menuType = "";
                Integer id = menu.getItem(i).getItemId();
                if (!protectedTypes.contains(id)) {
                    switch (id) {
                        case R.id.createNote:
                            menuType = "note";
                            break;
                        case R.id.createArticle:
                            menuType = "article";
                            break;
                        case R.id.createLike:
                            menuType = "like";
                            break;
                        case R.id.createBookmark:
                            menuType = "bookmark";
                            break;
                        case R.id.createReply:
                            menuType = "reply";
                            break;
                        case R.id.createRepost:
                            menuType = "repost";
                            break;
                        case R.id.createRead:
                            menuType = "read";
                            break;
                        case R.id.createEvent:
                            menuType = "event";
                            break;
                        case R.id.createRSVP:
                            menuType = "rsvp";
                            break;
                        case R.id.createIssue:
                            menuType = "issue";
                            break;
                        case R.id.createCheckin:
                            menuType = "checkin";
                            break;
                        case R.id.createVenue:
                            menuType = "venue";
                            break;
                        case R.id.createGeocache:
                            menuType = "geocache";
                            break;
                    }

                    if (!postTypeList.contains(menuType)) {
                        menu.getItem(i).setVisible(false);
                    }
                }
            }
        }

        Utility.setNightTheme(getApplicationContext());
    }

    // Go to main app.
    class goToMainAppOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Intent i = new Intent(getApplicationContext(), LaunchActivity.class);
            startActivity(i);
            finish();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case R.id.createArticle:
                Intent CreateArticle = new Intent(getBaseContext(), ArticleActivity.class);
                if (incomingTitle != null && incomingTitle.length() > 0) {
                    CreateArticle.putExtra("incomingTitle", incomingTitle);
                }
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
                if (incomingTitle != null && incomingTitle.length() > 0) {
                    CreateLike.putExtra("incomingTitle", incomingTitle);
                }
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
                if (incomingTitle != null && incomingTitle.length() > 0) {
                    CreateBookmark.putExtra("incomingTitle", incomingTitle);
                }
                if (incomingText.length() > 0) {
                    CreateBookmark.putExtra("incomingText", incomingText);
                }
                startActivity(CreateBookmark);
                break;
            case R.id.createRepost:
                Intent CreateRepost = new Intent(getBaseContext(), RepostActivity.class);
                if (incomingTitle != null && incomingTitle.length() > 0) {
                    CreateRepost.putExtra("incomingTitle", incomingTitle);
                }
                if (incomingText.length() > 0) {
                    CreateRepost.putExtra("incomingText", incomingText);
                }
                startActivity(CreateRepost);
                break;
            case R.id.createEvent:
                Intent CreateEvent = new Intent(getBaseContext(), EventActivity.class);
                if (incomingTitle != null && incomingTitle.length() > 0) {
                    CreateEvent.putExtra("incomingTitle", incomingTitle);
                }
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
            case R.id.createRead:
                Intent CreateRead = new Intent(getBaseContext(), ReadActivity.class);
                if (incomingText.length() > 0) {
                    CreateRead.putExtra("incomingText", incomingText);
                }
                startActivity(CreateRead);
                break;
            case R.id.createIssue:
                Intent CreateIssue = new Intent(getBaseContext(), IssueActivity.class);
                if (incomingText.length() > 0) {
                    CreateIssue.putExtra("incomingText", incomingText);
                }
                startActivity(CreateIssue);
                break;
            case R.id.createMedia:
                Intent CreateMedia = new Intent(getBaseContext(), UploadActivity.class);
                if (incomingImage != null && incomingImage.length() > 0) {
                    CreateMedia.putExtra("incomingImage", incomingImage);
                }
                startActivity(CreateMedia);
                break;
            case R.id.createCheckin:
                Intent CreateCheckin = new Intent(getBaseContext(), CheckinActivity.class);
                if (incomingText.length() > 0) {
                    CreateCheckin.putExtra("incomingText", incomingText);
                }
                startActivity(CreateCheckin);
                break;
            case R.id.createVenue:
                Intent CreateVenue = new Intent(getBaseContext(), VenueActivity.class);
                if (incomingTitle != null && incomingTitle.length() > 0) {
                    CreateVenue.putExtra("incomingTitle", incomingTitle);
                }
                if (incomingText.length() > 0) {
                    CreateVenue.putExtra("incomingText", incomingText);
                }
                startActivity(CreateVenue);
                break;
            case R.id.createGeocache:
                Intent CreateGeocache = new Intent(getBaseContext(), GeocacheActivity.class);
                if (incomingText.length() > 0) {
                    CreateGeocache.putExtra("incomingText", incomingText);
                }
                startActivity(CreateGeocache);
                break;
        }
        return false;
    }

}
