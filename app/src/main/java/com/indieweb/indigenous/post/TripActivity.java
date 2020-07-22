package com.indieweb.indigenous.post;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.util.Utility;

import org.xmlpull.v1.XmlPullParserException;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import io.ticofab.androidgpxparser.parser.GPXParser;
import io.ticofab.androidgpxparser.parser.domain.Gpx;
import io.ticofab.androidgpxparser.parser.domain.Track;
import io.ticofab.androidgpxparser.parser.domain.TrackPoint;
import io.ticofab.androidgpxparser.parser.domain.TrackSegment;

public class TripActivity extends BaseCreate {

    private TextView pointsInfo;
    private final List<String> points = new ArrayList<>();
    private final int PICK_GPX_REQUEST = 30;
    private EditText cost;
    private EditText distance;
    private EditText duration;
    private Spinner transport;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        addCounter = true;
        canAddMedia = false;
        canAddLocation = false;

        setContentView(R.layout.activity_trip);
        super.onCreate(savedInstanceState);

        saveAsDraft.setVisibility(View.GONE);
        saveAsDraft = null;
        pointsInfo = findViewById(R.id.pointsInfo);
        transport = findViewById(R.id.transport);
        cost = findViewById(R.id.cost);
        distance = findViewById(R.id.distance);
        duration = findViewById(R.id.duration);
        startDate = findViewById(R.id.startDate);
        endDate = findViewById(R.id.endDate);

        // Start and end date listeners.
        startDate.setOnClickListener(new TripActivity.startDateOnClickListener());
        endDate.setOnClickListener(new TripActivity.endDateOnClickListener());

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String action = intent.getAction();
            if (Intent.ACTION_SEND.equals(action)) {
                try {
                    if (extras.containsKey(Intent.EXTRA_STREAM)) {
                        String gpxFile = Objects.requireNonNull(extras.get(Intent.EXTRA_STREAM)).toString();
                        if (gpxFile.length() > 0) {
                            parseGPXfile(Uri.parse(gpxFile), intent, false);
                        }
                    }
                }
                catch (Exception ignored) { }
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        MenuItem item = menu.findItem(R.id.loadGpx);
        item.setVisible(true);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == R.id.loadGpx) {
            Intent ii = new Intent();
            ii.setType("application/gpx+xml");
            ii.setAction(Intent.ACTION_OPEN_DOCUMENT);
            if (!isMediaRequest) {
                ii.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
            }
            startActivityForResult(Intent.createChooser(ii, getString(R.string.trip_load_gpx)), PICK_GPX_REQUEST);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == PICK_GPX_REQUEST && resultCode == RESULT_OK) {
            if (data.getData() != null) {
                parseGPXfile(data.getData(), data, true);
            }
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * Parse a GPX file.
     *
     * @param uri
     *   The file uri.
     * @param data
     *   The intent data.
     */
    private void parseGPXfile(Uri uri, Intent data, boolean takePermission) {

        final int takeFlags = data.getFlags()
                & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

        try {

            if (takePermission) {
                getContentResolver().takePersistableUriPermission(uri, takeFlags);
            }

            GPXParser mParser = new GPXParser();
            Gpx parsedGpx = null;
            try {
                InputStream in = getContentResolver().openInputStream(uri);
                parsedGpx = mParser.parse(in);
            }
            catch (IOException | XmlPullParserException e) {
                Snackbar.make(layout, String.format(getString(R.string.trip_reading_error), e.getMessage()), Snackbar.LENGTH_LONG).show();
            }

            if (parsedGpx != null) {
                points.clear();
                List<Track> tracks = parsedGpx.getTracks();

                for (int i = 0; i < tracks.size(); i++) {
                    Track track = tracks.get(i);
                    List<TrackSegment> segments = track.getTrackSegments();
                    for (int j = 0; j < segments.size(); j++) {
                        TrackSegment segment = segments.get(j);
                        for (TrackPoint trackPoint : segment.getTrackPoints()) {
                            String coordinates = String.format("%s,%s,%s", trackPoint.getLatitude(), trackPoint.getLongitude(), trackPoint.getElevation());
                            String GeoURI = "geo:" + coordinates;
                            points.add(GeoURI);
                        }
                    }
                }

                if (points.size() > 0) {
                    String message = String.format(getString(R.string.trip_points_count), points.size());
                    pointsInfo.setText(message);
                    Snackbar.make(layout, message, Snackbar.LENGTH_LONG).show();
                    setChanges(true);
                }
                else {
                    Snackbar.make(layout, getString(R.string.trip_no_points_found), Snackbar.LENGTH_LONG).show();
                }
            }

        }
        catch (Exception e) {
            String message = String.format(getString(R.string.trip_reading_error), e.getMessage());
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

    @Override
    public void onPostButtonClick(MenuItem item) {

        boolean hasErrors = false;

        if (TextUtils.isEmpty(title.getText())) {
            hasErrors = true;
            title.setError(getString(R.string.required_field));
        }

        if (!hasErrors) {

            if (!TextUtils.isEmpty(startDate.getText())) {
                bodyParams.put("start", startDate.getText().toString());
            }

            if (!TextUtils.isEmpty(endDate.getText())) {
                bodyParams.put("end", endDate.getText().toString());
            }

            if (!TextUtils.isEmpty(cost.getText())) {
                bodyParams.put("cost", cost.getText().toString());
            }

            if (!TextUtils.isEmpty(distance.getText())) {
                bodyParams.put("distance", distance.getText().toString());
            }

            if (!TextUtils.isEmpty(duration.getText())) {
                bodyParams.put("duration", duration.getText().toString());
            }

            String t = transport.getSelectedItem().toString();
            if (!t.equals("No transport")) {
                bodyParams.put("transport", t);
            }

            int i = 0;
            for (String p : points) {
                bodyParams.put("route_multiple_["+ i +"]", p);
                i++;
            }

            sendBasePost(item);
        }
    }

    /**
     * Start date onclick listener.
     */
    class startDateOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Utility.showDateTimePickerDialog(TripActivity.this, startDate);
        }
    }

    /**
     * End date onclick listener
     */
    class endDateOnClickListener implements View.OnClickListener {
        @Override
        public void onClick(View v) {
            Utility.showDateTimePickerDialog(TripActivity.this, endDate);
        }
    }

}
