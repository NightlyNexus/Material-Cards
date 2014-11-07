package com.brianco.materialcards.adapter;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Vibrator;
import android.support.v7.widget.MyRoundRectDrawableWithShadow;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.brianco.materialcards.HoverService;
import com.brianco.materialcards.R;
import com.brianco.materialcards.model.PaletteColor;

import java.util.List;

public class CardAdapter extends RecyclerView.Adapter<CardAdapter.ViewHolder> {

    private final Context mContext;
    private final Resources mResources;
    private final List<PaletteColor> mColorList;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private final View cardView;
        private final TextView baseColorView;
        private final TextView colorHexView;
        private final ImageView copyView;

        public ViewHolder(final View view) {
            super(view);
            cardView = view;
            baseColorView = (TextView) cardView.findViewById(R.id.base_name);
            colorHexView = (TextView) cardView.findViewById(R.id.hex_value);
            copyView = (ImageView) cardView.findViewById(R.id.copy_icon);
        }
    }

    public CardAdapter(final Context context, final List<PaletteColor> colorList) {
        mContext = context;
        mResources = mContext.getResources();
        mColorList = colorList;
    }

    @Override
    public CardAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        final View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onViewRecycled(ViewHolder holder) {
        super.onViewRecycled(holder);
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        final PaletteColor color = mColorList.get(position);
        final String parentColorName = color.getParentName();
        final String colorBaseName = color.getBaseName();
        final String colorHexString = color.getHexString();
        final int colorHex = color.getHex();
        final boolean dark = color.isDark();
        final int parentHex = color.getParentHex();
        final int textColorResource = dark ? R.color.md_white_1000 : R.color.md_black_1000;
        final int textColor = mResources.getColor(textColorResource);
        final int copyIconResource = dark ? R.drawable.ic_action_copy_dark : R.drawable.ic_action_copy_light;
        // holder.cardView.setBackgroundColor(colorHex);
        final MyRoundRectDrawableWithShadow backgroundHack
                = new MyRoundRectDrawableWithShadow(mResources, colorHex, 0,
                mResources.getDimension(R.dimen.shadow_size_hack),
                        mResources.getDimension(R.dimen.max_shadow_size_hack));
        holder.cardView.setBackground(backgroundHack);
        holder.colorHexView.setText(colorHexString);
        holder.colorHexView.setTextColor(textColor);
        holder.baseColorView.setText(colorBaseName);
        holder.baseColorView.setTextColor(textColor);
        holder.copyView.setImageResource(copyIconResource);
        holder.copyView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PaletteColor.copyColorToClipboard(mContext, parentColorName, colorBaseName, colorHexString);
            }
        });
        holder.cardView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                final Vibrator vibrator = (Vibrator) mContext.getSystemService(Context.VIBRATOR_SERVICE);
                vibrator.vibrate(mResources.getInteger(R.integer.vibrate_long_press));
                final Intent intent = new Intent(mContext, HoverService.class);
                intent.putExtra(HoverService.COLOR_VALUE_EXTRA, colorHex);
                intent.putExtra(HoverService.COLOR_NAME_EXTRA, colorHexString);
                intent.putExtra(HoverService.COLOR_BASE_NAME_EXTRA, colorBaseName);
                intent.putExtra(HoverService.COLOR_PARENT_NAME_EXTRA, parentColorName);
                intent.putExtra(HoverService.COLOR_PARENT_VALUE_EXTRA, parentHex);
                mContext.startService(intent);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mColorList.size();
    }
}
