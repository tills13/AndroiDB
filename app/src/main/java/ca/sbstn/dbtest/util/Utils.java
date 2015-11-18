package ca.sbstn.dbtest.util;

import android.content.res.Resources;
import android.util.TypedValue;

/**
 * Created by tills13 on 11/17/2015.
 */
public class Utils {
    public static float dpToPixels(Resources resources, int dp) {
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, resources.getDisplayMetrics());
        return px;
    }

    public static float pixelsToDp(Resources resources, int pixels) {
        float dp = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_PX, pixels, resources.getDisplayMetrics());
        return dp;
    }
}
