package com.indieweb.indigenous.micropub.contact;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.general.DebugActivity;
import com.indieweb.indigenous.micropub.post.ContactActivity;
import com.indieweb.indigenous.model.Contact;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ContactFragment extends Fragment implements View.OnClickListener, SwipeRefreshLayout.OnRefreshListener {

    private boolean showRefreshMessage = false;
    private ListView listContact;
    private SwipeRefreshLayout refreshLayout;
    private ContactListAdapter adapter;
    private List<Contact> Contacts = new ArrayList<>();
    private User user;
    private String debugResponse;
    private RelativeLayout layout;
    private LinearLayout noConnection;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_contacts, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        requireActivity().setTitle(R.string.contact_list_title);

        noConnection = view.findViewById(R.id.noConnection);
        listContact = view.findViewById(R.id.contact_list);
        refreshLayout = view.findViewById(R.id.refreshContacts);
        TextView noMicropubEndpoint = view.findViewById(R.id.noMicropubEndpoint);
        user = new Accounts(getContext()).getCurrentUser();
        view.findViewById(R.id.actionButton).setOnClickListener(this);
        layout = view.findViewById(R.id.contacts_root);

        if (user.getMicropubEndpoint().length() > 0) {
            setHasOptionsMenu(true);
            refreshLayout.setOnRefreshListener(this);
            refreshLayout.setRefreshing(true);
            refreshLayout.setVisibility(View.VISIBLE);
            listContact.setVisibility(View.VISIBLE);
            startContacts();
        }
        else {
            listContact.setVisibility(View.GONE);
            noMicropubEndpoint.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Start contacts.
     */
    private void startContacts() {
        noConnection.setVisibility(View.GONE);
        Contacts = new ArrayList<>();
        listContact.setVisibility(View.VISIBLE);
        adapter = new ContactListAdapter(requireContext(), Contacts, user, layout);
        listContact.setAdapter(adapter);
        loadContacts();
    }

    /**
     * Load contacts.
     */
    private void loadContacts() {

        if (!new Connection(requireContext()).hasConnection()) {
            showRefreshMessage = false;
            checkRefreshingStatus();
            noConnection.setVisibility(View.VISIBLE);
            return;
        }

        String MicropubEndpoint = user.getMicropubEndpoint();
        if (MicropubEndpoint.contains("?")) {
            MicropubEndpoint += "&q=contact";
        }
        else {
            MicropubEndpoint += "?q=contact";
        }

        StringRequest getRequest = new StringRequest(Request.Method.GET, MicropubEndpoint,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {

                        debugResponse = response;

                        try {
                            JSONObject categoryResponse = new JSONObject(response);
                            if (categoryResponse.has("contacts")) {
                                JSONObject contactObject;
                                JSONArray contactList = categoryResponse.getJSONArray("contacts");
                                if (contactList.length() > 0) {
                                    for (int i = 0; i < contactList.length(); i++) {
                                        contactObject = contactList.getJSONObject(i);
                                        if (contactObject.has("name")) {
                                            Contact contact = new Contact();
                                            contact.setName(contactObject.getString("name"));

                                            if (contactObject.has("nickname")) {
                                                contact.setNickname(contactObject.getString("nickname"));
                                            }

                                            if (contactObject.has("url")) {
                                                contact.setUrl(contactObject.getString("url"));
                                            }

                                            if (contactObject.has("photo")) {
                                                contact.setPhoto(contactObject.getString("photo"));
                                            }

                                            if (contactObject.has("_internal_url")) {
                                                contact.setInternalUrl(contactObject.getString("_internal_url"));
                                            }

                                            Contacts.add(contact);
                                        }
                                    }
                                }
                            }

                            adapter.notifyDataSetChanged();
                            checkRefreshingStatus();
                        }
                        catch (JSONException e) {
                            showRefreshMessage = false;
                            Snackbar.make(layout, String.format(getString(R.string.contact_list_parse_error), e.getMessage()), Snackbar.LENGTH_LONG).show();
                            checkRefreshingStatus();
                        }
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Context context = getContext();
                        Utility.parseNetworkError(error, context, R.string.contact_network_fail, R.string.contact_fail);
                        showRefreshMessage = false;
                        checkRefreshingStatus();
                    }
                }
        )
        {
            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");
                headers.put("Authorization", "Bearer " + user.getAccessToken());
                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(requireContext());
        queue.add(getRequest);
    }

    @Override
    public void onRefresh() {
        showRefreshMessage = true;
        startContacts();
    }

    /**
     * Checks the state of the pull to refresh.
     */
    private void checkRefreshingStatus() {
        if (refreshLayout.isRefreshing()) {
            if (showRefreshMessage) {
                Snackbar.make(layout, getString(R.string.contacts_refreshed), Snackbar.LENGTH_SHORT).show();
            }
            refreshLayout.setRefreshing(false);
        }
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
                showRefreshMessage = true;
                refreshLayout.setRefreshing(true);
                startContacts();
                return true;

            case R.id.contact_debug:
                Intent i = new Intent(getContext(), DebugActivity.class);
                Indigenous app = Indigenous.getInstance();
                app.setDebug(debugResponse);
                startActivity(i);
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
