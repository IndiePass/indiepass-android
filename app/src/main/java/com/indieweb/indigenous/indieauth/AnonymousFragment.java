package com.indieweb.indigenous.indieauth;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Accounts;
import com.indieweb.indigenous.util.Preferences;

public class AnonymousFragment extends Fragment {

    private RelativeLayout layout;
    private User user;
    private EditText readerEndpoint;
    private EditText postEndpoint;
    private EditText token;
    private Button save;
    private Button reset;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_user_anonymous, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        setHasOptionsMenu(true);
        requireActivity().setTitle(R.string.accounts);
        user = new Accounts(getContext()).getCurrentUser();
        layout = view.findViewById(R.id.anonymous_root);
        readerEndpoint = view.findViewById(R.id.reader);
        postEndpoint = view.findViewById(R.id.post);
        token = view.findViewById(R.id.token);
        save = view.findViewById(R.id.save);
        reset = view.findViewById(R.id.reset);
        render();
    }

    /**
     * Render settings.
     */
    private void render() {
        setValues();
        save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Preferences.setPreference(getContext(), "anonymous_microsub_endpoint", readerEndpoint.getText().toString());
                Preferences.setPreference(getContext(), "anonymous_micropub_endpoint", postEndpoint.getText().toString());
                Preferences.setPreference(getContext(), "anonymous_token", token.getText().toString());
                Snackbar.make(layout, getString(R.string.anonymous_saved), Snackbar.LENGTH_SHORT).show();
            }
        });

        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
                builder.setTitle(getString(R.string.reset_confirm));
                builder.setCancelable(true);
                builder.setPositiveButton(getString(R.string.reset),new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog,int id) {

                        Preferences.setPreference(getContext(), "anonymous_microsub_endpoint", getString(R.string.anonymous_microsub_endpoint));
                        Preferences.setPreference(getContext(), "anonymous_micropub_endpoint","");
                        Preferences.setPreference(getContext(), "anonymous_token", "");
                        user = new Accounts(getContext()).getCurrentUser();
                        setValues();

                        Snackbar.make(layout, getString(R.string.anonymous_reset), Snackbar.LENGTH_SHORT).show();
                    }
                });
                builder.setNegativeButton(requireContext().getString(R.string.cancel), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int index) {
                        dialog.dismiss();
                    }
                });
                builder.show();
            }
        });
    }

    /**
     * Set values.
     */
    private void setValues() {
        readerEndpoint.setText(user.getMicrosubEndpoint());
        postEndpoint.setText(user.getMicropubEndpoint());
        token.setText(user.getAccessToken());
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.accounts_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        if (item.getItemId() == R.id.user_add) {
            Intent addUser = new Intent(getContext(), IndieAuthActivity.class);
            startActivity(addUser);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

}
