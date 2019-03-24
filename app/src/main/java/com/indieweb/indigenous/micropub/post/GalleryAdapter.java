package com.indieweb.indigenous.micropub.post;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.indieweb.indigenous.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {

    private List<Uri> images;
    private List<String> captions;
    private Context context;
    private boolean isMediaRequest;

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;

        MyViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.imagePreview);
        }
    }

    public GalleryAdapter(Activity activity, List<Uri> images, List<String> captions, boolean isMediaRequest) {
        this.context = activity;
        this.images = images;
        this.captions = captions;
        this.isMediaRequest = isMediaRequest;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resource = R.layout.gallery_thumbnail;
        if (isMediaRequest) {
            resource = R.layout.gallery_thumbnail_single;
        }
        View itemView = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Uri uri = images.get(position);
        if (!isMediaRequest) {
            holder.thumbnail.setOnClickListener(new OnImageClickListener(position));
        }
        Glide.with(context).load(uri).thumbnail(0.5f).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    // Image click listener.
    class OnImageClickListener implements View.OnClickListener {

        int position;

        OnImageClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {
            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle("Set caption or remove image");

            View view = LayoutInflater.from(context).inflate(R.layout.dialog_single_input, null);
            final EditText input = view.findViewById(R.id.editText);
            String defaultCaption = captions.get(position);
            if (defaultCaption.length() > 0) {
                input.setText(defaultCaption);
            }
            input.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    input.post(new Runnable() {
                        @Override
                        public void run() {
                            InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
                            inputMethodManager.showSoftInput(input, InputMethodManager.SHOW_IMPLICIT);
                        }
                    });
                }
            });
            builder.setView(view);

            builder.setPositiveButton("Save and close", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    String caption = input.getText().toString();
                    captions.set(position, caption);
                    dialog.dismiss();
                }
            });
            builder.setNegativeButton(context.getString(R.string.delete_image), new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    images.remove(position);
                    captions.remove(position);
                    notifyDataSetChanged();
                }
            });
            builder.show();
        }
    }

}