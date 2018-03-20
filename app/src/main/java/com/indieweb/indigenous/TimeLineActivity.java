package com.indieweb.indigenous;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.kennyc.bottomsheet.BottomSheet;
import com.kennyc.bottomsheet.BottomSheetListener;

public class TimeLineActivity extends AppCompatActivity implements View.OnClickListener, BottomSheetListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);
        findViewById(R.id.actionButton).setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.actionButton:
                new BottomSheet.Builder(this)
                        .setSheet(R.menu.menu)
                        .setListener(this)
                        .show();
                break;
        }
    }

    @Override
    public void onSheetShown(@NonNull BottomSheet bottomSheet, @Nullable Object object) {}

    @Override
    public void onSheetItemSelected(@NonNull BottomSheet bottomSheet, MenuItem menuItem, @Nullable Object o) {
        switch (menuItem.getItemId()) {
            case R.id.createArticle:
                Intent CreateArticle = new Intent(getBaseContext(), ArticleActivity.class);
                startActivity(CreateArticle);
                break;
            case R.id.createNote:
                Intent CreateNote = new Intent(getBaseContext(), NoteActivity.class);
                startActivity(CreateNote);
                break;
        }
    }

    @Override
    public void onSheetDismissed(@NonNull BottomSheet bottomSheet, @Nullable Object o, int i) {}
}
