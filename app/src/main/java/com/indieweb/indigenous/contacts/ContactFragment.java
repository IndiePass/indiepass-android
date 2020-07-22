package com.indieweb.indigenous.contacts;

import android.accounts.AccountManager;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.general.BaseFragment;
import com.indieweb.indigenous.indieweb.micropub.MicropubAction;
import com.indieweb.indigenous.post.ContactActivity;
import com.indieweb.indigenous.model.Contact;
import com.indieweb.indigenous.util.HTTPRequest;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import java.util.ArrayList;
import java.util.List;

public class ContactFragment extends BaseFragment implements View.OnClickListener {

    private ListView listContact;
    private ContactListAdapter adapter;
    private final List<Contact> Contacts = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(R.string.contact_list_title);

        listContact = view.findViewById(R.id.contact_list);
        layout = view.findViewById(R.id.contacts_root);

        setRefreshedMessage(R.string.contacts_refreshed);
        TextView noMicropubEndpoint = view.findViewById(R.id.noMicropubEndpoint);
        view.findViewById(R.id.actionButton).setOnClickListener(this);

        if (user.getMicropubEndpoint().length() > 0) {
            setHasOptionsMenu(true);
            setOnRefreshListener();
            setLayoutRefreshing(true);
            showRefreshLayout();
            listContact.setVisibility(View.VISIBLE);
            startContacts();
        }
        else {
            listContact.setVisibility(View.GONE);
            noMicropubEndpoint.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void OnSuccessRequest(String response) {
        List<Contact> ContactsLive = MicropubAction.parseContactsResponse(response, requireContext(), false);
        if (ContactsLive.size() > 0) {
            Contacts.addAll(ContactsLive);
            adapter.notifyDataSetChanged();
        }
        checkRefreshingStatus();
    }

    /**
     * Start contacts.
     */
    private void startContacts() {
        hideNoConnection();
        listContact.setVisibility(View.VISIBLE);
        adapter = new ContactListAdapter(requireContext(), Contacts, user, layout);
        listContact.setAdapter(adapter);
        loadContacts();
    }

    /**
     * Load contacts.
     */
    private void loadContacts() {
        Contacts.clear();

        if (!Utility.hasConnection(requireContext())) {
            setShowRefreshedMessage(false);

            List<Contact> ContactsOffline = new ArrayList<>();
            AccountManager am = AccountManager.get(requireContext());
            String response = am.getUserData(user.getAccount(), "contact_list");
            if (response != null && response.length() > 0) {
                debugResponse = response;
                ContactsOffline = MicropubAction.parseContactsResponse(response, requireContext(), false);
            }

            if (ContactsOffline.size() > 0) {
                Contacts.addAll(ContactsOffline);
                adapter.notifyDataSetChanged();
                Snackbar.make(layout, getString(R.string.contacts_offline), Snackbar.LENGTH_SHORT).show();
            }
            else {
                showNoConnection();
            }

            checkRefreshingStatus();
            return;
        }

        String MicropubEndpoint = user.getMicropubEndpoint();
        if (MicropubEndpoint.contains("?")) {
            MicropubEndpoint += "&q=contact";
        }
        else {
            MicropubEndpoint += "?q=contact";
        }

        HTTPRequest r = new HTTPRequest(this.volleyRequestListener, user, requireContext());
        r.doGetRequest(MicropubEndpoint);
    }

    @Override
    public void onRefresh() {
        setShowRefreshedMessage(true);
        startContacts();
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.actionButton) {
            Intent contactActivity = new Intent(getContext(), ContactActivity.class);
            startActivity(contactActivity);
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.contact_menu, menu);

        boolean debugJson = Preferences.getPreference(getActivity(), "pref_key_debug_contact_list", false);
        if (debugJson) {
            MenuItem item = menu.findItem(R.id.contact_debug);
            if (item != null) {
                item.setVisible(true);
            }
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.contact_list_refresh:
                setShowRefreshedMessage(true);
                setLayoutRefreshing(true);
                startContacts();
                return true;

            case R.id.contact_debug:
                Utility.showDebugInfo(getContext(), debugResponse);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
