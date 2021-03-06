package com.brianco.materialcards.adapter;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.RippleDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.brianco.materialcards.R;
import com.brianco.materialcards.model.PaletteColorSection;

import java.util.List;

public class DrawerAdapter extends ArrayAdapter<PaletteColorSection> {

    private final LayoutInflater mLayoutInflater;
    private List<PaletteColorSection> mColorList;

    public DrawerAdapter(Context context, List<PaletteColorSection> colorList) {
        super(context, 0, colorList);
        mLayoutInflater = LayoutInflater.from(context);
        mColorList = colorList;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        final ViewHolder holder;
        if (convertView == null) {
            convertView = mLayoutInflater.inflate(R.layout.nav_item, parent, false);
            holder = new ViewHolder((TextView) convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        final PaletteColorSection paletteColorSection = mColorList.get(position);
        final String colorName = paletteColorSection.getColorSectionName();
        final int colorValue = paletteColorSection.getColorSectionValue();
        holder.textView.setText(colorName);
        final StateListDrawable sld = new StateListDrawable();
        final Drawable d = new ColorDrawable(colorValue);
        final Drawable r = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
                ? new RippleDrawable(new ColorStateList(new int[][]{{}},
                new int[]{colorValue}), null, null)
                : d;
        sld.addState(new int[] { android.R.attr.state_pressed }, r);
        sld.addState(new int[] { android.R.attr.state_checked }, d);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            holder.textView.setBackground(sld);
        } else {
            holder.textView.setBackgroundDrawable(sld);
        }
        return convertView;
    }

    private static final class ViewHolder {
        private final TextView textView;
        private ViewHolder(TextView textView) {
            this.textView = textView;
        }
    }
}
