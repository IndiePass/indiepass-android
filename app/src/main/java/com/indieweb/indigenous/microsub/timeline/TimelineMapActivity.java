package com.indieweb.indigenous.microsub.timeline;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.general.BaseMapActivity;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.FeatureCollection;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.style.layers.SymbolLayer;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.mapbox.mapboxsdk.utils.BitmapUtils;

import java.util.Objects;

import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconImage;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.iconOffset;

public class TimelineMapActivity extends BaseMapActivity {

    Double latitude;
    Double longitude;
    boolean zoomed = false;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            latitude = Double.parseDouble(Objects.requireNonNull(extras.getString("latitude")));
            longitude = Double.parseDouble(Objects.requireNonNull(extras.getString("longitude")));
        }

        if (longitude != null && latitude != null) {
            renderMap(savedInstanceState);
        }
        else {
            Snackbar.make(layout, getString(R.string.coordinates_not_found), Snackbar.LENGTH_SHORT).show();
        }
    }

    /**
     * Renders the location on the map.
     */
    protected void renderMap(Bundle savedInstanceState) {
        mapView.onCreate(savedInstanceState);
        mapView.addOnDidFinishLoadingStyleListener(new MapView.OnDidFinishLoadingStyleListener() {
            @Override
            public void onDidFinishLoadingStyle() {
                addMarkerIconsToMap(mapboxMap.getStyle());
                if (!zoomed) {
                    zoomCamera();
                    zoomed = true;
                }
            }
        });
        mapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull final MapboxMap mapboxMap) {
                TimelineMapActivity.this.mapboxMap = mapboxMap;
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
            }
        });
    }

    /**
     * Zoom the camera.
     */
    private void zoomCamera() {
        mapboxMap.easeCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(latitude, longitude), 13), 2000);
    }

    /**
     * Add marker to map.
     *
     * @param loadedMapStyle
     *   The current map style.
     */
    private void addMarkerIconsToMap(@NonNull Style loadedMapStyle) {
        loadedMapStyle.addImage("icon-id", BitmapUtils.getBitmapFromDrawable(getResources().getDrawable(R.drawable.red_marker)));
        loadedMapStyle.addSource(new GeoJsonSource("source-id",
                FeatureCollection.fromFeatures(new Feature[] {
                        Feature.fromGeometry(Point.fromLngLat(longitude, latitude)),
                })));
        loadedMapStyle.addLayer(new SymbolLayer("layer-id",
                "source-id").withProperties(
                iconImage("icon-id"),
                iconOffset(new Float[]{0f,-8f})
        ));
    }

}
