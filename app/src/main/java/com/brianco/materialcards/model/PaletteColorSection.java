package com.brianco.materialcards.model;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class PaletteColorSection implements Parcelable {
    private final String colorSectionName;
    private final int colorSectionValue;
    private final ArrayList<PaletteColor> paletteColorList;

    public PaletteColorSection(final String colorSectionName, final int colorSectionValue,
                               final String[] baseColorNames, final int[] colorValues) {
        if (baseColorNames.length != colorValues.length) {
            throw new RuntimeException("Must supply one-to-one base names with colors.  "
                    + colorSectionName + " has " + baseColorNames.length + " base names and "
                    + colorValues.length + " colors.");
        }
        this.colorSectionName = colorSectionName;
        this.colorSectionValue = colorSectionValue;
        paletteColorList = new ArrayList<PaletteColor>(baseColorNames.length);
        for (int i = 0; i < baseColorNames.length; i++) {
            paletteColorList.add(
                    new PaletteColor(colorValues[i], baseColorNames[i],
                            colorSectionName, colorSectionValue));
        }
    }

    public PaletteColorSection(final String colorSectionName, final int colorSectionValue,
                               final ArrayList<PaletteColor> paletteColorList) {
        this.colorSectionName = colorSectionName;
        this.colorSectionValue = colorSectionValue;
        this.paletteColorList = paletteColorList;
    }

    public String getColorSectionName() {
        return colorSectionName;
    }

    public int getColorSectionValue() {
        return colorSectionValue;
    }

    public ArrayList<PaletteColor> getPaletteColorList() {
        return paletteColorList;
    }

    public static ArrayList<PaletteColorSection> getPaletteColorSectionsList(
            final String[] colorSectionsNames,
            final int[] colorSectionsValues,
            final String[][] baseColorNames,
            final int[][] colorValues) {
        if (colorSectionsNames.length != colorSectionsValues.length
                || colorSectionsNames.length != baseColorNames.length
                || colorSectionsNames.length != colorValues.length) {
            throw new RuntimeException("Must supply one-to-one parameters.");
        }
        ArrayList<PaletteColorSection> colorList = new ArrayList<PaletteColorSection>();
        for (int i = 0; i < colorSectionsNames.length; i++) {
            colorList.add(new PaletteColorSection(colorSectionsNames[i], colorSectionsValues[i],
                    baseColorNames[i], colorValues[i]));
        }
        return colorList;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel out, int flags) {
        out.writeString(colorSectionName);
        out.writeInt(colorSectionValue);
        out.writeList(paletteColorList);
    }

    public static final Parcelable.Creator<PaletteColorSection> CREATOR
            = new Parcelable.Creator<PaletteColorSection>() {
        public PaletteColorSection createFromParcel(Parcel in) {
            final String colorSectionName = in.readString();
            final int colorSectionValue = in.readInt();
            final ArrayList<PaletteColor> paletteColorList
                    = in.readArrayList(PaletteColorSection.class.getClassLoader());
            return new PaletteColorSection(colorSectionName, colorSectionValue, paletteColorList);
        }

        public PaletteColorSection[] newArray(int size) {
            return new PaletteColorSection[size];
        }
    };
}
