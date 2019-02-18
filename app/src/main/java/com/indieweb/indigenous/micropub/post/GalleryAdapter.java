package com.indieweb.indigenous.micropub.post;

import android.content.Context;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.indieweb.indigenous.R;

import java.util.List;

public class GalleryAdapter extends RecyclerView.Adapter<GalleryAdapter.MyViewHolder> {

    private List<Uri> images;
    private Context mContext;

    class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnail;

        MyViewHolder(View view) {
            super(view);
            thumbnail = view.findViewById(R.id.imagePreview);
        }
    }

    GalleryAdapter(Context context, List<Uri> images) {
        mContext = context;
        this.images = images;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.gallery_thumbnail, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Uri uri = images.get(position);
        Glide.with(mContext).load(uri).thumbnail(0.5f).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return images.size();
    }

}