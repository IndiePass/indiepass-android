package com.indieweb.indigenous.draft;

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
import androidx.appcompat.app.AlertDialog;
import com.google.android.material.snackbar.Snackbar;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.post.*;
import com.indieweb.indigenous.util.Utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import static com.indieweb.indigenous.MainActivity.POST_DRAFT;

/**
 * Drafts list adapter.
 */
public class DraftListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<Draft> drafts;
    private final LayoutInflater mInflater;
    private final LinearLayout layout;
    private final DraftFragment.OnDraftChangedListener callback;

    DraftListAdapter(Context context, List<Draft> drafts, DraftFragment.OnDraftChangedListener callback, LinearLayout layout) {
        this.context = context;
        this.drafts = drafts;
        this.callback = callback;
        this.layout = layout;
        this.mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return drafts.size();
    }

    public Draft getItem(int position) {
        return drafts.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void onClick(View view) {
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_draft, null);
            holder = new ViewHolder();
            holder.row = convertView.findViewById(R.id.draft_list_item_row);
            holder.account = convertView.findViewById(R.id.draft_list_account);
            holder.label = convertView.findViewById(R.id.draft_list_label);
            holder.published = convertView.findViewById(R.id.draft_list_type_published);
            holder.update = convertView.findViewById(R.id.draftUpdate);
            holder.delete = convertView.findViewById(R.id.draftDelete);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Draft draft = drafts.get(position);
        if (draft != null) {

            holder.position = position;

            // Color of row.
            int color = context.getResources().getColor(R.color.listRowBackgroundColor);
            holder.row.setBackgroundColor(color);

            // Account
            String account = Utility.stripEndingSlash(draft.getAccount());
            holder.account.setText(String.format(context.getString(R.string.account_name), account));

            // Label
            String label = "";
            if (draft.getName().length() > 0) {
                label = draft.getName();
            } else if (draft.getBody().length() > 0) {
                label = draft.getBody();
            }
            if (label.length() > 40) {
                label = label.substring(0, 40) + " ...";
            }
            holder.label.setText(label);

            // Published.
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            @SuppressLint("SimpleDateFormat")
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MM yyyy HH:mm");
            try {
                holder.published.setVisibility(View.VISIBLE);
                Date result = formatIn.parse(draft.getTimestamp());
                holder.published.setText(String.format(context.getString(R.string.draft_last_edit), draft.getType(), formatOut.format(result)));
            } catch (ParseException ignored) {
                holder.published.setVisibility(View.GONE);
            }

            holder.update.setOnClickListener(new OnUpdateClickListener(position));
            holder.delete.setOnClickListener(new OnDeleteClickListener(position));

        }

        return convertView;
    }

    public static class ViewHolder {
        int position;
        TextView account;
        TextView label;
        TextView published;
        Button update;
        Button delete;
        LinearLayout row;
    }

    // Update listener.
    class OnUpdateClickListener implements OnClickListener {

        final int position;

        OnUpdateClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Draft draft = drafts.get(this.position);
            Intent startActivity = null;

            switch (draft.getType()) {
                case "note":
                    startActivity = new Intent(context, NoteActivity.class);
                    break;
                case "article":
                    startActivity = new Intent(context, ArticleActivity.class);
                    break;
                case "like":
                    startActivity = new Intent(context, LikeActivity.class);
                    break;
                case "bookmark":
                    startActivity = new Intent(context, BookmarkActivity.class);
                    break;
                case "reply":
                    startActivity = new Intent(context, ReplyActivity.class);
                    break;
                case "repost":
                    startActivity = new Intent(context, RepostActivity.class);
                    break;
                case "event":
                    startActivity = new Intent(context, EventActivity.class);
                    break;
                case "rsvp":
                    startActivity = new Intent(context, RsvpActivity.class);
                    break;
                case "read":
                    startActivity = new Intent(context, ReadActivity.class);
                    break;
                case "issue":
                    startActivity = new Intent(context, IssueActivity.class);
                    break;
                case "checkin":
                    startActivity = new Intent(context, CheckinActivity.class);
                    break;
                case "geocache":
                    startActivity = new Intent(context, GeocacheActivity.class);
                    break;
            }

            if (startActivity != null) {
                startActivity.putExtra("account", draft.getAccount());
                startActivity.putExtra("draftId", draft.getId());
                ((Activity) context).startActivityForResult(startActivity, POST_DRAFT);
            }
        }
    }

    // Delete listener.
    class OnDeleteClickListener implements OnClickListener {

        final int position;

        OnDeleteClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final Draft draft = drafts.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.draft_delete_confirm));
            builder.setPositiveButton(context.getString(R.string.delete_post), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.deleteDraft(draft.getId());
                    drafts.remove(position);
                    notifyDataSetChanged();
                    callback.onDraftChanged();
                    Snackbar.make(layout, context.getString(R.string.draft_deleted), Snackbar.LENGTH_SHORT).show();
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

}