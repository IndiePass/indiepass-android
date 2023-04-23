package com.indieweb.indigenous.contacts;

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
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.indieweb.indigenous.Indigenous;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.indieweb.micropub.MicropubAction;
import com.indieweb.indigenous.post.ContactActivity;
import com.indieweb.indigenous.model.Contact;
import com.indieweb.indigenous.model.User;

import java.util.List;

import static com.indieweb.indigenous.MainActivity.UPDATE_POST;

/**
 * Contact list adapter.
 */
public class ContactListAdapter extends BaseAdapter implements OnClickListener {

    private final Context context;
    private final List<Contact> contacts;
    private final LayoutInflater mInflater;
    private final User user;
    private final RelativeLayout layout;

    ContactListAdapter(Context context, List<Contact> contacts, User user, RelativeLayout layout) {
        this.context = context;
        this.contacts = contacts;
        this.user = user;
        this.layout = layout;
        this.mInflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }

    public int getCount() {
        return contacts.size();
    }

    public Contact getItem(int position) {
        return contacts.get(position);
    }

    public long getItemId(int position) {
        return position;
    }

    public void onClick(View view) { }

    public static class ViewHolder {
        int position;
        TextView name;
        TextView nickname;
        TextView url;
        ImageView photo;
        Button update;
        Button delete;
        LinearLayout row;
    }

    public View getView(final int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;

        if (convertView == null) {
            convertView = mInflater.inflate(R.layout.list_item_contact, null);
            holder = new ViewHolder();
            holder.row = convertView.findViewById(R.id.contact_list_item_row);
            holder.name = convertView.findViewById(R.id.contact_name);
            holder.photo = convertView.findViewById(R.id.contact_photo);
            holder.nickname = convertView.findViewById(R.id.contact_nickname);
            holder.url = convertView.findViewById(R.id.contact_url);
            holder.update = convertView.findViewById(R.id.contactUpdate);
            holder.delete = convertView.findViewById(R.id.contactDelete);
            convertView.setTag(holder);
        }
        else {
            holder = (ViewHolder) convertView.getTag();
        }

        final Contact contact = contacts.get(position);
        if (contact != null) {

            holder.position = position;

            // Color of row.
            int color = context.getResources().getColor(R.color.listRowBackgroundColor);
            holder.row.setBackgroundColor(color);

            // Photo
            Glide.with(context)
                    .load(contact.getPhoto())
                    .apply(RequestOptions.circleCropTransform().placeholder(R.drawable.avatar))
                    .into(holder.photo);

            // Name
            String name = context.getString(R.string.contact_no_name);
            if (contact.getName().length() > 0) {
                name = contact.getName();
            }
            holder.name.setText(name);

            // Nickname
            if (contact.getNickname().length() > 0) {
                holder.nickname.setVisibility(View.VISIBLE);
                holder.nickname.setText(contact.getNickname());
            }
            else {
                holder.nickname.setVisibility(View.GONE);
            }

            // Nickname
            if (contact.getUrl().length() > 0) {
                holder.url.setVisibility(View.VISIBLE);
                holder.url.setText(contact.getUrl());
            }
            else {
                holder.url.setVisibility(View.GONE);
            }

            if (contact.getInternalUrl().length() > 0) {
                holder.update.setVisibility(View.VISIBLE);
                holder.delete.setVisibility(View.VISIBLE);
                holder.update.setOnClickListener(new OnUpdateClickListener(position));
                holder.delete.setOnClickListener(new OnDeleteClickListener(position));
            }
            else {
                holder.update.setVisibility(View.GONE);
                holder.delete.setVisibility(View.GONE);
            }

        }

        return convertView;
    }

    // Update listener.
    class OnUpdateClickListener implements OnClickListener {

        final int position;

        OnUpdateClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            Contact contact = contacts.get(this.position);
            Indigenous app = Indigenous.getInstance();
            app.setContact(contact);
            Intent startActivity =  new Intent(context, ContactActivity.class);
            startActivity.putExtra("updateContact", true);
            ((Activity) context).startActivityForResult(startActivity, UPDATE_POST);
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
            final Contact contact = contacts.get(this.position);

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.contact_delete_confirm));
            builder.setPositiveButton(context.getString(R.string.delete_post),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    new MicropubAction(context, user, layout).deleteItem(contact.getInternalUrl());
                    contacts.remove(position);
                    notifyDataSetChanged();
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