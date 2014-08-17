package com.brianco.materialcards.model;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.widget.Toast;

import com.brianco.materialcards.R;

public class PaletteColor implements Parcelable {

    private final int hex;
    private final boolean isDark;
    private final String hexString;
    private final String baseName;
    private final String parentName;
    private final int parentHex;

    public PaletteColor(final int hex, final String baseName, final String parentName,
                        final int parentHex) {
        this.hex = hex;
        this.isDark = 0.2126 * Color.red(hex)
                + 0.7152 * Color.green(hex) + 0.0722 * Color.blue(hex) < 128;
        this.hexString = String.format("#%06X", 0xFFFFFF & this.hex);
        this.baseName = baseName;
        this.parentName = parentName;
        this.parentHex = parentHex;
    }

    public int getHex() {
        return hex;
    }

    public boolean isDark() {
        return isDark;
    }

    public String getHexString() {
        return hexString;
    }

    public String getBaseName() {
        return baseName;
    }

    public String getParentName() {
        return parentName;
    }

    public int getParentHex() {
        return parentHex;
    }

    public static void copyColorToClipboard(Context context, String parentColorName, String colorBaseName, String colorHex) {
        final ClipboardManager clipboard
                = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        final ClipData clip
                = ClipData.newPlainText(context.getString(R.string.color_clipboard, parentColorName, colorBaseName), colorHex);
        clipboard.setPrimaryClip(clip);

        Toast.makeText(context, context.getString(R.string.color_copied, colorHex),
                Toast.LENGTH_SHORT).show();
    }

    @Override
     public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeInt(hex);
        out.writeString(baseName);
        out.writeString(parentName);
        out.writeInt(parentHex);
    }

    public static final Parcelable.Creator<PaletteColor> CREATOR
            = new Parcelable.Creator<PaletteColor>() {
        public PaletteColor createFromParcel(Parcel in) {
            final int hex = in.readInt();
            final String baseName = in.readString();
            final String parentName = in.readString();
            final int parentHex = in.readInt();
            return new PaletteColor(hex, baseName, parentName, parentHex);
        }

        public PaletteColor[] newArray(int size) {
            return new PaletteColor[size];
        }
    };
}
