package com.indieweb.indigenous.general;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.indieweb.indigenous.R;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class AboutFragment extends Fragment {

    View currentView;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.activity_about, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().setTitle("About");
        currentView = view;
        loadAbout();

    }

    private class getAboutAsyncTask extends AsyncTask<Void, Void, StringBuilder> {

        @Override
        protected StringBuilder doInBackground(Void... voids) {

            StringBuilder text = new StringBuilder();

            try {
                String line;
                BufferedReader reader = new BufferedReader(new InputStreamReader(getActivity().getAssets().open("changelog")));
                while ((line = reader.readLine()) != null) {
                    text.append(line);
                    text.append('\n');
                }
            } catch (IOException ignored) {
                Toast.makeText(getContext(), "Exception: " + ignored.getMessage(), Toast.LENGTH_SHORT).show();
            }

            return text;
        }

        protected void onPostExecute(StringBuilder text) {
            displayAbout(text);
        }

    }

    public void loadAbout() {
        new getAboutAsyncTask().execute();
    }

    public void displayAbout(StringBuilder text) {
        TextView t = currentView.findViewById(R.id.about_changelog);
        t.setMovementMethod(LinkMovementMethod.getInstance());
        t.setText(Html.fromHtml(text.toString()));
    }

}
