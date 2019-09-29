package com.indieweb.indigenous.indieauth;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;

import java.util.ArrayList;
import java.util.List;

public class UsersFragment extends Fragment {

    User currentUser;
    ListView listView;
    List<User> users = new ArrayList<>();

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
        currentUser = new Accounts(getContext()).getCurrentUser();
        requireActivity().setTitle(R.string.accounts);
        startUsersList();
    }

    /**
     * Start users lists.
     */
    public void startUsersList() {
        users = new Accounts(getContext()).getAllUsers();
        UsersListAdapter adapter = new UsersListAdapter(requireContext(), getActivity(), users, currentUser);
        listView.setAdapter(adapter);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.accounts_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.user_add:
                Intent addUser = new Intent(getContext(), IndieAuthActivity.class);
                startActivity(addUser);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
