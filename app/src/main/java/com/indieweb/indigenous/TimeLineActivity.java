package com.indieweb.indigenous;

import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

public class TimeLineActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timeline);

        FloatingActionButton note = findViewById(R.id.noteButton);
        note.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent CreateNote = new Intent(getBaseContext(), NoteActivity.class);
                startActivity(CreateNote);
            }
        });

        FloatingActionButton article = findViewById(R.id.articleButton);
        article.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent CreateArticle = new Intent(getBaseContext(), ArticleActivity.class);
                startActivity(CreateArticle);
            }
        });

    }

}
