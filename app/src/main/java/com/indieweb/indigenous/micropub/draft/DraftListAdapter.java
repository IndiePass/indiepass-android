package com.indieweb.indigenous.micropub.draft;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.db.DatabaseHelper;
import com.indieweb.indigenous.micropub.MicropubActionDelete;
import com.indieweb.indigenous.micropub.post.ArticleActivity;
import com.indieweb.indigenous.micropub.post.NoteActivity;
import com.indieweb.indigenous.micropub.post.ReplyActivity;
import com.indieweb.indigenous.micropub.post.UpdateActivity;
import com.indieweb.indigenous.micropub.source.PostListAdapter;
import com.indieweb.indigenous.microsub.timeline.TimelineActivity;
import com.indieweb.indigenous.model.Channel;
import com.indieweb.indigenous.model.Draft;
import com.indieweb.indigenous.model.PostListItem;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Drafts list adapter.
 */
public class DraftListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<Draft> drafts;
    private LayoutInflater mInflater;

    public DraftListAdapter(Context context, List<Draft> drafts) {
        this.context = context;
        this.drafts = drafts;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
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

    public void onClick(View view) { }

    public static class ViewHolder {
        public int position;
        public TextView label;
        public TextView published;
        public Button update;
        public Button delete;
        public LinearLayout row;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.draft_list_item, null);
            holder = new ViewHolder();
            holder.row = convertView.findViewById(R.id.draft_list_item_row);
            holder.label = convertView.findViewById(R.id.draft_list_label);
            holder.published = convertView.findViewById(R.id.draft_list_published);
            holder.update = convertView.findViewById(R.id.draftUpdate);
            holder.delete = convertView.findViewById(R.id.draftDelete);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Draft draft = drafts.get(position);
        if (draft != null) {

            holder.position = position;

            // Color of row.
            int color = context.getResources().getColor(R.color.listRowBackgroundColor);
            holder.row.setBackgroundColor(color);

            // Label
            String label = draft.getType();
            if (draft.getName().length() > 0) {
                label = draft.getName();
            }
            else if (draft.getBody().length() > 0) {
                label = draft.getBody();
            }
            if (label.length() > 40) {
                label = label.substring(0, 40) + " ...";
            }
            holder.label.setText(label);

            // Published.
            SimpleDateFormat formatIn = new SimpleDateFormat("yyyy-MM-dd kk:mm:ss");
            SimpleDateFormat formatOut = new SimpleDateFormat("dd MMM yyyy kk:mm");
            Date result;
            try {
                result = formatIn.parse(draft.getPublished());
                holder.published.setVisibility(View.VISIBLE);
                holder.published.setText(formatOut.format(result));
            }
            catch (ParseException ignored) {
                holder.published.setVisibility(View.GONE);
            }

            holder.update.setOnClickListener(new OnUpdateClickListener(position));
            holder.delete.setOnClickListener(new OnDeleteClickListener(position));

        }

        return convertView;
    }

    // Update listener.
    class OnUpdateClickListener implements OnClickListener {

        int position;

        OnUpdateClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Draft draft = drafts.get(this.position);

            switch (draft.getType()) {
                case "note":
                    Intent startNote = new Intent(context, NoteActivity.class);
                    startNote.putExtra("draftId", draft.getId());
                    context.startActivity(startNote);
                    break;
                case "article":
                    Intent startArticle = new Intent(context, ArticleActivity.class);
                    startArticle.putExtra("draftId", draft.getId());
                    context.startActivity(startArticle);
                    break;
                case "reply":
                    Intent startReply = new Intent(context, ReplyActivity.class);
                    startReply.putExtra("draftId", draft.getId());
                    context.startActivity(startReply);
                    break;
            }
        }
    }

    // Delete listener.
    class OnDeleteClickListener implements OnClickListener {

        int position;

        OnDeleteClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final Draft draft = drafts.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Are you sure you want to delete this draft ?");
            builder.setPositiveButton(context.getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    DatabaseHelper db = new DatabaseHelper(context);
                    db.deleteDraft(draft.getId());
                    drafts.remove(position);
                    notifyDataSetChanged();
                    Toast.makeText(context, context.getString(R.string.draft_deleted), Toast.LENGTH_SHORT).show();
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