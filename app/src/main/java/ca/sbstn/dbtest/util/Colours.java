package ca.sbstn.dbtest.util;

import android.graphics.Color;

/**
 * Created by tills13 on 2015-11-23.
 */
public class Colours {
    public static int darken(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }
}
