package com.indieweb.indigenous.photoeditor.filters;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.indieweb.indigenous.R;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import ja.burhanrashid52.photoeditor.PhotoFilter;

public class FilterViewAdapter extends RecyclerView.Adapter<FilterViewAdapter.ViewHolder> {

    private final FilterListener mFilterListener;
    private final List<Pair<String, PhotoFilter>> mPairList = new ArrayList<>();

    public FilterViewAdapter(FilterListener filterListener) {
        mFilterListener = filterListener;
        setupFilters();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.photoeditor_row_filter_view, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Pair<String, PhotoFilter> filterPair = mPairList.get(position);
        Bitmap fromAsset = getBitmapFromAsset(holder.itemView.getContext(), filterPair.first);
        holder.mImageFilterView.setImageBitmap(fromAsset);
        holder.mTxtFilterName.setText(filterPair.second.name().replace("_", " "));
    }

    @Override
    public int getItemCount() {
        return mPairList.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        final ImageView mImageFilterView;
        final TextView mTxtFilterName;

        ViewHolder(View itemView) {
            super(itemView);
            mImageFilterView = itemView.findViewById(R.id.imgFilterView);
            mTxtFilterName = itemView.findViewById(R.id.txtFilterName);
            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mFilterListener.onFilterSelected(mPairList.get(getLayoutPosition()).second);
                }
            });
        }
    }

    private Bitmap getBitmapFromAsset(Context context, String strName) {
        AssetManager assetManager = context.getAssets();
        InputStream istr;
        try {
            istr = assetManager.open(strName);
            return BitmapFactory.decodeStream(istr);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    private void setupFilters() {
        mPairList.add(new Pair<>("filters/original.webp", PhotoFilter.NONE));
        mPairList.add(new Pair<>("filters/auto_fix.webp", PhotoFilter.AUTO_FIX));
        mPairList.add(new Pair<>("filters/brightness.webp", PhotoFilter.BRIGHTNESS));
        mPairList.add(new Pair<>("filters/contrast.webp", PhotoFilter.CONTRAST));
        mPairList.add(new Pair<>("filters/documentary.webp", PhotoFilter.DOCUMENTARY));
        mPairList.add(new Pair<>("filters/dual_tone.webp", PhotoFilter.DUE_TONE));
        mPairList.add(new Pair<>("filters/fill_light.webp", PhotoFilter.FILL_LIGHT));
        mPairList.add(new Pair<>("filters/fish_eye.webp", PhotoFilter.FISH_EYE));
        mPairList.add(new Pair<>("filters/grain.webp", PhotoFilter.GRAIN));
        mPairList.add(new Pair<>("filters/gray_scale.webp", PhotoFilter.GRAY_SCALE));
        mPairList.add(new Pair<>("filters/lomish.webp", PhotoFilter.LOMISH));
        mPairList.add(new Pair<>("filters/negative.webp", PhotoFilter.NEGATIVE));
        mPairList.add(new Pair<>("filters/posterize.webp", PhotoFilter.POSTERIZE));
        mPairList.add(new Pair<>("filters/saturate.webp", PhotoFilter.SATURATE));
        mPairList.add(new Pair<>("filters/sepia.webp", PhotoFilter.SEPIA));
        mPairList.add(new Pair<>("filters/sharpen.webp", PhotoFilter.SHARPEN));
        mPairList.add(new Pair<>("filters/temperature.webp", PhotoFilter.TEMPERATURE));
        mPairList.add(new Pair<>("filters/tint.webp", PhotoFilter.TINT));
        mPairList.add(new Pair<>("filters/vignette.webp", PhotoFilter.VIGNETTE));
        mPairList.add(new Pair<>("filters/cross_process.webp", PhotoFilter.CROSS_PROCESS));
        mPairList.add(new Pair<>("filters/b_n_w.webp", PhotoFilter.BLACK_WHITE));
        mPairList.add(new Pair<>("filters/flip_horizontal.webp", PhotoFilter.FLIP_HORIZONTAL));
        mPairList.add(new Pair<>("filters/flip_vertical.webp", PhotoFilter.FLIP_VERTICAL));
        mPairList.add(new Pair<>("filters/rotate.webp", PhotoFilter.ROTATE));
    }
}
