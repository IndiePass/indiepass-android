package com.indieweb.indigenous.tracker;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Point;
import com.indieweb.indigenous.model.Track;
import com.indieweb.indigenous.model.User;
import com.indieweb.indigenous.util.Connection;
import com.indieweb.indigenous.util.Preferences;
import com.indieweb.indigenous.util.Utility;
import com.indieweb.indigenous.util.VolleyMultipartRequest;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.indieweb.indigenous.MainActivity.UPDATE_TRACK;

/**
 * Tracker list adapter.
 */
public class TrackerListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<Track> tracks;
    private LayoutInflater mInflater;
    private DatabaseHelper db;
    private User user;

    TrackerListAdapter(Context context, List<Track> tracks, User user) {
        this.context = context;
        this.tracks = tracks;
        this.db = new DatabaseHelper(context);
        this.user = user;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return tracks.size();
    }

    public Track getItem(int position) {
        return tracks.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void onClick(View view) { }

    public static class ViewHolder {
        int position;
        TextView label;
        TextView meta;
        Button post;
        Button edit;
        //Button map;
        Button delete;
        LinearLayout row;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_track, null);
            holder = new ViewHolder();
            holder.row = convertView.findViewById(R.id.track_list_item_row);
            holder.label = convertView.findViewById(R.id.track_list_label);
            holder.meta = convertView.findViewById(R.id.track_list_meta);
            holder.edit = convertView.findViewById(R.id.trackEdit);
            holder.post = convertView.findViewById(R.id.trackPost);
            //holder.map = convertView.findViewById(R.id.trackMap);
            holder.delete = convertView.findViewById(R.id.trackDelete);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Track track = tracks.get(position);
        if (track != null) {

            holder.position = position;

            // Color of row.
            int color = context.getResources().getColor(R.color.listRowBackgroundColor);
            holder.row.setBackgroundColor(color);

            // Label
            String label = track.getTitle();
            holder.label.setText(label);

            // Meta.
            String started;
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MM yyyy HH:mm");
            try {
                Date result = formatIn.parse(track.getStartTime());
                started = formatOut.format(result);
            }
            catch (ParseException ignored) {
                started = "/";
            }

            String ended;
            if (!track.getStartTime().equals(track.getEndTime())) {
                try {
                    Date result = formatIn.parse(track.getEndTime());
                    ended = formatOut.format(result);
                }
                catch (ParseException e) {
                    ended = "/";
                }
                holder.meta.setText(String.format(context.getString(R.string.tracker_meta_end), track.getPointCount(), track.getTransport(), track.getInterval(), started, ended));
            }
            else {
                holder.meta.setText(String.format(context.getString(R.string.tracker_meta_no_end), track.getPointCount(), track.getTransport(), track.getInterval(), started));
            }

            holder.post.setOnClickListener(new OnPostClickListener(position));
            holder.edit.setOnClickListener(new OnEditClickListener(position));
            //holder.map.setOnClickListener(new OnMapClickListener(position));
            holder.delete.setOnClickListener(new OnDeleteClickListener(position));
        }

        return convertView;
    }

    // Edit listener.
    class OnEditClickListener implements OnClickListener {

        int position;

        OnEditClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Track track = tracks.get(this.position);
            Intent startActivity = new Intent(context, TrackActivity.class);
            startActivity.putExtra("trackId", track.getId());
            ((Activity) context).startActivityForResult(startActivity, UPDATE_TRACK);
        }
    }

    // Post listener.
    class OnPostClickListener implements OnClickListener {

        int position;

        OnPostClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final Track track = tracks.get(this.position);
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.track_post_confirm));
            builder.setPositiveButton(context.getString(R.string.send),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    sendTrack(track);
                }
            });
            builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    // Map listener.
    /*class OnMapClickListener implements OnClickListener {

        int position;

        OnMapClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Track track = tracks.get(this.position);
            Map<Integer, Point> points = db.getPoints(track.getId());
            Collection<Point> values = points.values();
            String geo = "";
            int i = 0;
            for (Point p: values) {
                String[] coordinates = p.getPoint().replace("geo:", "").split(",");
                if (i > 0) {
                    geo += ",";
                }
                geo += "[" + coordinates[0] + "," + coordinates[1] + "]";
                i++;
            }

            // Google maps version - bit annoying as it opens navigation.
            //String[] coordinates = p.getPoint().replace("geo:", "").split(",");
            //geo += "/" + coordinates[0] + "," + coordinates[1]; // (i > 0)
            //geo = coordinates[0] + "," + coordinates[1];
            //Uri geoLocation = Uri.parse("https://www.google.com/maps/dir/" + geo);

            // Atlas from Aaron - doesn't seem to have Belgium maps
            //String[] coordinates = p.getPoint().replace("geo:", "").split(",");
            //geo += ","; // (i > )
            //geo += "[" + coordinates[0] + "," + coordinates[1] + "]";
            //String mapUrl = "http://atlas.p3k.io/map/img?path[]=" + geo + ";icon:small-blue-cutout&basemap=gray&width=460&height=460&zoom=11";

            String mapUrl = "http://atlas.p3k.io/map/img?path[]=" + geo + ";icon:small-blue-cutout&basemap=gray&width=460&height=460";
            Uri geoLocation = Uri.parse(mapUrl);

            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(geoLocation);
            if (intent.resolveActivity(context.getPackageManager()) != null) {
                context.startActivity(intent);
            }
            else {
                Toast.makeText(context, context.getString(R.string.install_map_app), Toast.LENGTH_SHORT).show();
            }

        }
    }*/

    // Delete listener.
    class OnDeleteClickListener implements OnClickListener {

        int position;

        OnDeleteClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final Track track = tracks.get(this.position);
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.track_delete_confirm));
            builder.setPositiveButton(context.getString(R.string.delete_track),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    db.deleteTrack(track.getId());
                    tracks.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, context.getString(R.string.track_deleted), Toast.LENGTH_SHORT).show();
                }
            });
            builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    /**
     * Send track.
     *
     * @param track
     *   The track to send.
     */
    private void sendTrack(final Track track) {
        Map<Integer, Point> points = db.getPoints(track.getId());
        final Collection<Point> values = points.values();

        if (!new Connection(context).hasConnection()) {
            Toast.makeText(context, context.getString(R.string.no_connection), Toast.LENGTH_SHORT).show();
            return;
        }

        Toast.makeText(context, R.string.sending_please_wait, Toast.LENGTH_SHORT).show();
        String MicropubEndpoint = user.getMicropubEndpoint();
        if (MicropubEndpoint.length() == 0) {
            Toast.makeText(context, context.getString(R.string.no_micropub_endpoint), Toast.LENGTH_SHORT).show();
            return;
        }

        VolleyMultipartRequest getRequest = new VolleyMultipartRequest(Request.Method.POST, MicropubEndpoint,
                new Response.Listener<NetworkResponse>() {
                    @Override
                    public void onResponse(NetworkResponse response) {
                        Toast.makeText(context, R.string.post_track_success, Toast.LENGTH_SHORT).show();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Utility.parseNetworkError(error, context, R.string.post_track_network_error, R.string.post_track_error);
                    }
                }
        )
        {
            @Override
            protected Map<String, String> getParams() {
                Map<String, String> params = new HashMap<>();

                // Send along access token if configured.
                if (Preferences.getPreference(context, "pref_key_access_token_body", false)) {
                    params.put("access_token", user.getAccessToken());
                }

                // Put name and h.
                params.put("name", track.getTitle());
                params.put("h", "entry");

                int i = 0;
                for (Point p : values) {
                    params.put("route_multiple_["+ i +"]", p.getPoint());
                    i++;
                }

                return params;
            }

            @Override
            public Map<String, String> getHeaders() {
                HashMap<String, String> headers = new HashMap<>();
                headers.put("Accept", "application/json");

                // Send access token in header by default.
                if (!Preferences.getPreference(context, "pref_key_access_token_body", false)) {
                    headers.put("Authorization", "Bearer " + user.getAccessToken());
                }

                return headers;
            }
        };

        RequestQueue queue = Volley.newRequestQueue(context);
        queue.add(getRequest);
    }
}