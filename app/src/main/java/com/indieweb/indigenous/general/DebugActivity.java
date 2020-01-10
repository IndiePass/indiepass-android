package com.indieweb.indigenous.general;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONObject;

public class DebugActivity extends AppCompatActivity {

    ScrollView layout;
    String debugString = "No debugging info found";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        layout = findViewById(R.id.debug_root);

        Indigenous app = Indigenous.getInstance();
        String appDebug = app.getDebug();
        if (appDebug != null) {

            // Try parsing with JSON, in case it fails, we'll fallback to the default string.
            try {
                debugString = new JSONObject(appDebug).toString(4);
            }
            catch (Exception ignored) {
                debugString = appDebug;
            }
        }

        TextView t = findViewById(R.id.debug);
        t.setText(debugString);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.debug_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.clipboard) {
            try {
                Utility.copyToClipboard(debugString, getString(R.string.clipboard_label), getApplicationContext());
                Snackbar.make(layout, getString(R.string.clipboard_success), Snackbar.LENGTH_SHORT).show();
            }
            catch (Exception ignored) {
                Snackbar.make(layout, getString(R.string.clipboard_error), Snackbar.LENGTH_SHORT).show();
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
