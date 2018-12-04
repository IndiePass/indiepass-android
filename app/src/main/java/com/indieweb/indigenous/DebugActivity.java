package com.indieweb.indigenous;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;

public class DebugActivity extends AppCompatActivity {

    String debugString = "No debugging info found";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_debug);

        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            String debug = extras.getString("debug");

            // Try parsing with JSON, in case it fails, we'll fallback to the default string.
            try {
                debugString = new JSONObject(debug).toString(4);
            }
            catch (Exception ignored) {
                debugString = debug;
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
                    int sdk = android.os.Build.VERSION.SDK_INT;
                    if (sdk < android.os.Build.VERSION_CODES.HONEYCOMB) {
                        android.text.ClipboardManager clipboard = (android.text.ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
                        assert clipboard != null;
                        clipboard.setText(debugString);
                    }
                    else {
                        android.content.ClipboardManager clipboard = (android.content.ClipboardManager) this.getSystemService(CLIPBOARD_SERVICE);
                        android.content.ClipData clip = android.content.ClipData.newPlainText("Debug", debugString);
                        assert clipboard != null;
                        clipboard.setPrimaryClip(clip);
                    }
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
