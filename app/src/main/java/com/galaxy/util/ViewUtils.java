package com.galaxy.util;

import android.content.Context;
import android.util.DisplayMetrics;

public class ViewUtils {
    public static float convertDpToPixel(float dp, Context context){
        return dp * getDensity(context);
    }

    public static float getDensity(Context context) {
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        return metrics.density;
    }
}
