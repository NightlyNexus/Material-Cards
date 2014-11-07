package android.support.v7.widget;

import android.content.res.Resources;

public class MyRoundRectDrawableWithShadow extends RoundRectDrawableWithShadow {

    public MyRoundRectDrawableWithShadow(Resources resources, int backgroundColor,
                                         float radius, float shadowSize, float maxShadowSize) {
        super(resources, backgroundColor, radius, shadowSize, maxShadowSize);
    }
}
