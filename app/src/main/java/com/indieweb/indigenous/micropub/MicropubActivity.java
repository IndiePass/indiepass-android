package com.indieweb.indigenous.micropub;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.indieweb.indigenous.AboutActivity;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.SettingsActivity;
import com.indieweb.indigenous.indieauth.Endpoints;
import com.indieweb.indigenous.indieauth.IndieAuthActivity;
import com.indieweb.indigenous.micropub.draft.DraftActivity;
import com.indieweb.indigenous.micropub.post.ArticleActivity;
import com.indieweb.indigenous.micropub.post.BookmarkActivity;
import com.indieweb.indigenous.micropub.post.EventActivity;
import com.indieweb.indigenous.micropub.post.IssueActivity;
import com.indieweb.indigenous.micropub.post.LikeActivity;
import com.indieweb.indigenous.micropub.post.UploadActivity;
import com.indieweb.indigenous.micropub.post.NoteActivity;
import com.indieweb.indigenous.micropub.post.ReplyActivity;
import com.indieweb.indigenous.micropub.post.RepostActivity;
import com.indieweb.indigenous.micropub.post.RsvpActivity;
import com.indieweb.indigenous.micropub.post.UpdateActivity;
import com.indieweb.indigenous.micropub.source.PostListActivity;
import com.indieweb.indigenous.microsub.channel.ChannelActivity;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

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

        String microsubEndpoint = user.getMicrosubEndpoint();
        if (microsubEndpoint != null && microsubEndpoint.length() > 0) {
            Button goToReader = findViewById(R.id.goToReader);
            goToReader.setVisibility(View.VISIBLE);
            goToReader.setOnClickListener(new goToReaderOnClickListener());
        }

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

        // Hide Update if setting is not enabled.
        if (!Preferences.getPreference(this, "pref_key_source_update", false)) {
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.getItem(10);
            if (item != null) {
                item.setVisible(false);
            }
        }

        // Hide Posts if setting is not enabled.
        if (!Preferences.getPreference(this, "pref_key_source_post_list", false)) {
            Menu menu = navigationView.getMenu();
            MenuItem item = menu.getItem(11);
            if (item != null) {
                item.setVisible(false);
            }
        }

        // Hide post types if configured.
        if (Preferences.getPreference(this, "pref_key_post_type_hide", false)) {

            ArrayList<Integer> protectedTypes = new ArrayList<>();
            protectedTypes.add(R.id.createMedia);
            protectedTypes.add(R.id.updatePost);
            protectedTypes.add(R.id.sourcePostList);

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
                        case R.id.createEvent:
                            menuType = "event";
                            break;
                        case R.id.createRSVP:
                            menuType = "rsvp";
                            break;
                        case R.id.createIssue:
                            menuType = "issue";
                            break;
                    }

                    if (!postTypeList.contains(menuType)) {
                        menu.getItem(i).setVisible(false);
                    }
                }
            }
        }

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
            case R.id.updatePost:
                Intent UpdatePost = new Intent(getBaseContext(), UpdateActivity.class);
                if (incomingText.length() > 0) {
                    UpdatePost.putExtra("incomingText", incomingText);
                }
                startActivity(UpdatePost);
                break;
            case R.id.sourcePostList:
                Intent SourcePostList = new Intent(getBaseContext(), PostListActivity.class);
                startActivity(SourcePostList);
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
            case R.id.drafts:
                Intent goDraft = new Intent(getBaseContext(), DraftActivity.class);
                startActivity(goDraft);
                return true;

            case R.id.refreshConfiguration:
                new MicropubConfig(getApplicationContext(), new Accounts(this).getCurrentUser()).refresh();
                new Endpoints(getApplicationContext(), user).refresh();
                return true;

            case R.id.settings:
                Intent goSettings = new Intent(getBaseContext(), SettingsActivity.class);
                startActivity(goSettings);
                return true;

            case R.id.about:
                Intent goAbout = new Intent(getBaseContext(), AboutActivity.class);
                startActivity(goAbout);
                return true;

            case R.id.accounts:
                new Accounts(this).switchAccount(this);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
