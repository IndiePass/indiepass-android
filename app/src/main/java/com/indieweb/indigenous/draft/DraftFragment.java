package com.indieweb.indigenous.draft;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.users.Accounts;

import java.util.List;

public class DraftFragment extends Fragment {

    private OnDraftChangedListener callback;

    public void OnDraftChangedListener(OnDraftChangedListener callback) {
        this.callback = callback;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_drafts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(R.string.drafts);

        LinearLayout layout = view.findViewById(R.id.draft_root);
        ListView listDraft = view.findViewById(R.id.draft_list);
        TextView empty = view.findViewById(R.id.noDrafts);

        User user = new Accounts(getContext()).getDefaultUser();
        DatabaseHelper db = new DatabaseHelper(requireContext());
        List<Draft> drafts = db.getDrafts();

        if (drafts.size() == 0) {
            listDraft.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
        } else {
            DraftListAdapter adapter = new DraftListAdapter(requireContext(), drafts, callback, layout);
            listDraft.setAdapter(adapter);
            adapter.notifyDataSetChanged();
        }
    }

    public interface OnDraftChangedListener {
        void onDraftChanged();
    }
}
