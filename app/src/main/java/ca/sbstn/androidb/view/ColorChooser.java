package ca.sbstn.androidb.view;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ColorChooser extends LinearLayout {
    private List<String> colors;
    private OnColorSelectedListener onColorSelectedListener;

    public ColorChooser(Context context) {
        super(context);
    }

    public ColorChooser(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ColorChooser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public ColorChooser(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        this.colors = new ArrayList<>();
    }

    public void refresh() {
        this.removeAllViews();

        for (String color : this.colors) {
            this.addView();
        }
    }

    public void setColors(String[] colors) {
        this.colors = new ArrayList<>(Arrays.asList(colors));

    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public List<String> getColors() {
        return this.colors;
    }

    public void setOnColorSelectedListener(OnColorSelectedListener listener) {
        this.onColorSelectedListener = listener;
    }

    public void removeOnColorSelectedListener() {
        this.onColorSelectedListener = null;
    }

    public void setSelectedColor(String color) {

    }

    public String getSelectedColor() {

    }

    public void setSelectedColor(int index) {

    }

    public int getSelectedColorIndex() {

    }

    public interface OnColorSelectedListener {
        public void onColorSelected(String color);
    }
}
