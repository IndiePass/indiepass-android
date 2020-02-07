package com.indieweb.indigenous.micropub.post;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.indieweb.indigenous.R;
import com.indieweb.indigenous.photoeditor.EditImageActivity;

import java.util.List;

public class ImageGalleryAdapter extends RecyclerView.Adapter<ImageGalleryAdapter.MyViewHolder> {

    private List<Uri> image;
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

    public ImageGalleryAdapter(Activity activity, List<Uri> image, List<String> captions, boolean isMediaRequest) {
        this.context = activity;
        this.image = image;
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
        Uri uri = image.get(position);
        if (!isMediaRequest) {
            holder.thumbnail.setOnClickListener(new OnImageClickListener(position));
        }
        Glide.with(context).load(uri).thumbnail(0.5f).into(holder.thumbnail);
    }

    @Override
    public int getItemCount() {
        return image.size();
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

            PopupMenu popup = new PopupMenu(context, v);
            Menu menu = popup.getMenu();
            popup.getMenuInflater().inflate(R.menu.image_list_item_menu, menu);
            popup.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                public boolean onMenuItemClick(final MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.image_edit:
                            Intent imageEdit = new Intent(context, EditImageActivity.class);
                            imageEdit.putExtra("imageUri", image.get(position).toString());
                            context.startActivity(imageEdit);
                            break;
                        case R.id.image_caption:
                            builder.setTitle(context.getString(R.string.set_caption));
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
                            builder.setPositiveButton(context.getString(R.string.save), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    String caption = input.getText().toString();
                                    captions.set(position, caption);
                                    dialog.dismiss();
                                }
                            });
                            builder.setNegativeButton(context.getString(R.string.cancel), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    dialog.dismiss();
                                }
                            });
                            builder.show();
                            break;
                        case R.id.image_delete:
                            builder.setTitle(context.getString(R.string.delete_image_confirm));
                            builder.setPositiveButton(context.getString(R.string.delete), new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    image.remove(position);
                                    captions.remove(position);
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
                            break;
                    }

                    return true;
                }
            });
            popup.show();


        }
    }

}