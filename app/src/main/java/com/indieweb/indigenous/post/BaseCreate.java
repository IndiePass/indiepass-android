package com.indieweb.indigenous.post;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;

import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputLayout;

import android.text.TextUtils;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.model.Syndication;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.Accounts;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static com.indieweb.indigenous.MainActivity.RESULT_DRAFT_SAVED;

@SuppressLint("Registered")
abstract public class BaseCreate extends BasePlatformCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get default user.
        user = new Accounts(this).getDefaultUser();

        // Post class.
        post = PostFactory.getPost(user, getApplicationContext());

        // Account wrapper.
        accountPostWrapper = findViewById(R.id.accountPostWrapper);
        accountPost = findViewById(R.id.accountPost);
        if (accountPostWrapper != null && new Accounts(this).getCount() > 1) {

            final List<String> accounts = new ArrayList<>();
            final List<User> users = new Accounts(BaseCreate.this).getAllUsers();
            for (User user: users) {
                accounts.add(user.getDisplayName());
            }

            // Check if an account is passed in.
            Bundle extras = getIntent().getExtras();
            if (extras != null) {
                String incomingAccount = extras.getString("account");
                if (incomingAccount != null && !incomingAccount.equals(user.getAccountNameWithoutProtocol())) {
                    User incomingUser = new Accounts(getApplicationContext()).getUser(incomingAccount, true, true);
                    if (incomingUser != null) {
                        user = incomingUser;
                        post = PostFactory.getPost(user, getApplicationContext());
                    }
                }
            }

            accountPostWrapper.setVisibility(View.VISIBLE);
            setAccountPostInfo(user.getDisplayName());
            accountPost.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final CharSequence[] accountItems = accounts.toArray(new CharSequence[0]);
                    final AlertDialog.Builder builder = new AlertDialog.Builder(BaseCreate.this);
                    builder.setTitle(getString(R.string.account_select_to_post));
                    builder.setCancelable(true);
                    builder.setItems(accountItems, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int index) {
                            user = new Accounts(getApplicationContext()).getUser(accounts.get(index), false, false);
                            post = PostFactory.getPost(user, getApplicationContext());
                            invalidateOptionsMenu();
                            setAccountPostInfo(user.getDisplayName());
                            prepareFields();
                        }
                    });
                    builder.show();
                }
            });
        }

        // Get a couple elements for requirement checks or pre-population.
        body = findViewById(R.id.body);
        title = findViewById(R.id.title);
        spoiler = findViewById(R.id.spoiler);
        url = findViewById(R.id.url);
        postStatus = findViewById(R.id.postStatus);
        tags = findViewById(R.id.tags);

        visibility = findViewById(R.id.postVisibility);
        if (visibility != null && Preferences.getPreference(getApplicationContext(), "pref_key_post_visibility", false)) {
            visibility.setVisibility(View.VISIBLE);
        }

        sensitivity = findViewById(R.id.postSensitivity);
        if (sensitivity != null && post.supports(Post.FEATURE_POST_SENSITIVITY)) {
            sensitivity.setVisibility(View.VISIBLE);
        }

        saveAsDraft = findViewById(R.id.saveAsDraft);
        publishDate = findViewById(R.id.publishDate);
        mediaPreviewGallery = findViewById(R.id.mediaPreviewGallery);
        locationWrapper = findViewById(R.id.locationWrapper);
        locationVisibility = findViewById(R.id.locationVisibility);
        locationName = findViewById(R.id.locationName);
        locationUrl = findViewById(R.id.locationUrl);
        locationQuery = findViewById(R.id.locationQuery);
        locationCoordinates = findViewById(R.id.locationCoordinates);
        layout = findViewById(R.id.post_root);

        // Add listener on body.
        if (body != null) {
            body.addTextChangedListener(BaseCreate.this);
        }

        // On checkin, set label and url visible already.
        if (isCheckin) {
            toggleLocationVisibilities(true);
        }

        // Publish date.
        if (publishDate != null) {
            publishDate.setOnClickListener(new publishDateOnClickListener());
        }

        if (isMediaRequest) {
            mediaUrl = findViewById(R.id.mediaUrl);
        }

        // Add counter.
        if (addCounter) {
            TextInputLayout textInputLayout = findViewById(R.id.textInputLayout);
            textInputLayout.setCounterEnabled(true);
        }

        // Prepare fields.
        prepareFields();

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action)) {
                try {

                    // Text
                    if (extras.containsKey(Intent.EXTRA_TEXT)) {
                        String incomingData = extras.get(Intent.EXTRA_TEXT).toString();
                        if (incomingData.length() > 0) {
                            if (url != null) {

                                if (extras.containsKey(Intent.EXTRA_SUBJECT)) {
                                    try {
                                        String incomingTitle = extras.get(Intent.EXTRA_SUBJECT).toString();
                                        if (incomingTitle.length() > 0 && title != null) {
                                            title.setText(incomingTitle);
                                        }
                                    }
                                    catch (NullPointerException ignored) { }
                                }
                                else if (extras.containsKey(Intent.EXTRA_TITLE)) {
                                    try {
                                        String incomingTitle = extras.get(Intent.EXTRA_TITLE).toString();
                                        if (incomingTitle.length() > 0 && title != null) {
                                            title.setText(incomingTitle);
                                        }
                                    }
                                    catch (NullPointerException ignored) { }
                                }

                                setUrlAndFocusOnMessage(incomingData);
                                if (autoSubmit.length() > 0) {
                                    if (Preferences.getPreference(this, autoSubmit, false)) {
                                        if (new Accounts(this).getCount() > 1) {
                                            final List<String> accounts = new ArrayList<>();
                                            final List<User> users = new Accounts(BaseCreate.this).getAllUsers();
                                            for (User user: users) {
                                                accounts.add(user.getDisplayName());
                                            }
                                            final CharSequence[] accountItems = accounts.toArray(new CharSequence[0]);
                                            final AlertDialog.Builder builder = new AlertDialog.Builder(this);
                                            builder.setTitle(getString(R.string.account_select_to_post));
                                            builder.setCancelable(false);
                                            builder.setItems(accountItems, new DialogInterface.OnClickListener() {
                                                @Override
                                                public void onClick(DialogInterface dialog, int index) {
                                                    user = new Accounts(getApplicationContext()).getUser(accounts.get(index), false, false);
                                                    sendBasePost(null);
                                                }
                                            });
                                            builder.show();
                                        }
                                        else {
                                            sendBasePost(null);
                                        }
                                    }
                                }
                            }
                        }
                    }

                    // Stream
                    if (isMediaRequest && mediaPreviewGallery != null && extras.containsKey(Intent.EXTRA_STREAM)) {
                        setChanges(true);
                        String incomingData = extras.get(Intent.EXTRA_STREAM).toString();
                        image.add(Uri.parse(incomingData));
                        caption.add("");
                        prepareImagePreview();
                    }

                }
                catch (NullPointerException ignored) { }
            }
            else {
                String incomingText = extras.getString("incomingText");
                if (incomingText != null && incomingText.length() > 0 && (body != null || url != null || locationUrl != null)) {
                    setChanges(true);
                    if (isCheckin && locationUrl != null) {
                        setUrlAndFocusOnMessage(incomingText);
                    }
                    else if (url != null) {
                        setUrlAndFocusOnMessage(incomingText);
                    }
                    else {
                        body.setText(incomingText);
                    }
                }

                String incomingTitle = extras.getString("incomingTitle");
                if (incomingTitle != null && incomingTitle.length() > 0 && title != null) {
                    setChanges(true);
                    title.setText(incomingTitle);
                }

                boolean testIncoming = extras.getBoolean("indigenousTesting");
                if (testIncoming) {
                    isTesting = true;
                }
            }

            if (canAddMedia) {
                String incomingImage = extras.getString("incomingImage");
                if (incomingImage != null && incomingImage.length() > 0 && mediaPreviewGallery != null) {
                    setChanges(true);
                    image.add(Uri.parse(incomingImage));
                    caption.add("");
                    prepareImagePreview();
                }
            }

            // Draft support.
            draftId = extras.getInt("draftId");
            if (draftId > 0) {
                setChanges(true);
                preparedDraft = true;
                prepareDraft(draftId);
            }
        }
    }

    /**
     * Prepare fields: hides or shows fields, add autocomplete listeners, etc.
     */
    void prepareFields() {

        setSyndicationTargets();

        // Title visibility.
        if (title != null) {
            if (!post.supports(Post.FEATURE_TITLE)) {
                title.setVisibility(View.GONE);
            }
            else {
                title.setVisibility(View.VISIBLE);
            }
        }

        // Post status visibility.
        if (postStatus != null) {
            if (!post.supports(Post.FEATURE_POST_STATUS)) {
                postStatus.setVisibility(View.GONE);
            }
            else {
                postStatus.setVisibility(View.VISIBLE);
            }
        }

        // Tags visibility.
        if (tags != null) {
            if (!post.supports(Post.FEATURE_CATEGORIES)) {
                tags.setVisibility(View.GONE);
            }
            else {
                tags.setVisibility(View.VISIBLE);
            }
        }

        // URL visibility.
        if (url != null) {
            if (post.hideUrlField()) {
                url.setVisibility(View.GONE);
            }
            else {
                url.setVisibility(View.VISIBLE);
            }
        }

        // Spoiler.
        if (spoiler != null) {
            LinearLayout spoilerWrapper = findViewById(R.id.spoilerWrapper);
            if (post.supports(Post.FEATURE_SPOILER)) {
                spoilerWrapper.setVisibility(View.VISIBLE);
            }
            else {
                spoilerWrapper.setVisibility(View.GONE);
            }
        }

        // Autocomplete of contacts.
        if (body != null && post.supports(Post.FEATURE_CATEGORIES) && user.isAuthenticated()) {
            post.prepareContactsAutocomplete(body);
        }

        // Autocomplete of tags.
        if (tags != null && post.supports(Post.FEATURE_CATEGORIES) && user.isAuthenticated()) {
            post.prepareTagsAutocomplete(tags);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CHECK_LOCATION_SETTINGS && resultCode == RESULT_OK) {
            startLocationUpdates();
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == R.id.addLocation) {
            if (!mRequestingLocationUpdates) {
                startLocationUpdates();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Set the syndication targets.
     */
    public void setSyndicationTargets() {
        LinearLayout syndicationLayout = findViewById(R.id.syndicationTargets);
        String syndicationTargetsString = user.getSyndicationTargets();
        if (syndicationLayout != null) {
            syndicationLayout.removeAllViews();
            syndicationTargets.clear();
            if (syndicationTargetsString.length() > 0) {
                JSONObject object;
                try {
                    JSONArray itemList = new JSONArray(syndicationTargetsString);

                    if (itemList.length() > 0) {
                        TextView syn = new TextView(this);
                        syn.setText(R.string.syndicate_to);
                        syn.setPadding(20, 10, 0, 0);
                        syn.setTextSize(15);
                        syn.setTextColor(getResources().getColor(R.color.textColor));
                        syndicationLayout.addView(syn);
                        syndicationLayout.setPadding(10, 0,0, 0 );
                    }

                    for (int i = 0; i < itemList.length(); i++) {
                        object = itemList.getJSONObject(i);
                        Syndication syndication = new Syndication();
                        syndication.setUid(object.getString("uid"));
                        syndication.setName(object.getString("name"));
                        if (object.has("checked")) {
                            syndication.setChecked(object.getBoolean("checked"));
                        }
                        syndicationTargets.add(syndication);

                        CheckBox ch = new CheckBox(this);
                        ch.setText(syndication.getName());
                        ch.setId(i);
                        ch.setTextSize(15);
                        ch.setPadding(0, 10, 0, 10);
                        ch.setTextColor(getResources().getColor(R.color.textColor));
                        if (syndication.isChecked()) {
                            ch.setChecked(true);
                        }
                        syndicationLayout.addView(ch);
                    }

                }
                catch (JSONException e) {
                    String message = String.format(getString(R.string.syndication_targets_parse_error), e.getMessage());
                    final Snackbar snack = Snackbar.make(layout, message, Snackbar.LENGTH_INDEFINITE);
                    snack.setAction(getString(R.string.close), new View.OnClickListener() {
                                @Override
                                public void onClick(View v) {
                                    snack.dismiss();
                                }
                            }
                    );
                    snack.show();
                }
            }
        }
    }

    /**
     * Set account posting info.
     *
     * @param userName
     *   The user who is posting.
     */
    public void setAccountPostInfo(@NonNull String userName) {
        accountPost.setText(String.format(getString(R.string.account_post_as), Utility.stripEndingSlash(userName)));
    }

    /**
     * Prepares the activity with the draft.
     *
     * @param draftId
     *   The draft id.
     */
    public void prepareDraft(Integer draftId) {
        db = new DatabaseHelper(this);

        Draft draft = db.getDraft(draftId);
        if (draft.getId() > 0) {

            // Set as checked again to avoid confusion.
            saveAsDraft.setChecked(true);

            // Post status.
            if (postStatus != null) {
                postStatus.setChecked(draft.getPublished() == 1);
            }

            // Visibility.
            if (visibility != null && draft.getVisibility().length() > 0) {
                int visibilitySelection = 0;
                switch (draft.getVisibility()) {
                    case "private":
                        visibilitySelection = 1;
                        break;
                    case "unlisted":
                        visibilitySelection = 2;
                        break;
                }
                visibility.setSelection(visibilitySelection);
            }

            // Sensitivity
            if (sensitivity != null && draft.getSensitivity() == 1) {
                sensitivity.setChecked(true);
            }

            // Name.
            if (title != null && draft.getName().length() > 0) {
                title.setText(draft.getName());
            }

            // Body.
            if (body != null && draft.getBody().length() > 0) {
                body.setText(draft.getBody());
            }

            // Spoiler.
            if (spoiler != null && draft.getSpoiler().length() > 0) {
                spoiler.setText(draft.getSpoiler());
            }

            // Tags.
            if (tags != null && draft.getTags().length() > 0) {
                tags.setText(draft.getTags());
            }

            // Url.
            if (url != null && draft.getUrl().length() > 0) {
                url.setText(draft.getUrl());
            }

            // Publish date.
            if (publishDate != null && draft.getPublishDate().length() > 0) {
                publishDate.setText(draft.getPublishDate());
            }

            // Start date.
            if (startDate != null && draft.getStartDate().length() > 0) {
                startDate.setText(draft.getStartDate());
            }

            // End date.
            if (endDate != null && draft.getEndDate().length() > 0) {
                endDate.setText(draft.getEndDate());
            }

            // RSVP.
            if (rsvp != null && draft.getSpinner().length() > 0) {
                int rsvpSelection = 0;
                switch (draft.getSpinner()) {
                    case "no":
                        rsvpSelection = 1;
                        break;
                    case "maybe":
                        rsvpSelection = 2;
                        break;
                    case "interested":
                        rsvpSelection = 3;
                        break;
                }
                rsvp.setSelection(rsvpSelection);
            }

            // Read.
            if (read != null && draft.getSpinner().length() > 0) {
                int readSelection = 0;
                switch (draft.getSpinner()) {
                    case "to-read":
                        readSelection = 1;
                        break;
                    case "reading":
                        readSelection = 2;
                        break;
                    case "finished":
                        readSelection = 3;
                        break;
                }
                read.setSelection(readSelection);
            }

            // Geocache log type.
            if (geocacheLogType != null && draft.getSpinner().length() > 0) {
                int logType = draft.getSpinner().equals("found") ? 0 : 1;
                geocacheLogType.setSelection(logType);
            }

            // Location coordinates.
            if (locationCoordinates != null && draft.getCoordinates().length() > 0) {
                coordinates = draft.getCoordinates();
                String coordinatesText = String.format(getString(R.string.location_coordinates), coordinates);
                locationCoordinates.setText(coordinatesText);
                toggleLocationVisibilities(true);

                int cc = 0;
                String[] coords = draft.getCoordinates().split(",");
                for (String c : coords) {
                    if (c != null && c.length() > 0) {
                        switch (cc) {
                            case 0:
                                latitude = Double.parseDouble(c);
                                break;
                            case 1:
                                longitude = Double.parseDouble(c);
                                break;
                        }
                    }
                    cc++;
                }
            }

            // Location name.
            if (locationName != null && draft.getLocationName().length() > 0) {
                locationName.setText(draft.getLocationName());
            }

            // Location url.
            if (locationUrl != null && draft.getLocationUrl().length() > 0) {
                locationUrl.setText(draft.getLocationUrl());
            }

            // Location visibility.
            if (locationVisibility != null && draft.getLocationVisibility().length() > 0) {
                int visibility = 0;
                if (draft.getLocationVisibility().equals("private")) {
                    visibility = 1;
                }
                else if (draft.getLocationVisibility().equals("protected")) {
                    visibility = 2;
                }

                locationVisibility.setSelection(visibility);
            }

            // Syndication targets.
            if (syndicationTargets.size() > 0) {
                CheckBox checkbox;
                String[] syndication = draft.getSyndicationTargets().split(";");
                for (String s : syndication) {
                    if (s != null && s.length() > 0) {
                        for (int j = 0; j < syndicationTargets.size(); j++) {
                            if (syndicationTargets.get(j).getUid().equals(s)) {
                                checkbox = findViewById(j);
                                if (checkbox != null) {
                                    checkbox.setChecked(true);
                                }
                            }
                        }
                    }
                }
            }

            // Images.
            if (canAddMedia && draft.getImage().length() > 0) {

                String[] uris = draft.getImage().split(";");
                for (String uri : uris) {
                    image.add(Uri.parse(uri));
                }

                String[] captionsList = draft.getCaption().split(";");
                caption.addAll(Arrays.asList(captionsList));
                int index = 0;
                for (String c: caption) {
                    if (c.equals(EMPTY_CAPTION)) {
                        caption.set(index, "");
                    }
                    index++;
                }

                prepareImagePreview();
            }

            // Video
            if (canAddMedia && draft.getVideo().length() > 0) {

                String[] uris = draft.getVideo().split(";");
                for (String uri : uris) {
                    video.add(Uri.parse(uri));
                }

                prepareVideoPreview();
            }

            // Audio
            if (canAddMedia && draft.getAudio().length() > 0) {

                String[] uris = draft.getAudio().split(";");
                for (String uri : uris) {
                    audio.add(Uri.parse(uri));
                }

                prepareAudioPreview();
            }
        }
    }

    /**
     * Save draft.
     */
    public void saveDraft(String type, Draft draft) {

        if (draft == null) {
            draft = new Draft();
        }

        if (draftId != null && draftId > 0) {
            draft.setId(draftId);
        }

        draft.setType(type);
        draft.setAccount(user.getAccountNameWithoutProtocol());

        if (title != null && !TextUtils.isEmpty(title.getText())) {
            draft.setName(title.getText().toString());
        }

        if (body != null && !TextUtils.isEmpty(body.getText())) {
            draft.setBody(body.getText().toString());
        }

        if (tags != null && !TextUtils.isEmpty(tags.getText())) {
            draft.setTags(tags.getText().toString());
        }

        if (url != null && !TextUtils.isEmpty(url.getText())) {
            draft.setUrl(url.getText().toString());
        }

        if (postStatus != null) {
            draft.setPublished(postStatus.isChecked() ? 1 : 0);
        }

        if (publishDate != null && !TextUtils.isEmpty(publishDate.getText())) {
            draft.setPublishDate(publishDate.getText().toString());
        }

        if (locationUrl != null && !TextUtils.isEmpty(locationUrl.getText())) {
            draft.setLocationUrl(locationUrl.getText().toString());
        }

        if (locationName != null && !TextUtils.isEmpty(locationName.getText())) {
            draft.setLocationName(locationName.getText().toString());
        }

        if (locationVisibility != null) {
            draft.setLocationVisibility(locationVisibility.getSelectedItem().toString());
        }

        if (coordinates != null) {
            draft.setCoordinates(coordinates);
        }

        if (visibility != null) {
            draft.setVisibility(visibility.getSelectedItem().toString());
        }

        if (sensitivity != null) {
            draft.setSensitivity(sensitivity.isChecked() ? 1 : 0);
        }

        if (spoiler != null) {
            draft.setSpoiler(spoiler.getText().toString());
        }

        if (syndicationTargets.size() > 0) {
            CheckBox checkbox;
            StringBuilder syndication = new StringBuilder();
            for (int j = 0; j < syndicationTargets.size(); j++) {
                checkbox = findViewById(j);
                if (checkbox != null && checkbox.isChecked()) {
                    syndication.append(syndicationTargets.get(j).getUid()).append(";");
                }
            }

            if (syndication.length() > 0) {
                draft.setSyndicationTargets(syndication.toString());
            }
        }

        if (image.size() > 0) {
            int index = 0;
            StringBuilder images = new StringBuilder();
            StringBuilder captions = new StringBuilder();
            for (Uri uri : this.image) {
                if (uri != null && uri.toString().length() > 0) {
                    images.append(uri).append(";");
                    String imageCaption = this.caption.get(index);
                    if (imageCaption.length() == 0) {
                        imageCaption = EMPTY_CAPTION;
                    }
                    captions.append(imageCaption).append(";");
                }
                index++;
            }
            draft.setImage(images.toString());
            draft.setCaption(captions.toString());
        }

        if (video.size() > 0) {
            StringBuilder video = new StringBuilder();
            for (Uri uri : this.video) {
                if (uri != null && uri.toString().length() > 0) {
                    video.append(uri).append(";");
                }
            }
            draft.setVideo(video.toString());
        }

        if (audio.size() > 0) {
            StringBuilder audio = new StringBuilder();
            for (Uri uri : this.audio) {
                if (uri != null && uri.toString().length() > 0) {
                    audio.append(uri).append(";");
                }
            }
            draft.setAudio(audio.toString());
        }

        db = new DatabaseHelper(this);
        db.saveDraft(draft);

        Intent returnIntent = new Intent();
        setResult(RESULT_DRAFT_SAVED, returnIntent);
        finish();
    }

}