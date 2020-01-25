package com.indieweb.indigenous.tracker;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.general.BaseMapActivity;
import com.indieweb.indigenous.model.TrackerPoint;
import com.indieweb.indigenous.model.Track;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.geometry.LatLngBounds;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.layers.PropertyFactory;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class TrackerMapActivity extends BaseMapActivity {

    Track track;
    DatabaseHelper db;
    Map<Integer, TrackerPoint> points;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new DatabaseHelper(getApplicationContext());
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            int id = extras.getInt("trackId");
            track = db.getTrack(id);
        }

        if (track != null) {
            points = db.getPoints(track.getId());
            renderMap(savedInstanceState);
        }
        else {
            Snackbar.make(layout, getString(R.string.track_not_found), Snackbar.LENGTH_SHORT).show();
        }

    }

    /**
     * Renders the map.
     */
    protected void renderMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.addOnDidFinishLoadingStyleListener(new MapView.OnDidFinishLoadingStyleListener() {
            @Override
            public void onDidFinishLoadingStyle() {
                renderTrack(mapboxMap.getStyle());
            }
        });
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                TrackerMapActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
            }
        });
    }

    /**
     * Render the track.
     *
     * @param style
     *   The current style.
     */
    private void renderTrack(Style style) {
        List<Point> routeCoordinates = new ArrayList<>();
        Collection<TrackerPoint> values = points.values();
        final List<LatLng> latLngList = new ArrayList(points.size());
        for (TrackerPoint p: values) {
            String[] coordinates = p.getPoint().replace("geo:", "").split(",");
            double lat = Double.parseDouble(coordinates[0]);
            double lon = Double.parseDouble(coordinates[1]);
            latLngList.add(new LatLng(lat, lon));
            routeCoordinates.add(Point.fromLngLat(lon, lat));
        }

        // Create the LineString from the list of coordinates and then make a
        // GeoJSON FeatureCollection so we can add the line to our map as a layer.
        style.addSource(new GeoJsonSource("line-source",
                FeatureCollection.fromFeatures(new Feature[] {Feature.fromGeometry(
                        LineString.fromLngLats(routeCoordinates)
                )})));

        // The layer properties for our line. This is where we make the line dotted,
        // set the color, etc.
        style.addLayer(new LineLayer("linelayer", "line-source").withProperties(
                PropertyFactory.lineDasharray(new Float[] {0.01f, 2f}),
                PropertyFactory.lineCap(Property.LINE_CAP_ROUND),
                PropertyFactory.lineJoin(Property.LINE_JOIN_ROUND),
                PropertyFactory.lineWidth(5f),
                PropertyFactory.lineColor(ContextCompat.getColor(getApplicationContext(), R.color.colorPrimary))
        ));

        if (latLngList.size() > 0) {
            LatLngBounds latLngBounds = new LatLngBounds.Builder()
                    .includes(latLngList)
                    .build();
            mapboxMap.easeCamera(CameraUpdateFactory.newLatLngBounds(latLngBounds, 100), 2000);
        }
    }

}
