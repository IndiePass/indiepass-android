package com.indieweb.indigenous.micropub.draft;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ListView;
import android.widget.TextView;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

import java.util.List;

public class DraftActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drafts);

        ListView listDraft = findViewById(R.id.draft_list);
        TextView empty = findViewById(R.id.noDrafts);

        User user = new Accounts(this).getCurrentUser();
        DatabaseHelper db = new DatabaseHelper(DraftActivity.this);
        List<Draft> drafts = db.getDrafts(user.getMeWithoutProtocol());

        if (drafts.size() == 0) {
            listDraft.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }
        else {
            DraftListAdapter adapter = new DraftListAdapter(this, drafts);
            listDraft.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }

    }


}
