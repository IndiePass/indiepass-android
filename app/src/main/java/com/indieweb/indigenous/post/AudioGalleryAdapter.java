package com.indieweb.indigenous.post;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.indieweb.indigenous.R;
import com.indieweb.indigenous.util.Utility;

import java.util.List;

public class AudioGalleryAdapter extends RecyclerView.Adapter<AudioGalleryAdapter.MyViewHolder> {

    private final List<Uri> audio;
    private final Context context;
    private final boolean isMediaRequest;

    class MyViewHolder extends RecyclerView.ViewHolder {
        final ImageView thumbnail;
        final TextView audioName;

        MyViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.audioIcon);
            audioName = view.findViewById(R.id.audioName);
        }
    }

    public AudioGalleryAdapter(Activity activity, List<Uri> audio, boolean isMediaRequest) {
        this.context = activity;
        this.audio = audio;
        this.isMediaRequest = isMediaRequest;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        int resource = R.layout.gallery_thumbnail_audio;
        if (isMediaRequest) {
            resource = R.layout.gallery_thumbnail_audio_single;
        }
        View itemView = LayoutInflater.from(parent.getContext()).inflate(resource, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        if (!isMediaRequest) {
            holder.thumbnail.setOnClickListener(new OnAudioClickListener(position));
        }
        holder.audioName.setText(Utility.getFilename(audio.get(position), context));
    }

    @Override
    public int getItemCount() {
        return audio.size();
    }

    // Audio click listener.
    class OnAudioClickListener implements View.OnClickListener {

        final int position;

        OnAudioClickListener(int position) {
            this.position = position;
        }

        @Override
        public void onClick(View v) {

            final AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setTitle(context.getString(R.string.delete_audio_confirm));
            builder.setPositiveButton(context.getString(R.string.delete),new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog,int id) {
                    audio.remove(position);
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