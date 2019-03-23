package com.indieweb.indigenous.micropub.draft;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

import java.util.List;

public class DraftFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_drafts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(R.string.drafts);

        ListView listDraft = view.findViewById(R.id.draft_list);
        TextView empty = view.findViewById(R.id.noDrafts);

        User user = new Accounts(getContext()).getCurrentUser();
        DatabaseHelper db = new DatabaseHelper(requireContext());
        List<Draft> drafts = db.getDrafts(user.getMeWithoutProtocol());

        if (drafts.size() == 0) {
            listDraft.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        }
        else {
            DraftListAdapter adapter = new DraftListAdapter(requireContext(), drafts);
            listDraft.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

}
