package com.indieweb.indiepass.post;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.indieweb.indiepass.R;

import java.util.List;

public class VideoGalleryAdapter extends RecyclerView.Adapter<VideoGalleryAdapter.MyViewHolder> {

    private final List<Uri> video;
    private final Context context;
    private final boolean isMediaRequest;

    static class MyViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;

        MyViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.imagePreview);
        }
    }

    public VideoGalleryAdapter(Activity activity, List<Uri> video, boolean isMediaRequest) {
        this.context = activity;
        this.video = video;
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
        Uri uri = video.get(position);
        if (!isMediaRequest) {
            holder.thumbnail.setOnClickListener(new OnVideoClickListener(position));
        }
        Glide.with(context).load(uri).thumbnail(0.5f).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return video.size();
    }

    // Video click listener.
    class OnVideoClickListener implements View.OnClickListener {

        final int position;

        OnVideoClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.delete_video_confirm));
            builder.setPositiveButton(context.getString(R.string.delete),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    video.remove(position);
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