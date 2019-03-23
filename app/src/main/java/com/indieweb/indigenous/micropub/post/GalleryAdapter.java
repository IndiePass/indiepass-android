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
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.indieweb.indigenous.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {

    private List<Uri> images;
    private Context context;
    private Activity activity;
    private boolean isMediaRequest;

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;

        MyViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.imagePreview);
        }
    }

    public GalleryAdapter(Context context, Activity activity, List<Uri> images, boolean isMediaRequest) {
        this.context = context;
        this.images = images;
        this.activity = activity;
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
            holder.thumbnail.setOnClickListener(new OnDeleteClickListener(position));
        }
        Glide.with(context).load(uri).thumbnail(0.5f).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

    // Delete listener.
    class OnDeleteClickListener implements View.OnClickListener {

        int position;

        OnDeleteClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Are you sure you want to delete this image ?");
            builder.setPositiveButton(context.getString(R.string.delete_image),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    images.remove(position);
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