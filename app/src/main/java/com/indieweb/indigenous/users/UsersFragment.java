package com.indieweb.indigenous.users;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import android.widget.RelativeLayout;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Utility;
import com.karumi.dexter.Dexter;
import com.karumi.dexter.PermissionToken;
import com.karumi.dexter.listener.PermissionDeniedResponse;
import com.karumi.dexter.listener.PermissionGrantedResponse;
import com.karumi.dexter.listener.PermissionRequest;
import com.karumi.dexter.listener.single.PermissionListener;

import java.util.List;

public class UsersFragment extends Fragment {

    private User currentUser;
    private ListView listView;
    private RelativeLayout layout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_users, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        listView = view.findViewById(R.id.users_list);
        currentUser = new Accounts(getContext()).getDefaultUser();
        requireActivity().setTitle(R.string.accounts);
        layout = view.findViewById(R.id.users_list_root);

        // This requires user permission from 22 on.
        Dexter.withActivity(requireActivity())
                .withPermission(Manifest.permission.GET_ACCOUNTS)
                .withListener(new PermissionListener() {
                    @Override
                    public void onPermissionGranted(PermissionGrantedResponse response) {
                        startUsersList();
                    }

                    @Override
                    public void onPermissionDenied(PermissionDeniedResponse response) {
                        if (response.isPermanentlyDenied()) {
                            Utility.openSettings(requireContext());
                        }
                    }

                    @Override
                    public void onPermissionRationaleShouldBeShown(PermissionRequest permission, PermissionToken token) {
                        token.continuePermissionRequest();
                    }

                }).check();
    }

    /**
     * Start users lists.
     */
    private void startUsersList() {
        try {
            List<User> users = new Accounts(getContext()).getAllUsers();
            UsersListAdapter adapter = new UsersListAdapter(requireContext(), getActivity(), users, currentUser, layout);
            listView.setAdapter(adapter);
        } catch (Exception e) {
            Snackbar.make(layout, getString(R.string.user_exception), Snackbar.LENGTH_LONG).show();
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.accounts_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.user_add) {
            Intent addUser = new Intent(getContext(), AuthActivity.class);
            startActivity(addUser);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
