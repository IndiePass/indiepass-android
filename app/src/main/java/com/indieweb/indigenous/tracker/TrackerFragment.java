package com.indieweb.indigenous.tracker;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Track;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

import java.util.List;

import static com.indieweb.indigenous.MainActivity.CREATE_TRACK;

public class TrackerFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private ListView listTracker;
    private TextView empty;
    private boolean showRefreshMessage = false;
    private SwipeRefreshLayout refreshLayout;
    private RelativeLayout layout;

    @Override
    public void onRefresh() {
        showRefreshMessage = true;
        startTracks();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_tracker_list, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        setHasOptionsMenu(true);

        view.findViewById(R.id.actionButton).setOnClickListener(this);
        requireActivity().setTitle(R.string.tracker);

        listTracker = view.findViewById(R.id.tracker_list);
        empty = view.findViewById(R.id.noTracks);
        refreshLayout = view.findViewById(R.id.refreshTracks);
        refreshLayout.setOnRefreshListener(this);
        layout = view.findViewById(R.id.tracker_root);

        startTracks();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.actionButton) {
            if (!TrackerUtils.requestingLocationUpdates(requireContext())) {
                Intent CreateTrack = new Intent(requireActivity(), TrackActivity.class);
                ((Activity) requireContext()).startActivityForResult(CreateTrack, CREATE_TRACK);
            }
            else {
                Snackbar.make(layout, getString(R.string.tracker_running), Snackbar.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.tracker_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.tracker_refresh:
                showRefreshMessage = true;
                refreshLayout.setRefreshing(true);
                startTracks();
                return true;

            case R.id.tracker_truncate:
                final AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(getString(R.string.tracker_truncate_confirm));
                builder.setPositiveButton(getString(R.string.delete_track),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {
                        DatabaseHelper db = new DatabaseHelper(requireContext());
                        db.deleteAllTrackerData();
                        startTracks();
                        Snackbar.make(layout, getString(R.string.tracker_truncated), Snackbar.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
        }


        return super.onOptionsItemSelected(item);
    }

    /**
     * Start tracks.
     */
    private void startTracks() {
        User user = new Accounts(getContext()).getCurrentUser();
        DatabaseHelper db = new DatabaseHelper(requireContext());
        List<Track> tracks = db.getTracks(user.getMeWithoutProtocol());

        if (tracks.size() == 0) {
            listTracker.setVisibility(View.GONE);
            empty.setVisibility(View.VISIBLE);
            refreshLayout.setVisibility(View.GONE);
        }
        else {
            refreshLayout.setVisibility(View.VISIBLE);
            refreshLayout.setRefreshing(true);
            TrackerListAdapter adapter = new TrackerListAdapter(requireContext(), tracks, user, layout);
            listTracker.setAdapter(adapter);
            adapter.notifyDataSetChanged();
            checkRefreshingStatus();
        }
    }

    /**
     * Checks the state of the pull to refresh.
     */
    private void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            if (showRefreshMessage) {
                Snackbar.make(layout, getString(R.string.tracker_refreshed), Snackbar.LENGTH_SHORT).show();
            }
            refreshLayout.setRefreshing(false);
        }
    }

}
