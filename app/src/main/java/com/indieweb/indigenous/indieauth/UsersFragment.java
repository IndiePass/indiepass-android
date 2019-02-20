package com.indieweb.indigenous.indieauth;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
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
        listView = view.findViewById(R.id.users_list);
        currentUser = new Accounts(getContext()).getCurrentUser();
        getActivity().setTitle(R.string.accounts);
        startUsersList();
    }

    public void startUsersList() {
        users = new Accounts(getContext()).getAllUsers();
        UsersListAdapter adapter = new UsersListAdapter(getContext(), getActivity(), users, currentUser);
        listView.setAdapter(adapter);
    }

}
