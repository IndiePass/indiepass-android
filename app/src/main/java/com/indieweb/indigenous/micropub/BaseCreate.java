package com.indieweb.indigenous.micropub;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.text.TextUtils;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.micropub.post.BasePlatformCreate;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.model.Syndication;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Preferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

@SuppressLint("Registered")
abstract public class BaseCreate extends BasePlatformCreate {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Get current user.
        user = new Accounts(this).getCurrentUser();

        // Syndication targets.
        LinearLayout syndicationLayout = findViewById(R.id.syndicationTargets);
        String syndicationTargetsString = user.getSyndicationTargets();
        if (syndicationLayout != null && syndicationTargetsString.length() > 0) {
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
                    syndicationTargets.add(syndication);

                    CheckBox ch = new CheckBox(this);
                    ch.setText(syndication.getName());
                    ch.setId(i);
                    ch.setTextSize(15);
                    ch.setPadding(0, 10, 0, 10);
                    ch.setTextColor(getResources().getColor(R.color.textColor));
                    syndicationLayout.addView(ch);
                }

            } catch (JSONException e) {
                Toast.makeText(this, "Error parsing syndication targets: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        // Get a couple elements for requirement checks or pre-population.
        title = findViewById(R.id.title);
        body = findViewById(R.id.body);
        url = findViewById(R.id.url);
        tags = findViewById(R.id.tags);
        saveAsDraft = findViewById(R.id.saveAsDraft);
        publishDate = findViewById(R.id.publishDate);
        imagePreviewGallery = findViewById(R.id.imagePreviewGallery);
        locationWrapper = findViewById(R.id.locationWrapper);
        locationVisibility = findViewById(R.id.locationVisibility);
        locationName = findViewById(R.id.locationName);
        locationUrl = findViewById(R.id.locationUrl);
        locationQuery = findViewById(R.id.locationQuery);
        locationCoordinates = findViewById(R.id.locationCoordinates);

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

        // Autocomplete of tags.
        if (tags != null && Preferences.getPreference(this, "pref_key_tags_list", false)) {
            new MicropubAction(getApplicationContext(), user).getTagsList(tags);
        }

        if (isMediaRequest) {
            mediaUrl = findViewById(R.id.mediaUrl);
        }

        // Add counter.
        if (addCounter) {
            TextInputLayout textInputLayout = findViewById(R.id.textInputLayout);
            textInputLayout.setCounterEnabled(true);
        }

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {

            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action)) {
                try {

                    // Text
                    if (extras.containsKey(Intent.EXTRA_TEXT)) {
                        String incomingData = extras.get(Intent.EXTRA_TEXT).toString();
                        if (incomingData != null && incomingData.length() > 0) {
                            if (url != null) {
                                setUrlAndFocusOnMessage(incomingData);
                                if (directSend.length() > 0) {
                                    if (Preferences.getPreference(this, directSend, false)) {
                                        sendBasePost(null);
                                    }
                                }
                            }
                        }
                    }

                    // Stream
                    if (isMediaRequest && imagePreviewGallery != null && extras.containsKey(Intent.EXTRA_STREAM)) {
                        setChanges(true);
                        String incomingData = extras.get(Intent.EXTRA_STREAM).toString();
                        images.add(Uri.parse(incomingData));
                        captions.add("");
                        prepareImagePreview();
                    }

                }
                catch (NullPointerException ignored) { }
            }
            else {
                String incomingText = extras.getString("incomingText");
                if (incomingText != null && incomingText.length() > 0 && (body != null || url != null || locationUrl != null)) {
                    setChanges(true);
                    if (locationUrl != null) {
                        setUrlAndFocusOnMessage(incomingText);
                    }
                    else if (url != null) {
                        setUrlAndFocusOnMessage(incomingText);
                    }
                    else {
                        body.setText(incomingText);
                    }
                }

                boolean testIncoming = extras.getBoolean("indigenousTesting");
                if (testIncoming) {
                    isTesting = true;
                }
            }

            if (canAddImage) {
                String incomingImage = extras.getString("incomingImage");
                if (incomingImage != null && incomingImage.length() > 0 && imagePreviewGallery != null) {
                    setChanges(true);
                    images.add(Uri.parse(incomingImage));
                    captions.add("");
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

            // Name.
            if (title != null && draft.getName().length() > 0) {
                title.setText(draft.getName());
            }

            // Body.
            if (body != null && draft.getBody().length() > 0) {
                body.setText(draft.getBody());
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

            // Geocache log type.
            if (geocacheLogType != null && draft.getSpinner().length() > 0) {
                int logType = draft.getSpinner().equals("found") ? 0 : 1;
                geocacheLogType.setSelection(logType);
            }

            // Location coordinates.
            if (locationCoordinates != null && draft.getCoordinates().length() > 0) {
                coordinates = draft.getCoordinates();
                String coordinatesText = "Coordinates (lat, lon, alt) " + coordinates;
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
            if (canAddImage && draft.getImages().length() > 0) {

                String[] uris = draft.getImages().split(";");
                for (String uri : uris) {
                    images.add(Uri.parse(uri));
                }

                String[] captionsList = draft.getCaptions().split(";");
                captions.addAll(Arrays.asList(captionsList));

                prepareImagePreview();
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
        draft.setAccount(user.getMeWithoutProtocol());

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

        if (images.size() > 0) {
            int index = 0;
            StringBuilder images = new StringBuilder();
            StringBuilder captions = new StringBuilder();
            for (Uri uri : this.images) {
                if (uri != null && uri.toString().length() > 0) {
                    images.append(uri.toString()).append(";");
                    captions.append(this.captions.get(index)).append(";");
                }
                index++;
            }
            draft.setImages(images.toString());
            draft.setCaptions(captions.toString());
        }

        db = new DatabaseHelper(this);
        db.saveDraft(draft);

        Toast.makeText(this, getString(R.string.draft_saved), Toast.LENGTH_SHORT).show();
        finish();
    }

}