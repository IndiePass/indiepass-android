package com.indieweb.indigenous.general;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONObject;

public class DebugActivity extends AppCompatActivity {

    String debugString = "No debugging info found";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

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
        switch (item.getItemId()) {
            case R.id.clipboard:

                try {
                    Utility.copyToClipboard(debugString, "Media url", getApplicationContext());
                    Toast.makeText(getApplicationContext(), "Copied to clipboard", Toast.LENGTH_SHORT).show();
                }
                catch (Exception ignored) {
                    Toast.makeText(getApplicationContext(), "Something went wrong to copy the text to the clipboard.", Toast.LENGTH_SHORT).show();
                }

                return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
