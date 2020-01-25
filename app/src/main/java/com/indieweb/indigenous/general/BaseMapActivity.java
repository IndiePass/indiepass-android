package com.indieweb.indigenous.general;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.indieweb.indigenous.BuildConfig;
import com.indieweb.indigenous.R;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.Style;

@SuppressLint("Registered")
public class BaseMapActivity extends AppCompatActivity {

    public RelativeLayout layout;
    public MapboxMap mapboxMap;
    public MapView mapView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(this, BuildConfig.MAPBOX_ACCESS_TOKEN);
        setContentView(R.layout.activity_map);

        layout = findViewById(R.id.map_root);
        mapView = findViewById(R.id.mapView);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_streets:
                mapboxMap.setStyle(Style.MAPBOX_STREETS);
                return true;
            case R.id.menu_dark:
                mapboxMap.setStyle(Style.DARK);
                return true;
            case R.id.menu_light:
                mapboxMap.setStyle(Style.LIGHT);
                return true;
            case R.id.menu_outdoors:
                mapboxMap.setStyle(Style.OUTDOORS);
                return true;
            case R.id.menu_satellite:
                mapboxMap.setStyle(Style.SATELLITE);
                return true;
            case R.id.menu_satellite_streets:
                mapboxMap.setStyle(Style.SATELLITE_STREETS);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
        mapView.onStop();
    }

    @Override
    public void onPause() {
        super.onPause();
        mapView.onPause();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mapView.onDestroy();
    }
}
