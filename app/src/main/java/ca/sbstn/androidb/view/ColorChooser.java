package ca.sbstn.androidb.view;

import android.content.Context;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.GridLayout;
import android.widget.HorizontalScrollView;
import android.widget.LinearLayout;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import ca.sbstn.androidb.R;

public class ColorChooser extends LinearLayout {
    private List<String> colors = new ArrayList<>();
    private String selectedColor;
    private OnColorSelectedListener onColorSelectedListener;

    @BindView(R.id.colors) GridLayout colorGridView;
    @BindView(R.id.scroll_view_container) HorizontalScrollView scrollView;

    public ColorChooser(Context context) {
        super(context);

        this.init();
    }

    public ColorChooser(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.init();
    }

    public ColorChooser(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        this.init();
    }

    public ColorChooser(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);

        this.init();
    }

    public void init() {
        View view = LayoutInflater.from(this.getContext()).inflate(R.layout.color_chooser, this);
        ButterKnife.bind(view);

        String[] colors = this.getContext().getResources().getStringArray(R.array.material_colors);

        this.colors = Arrays.asList(colors);
        this.setSelectedColor(0);
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();

        this.resetColorSelectors();
        this.updateSelectedColor();
    }

    @Override
    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        this.updateSelectedColor();
    }

    protected void resetColorSelectors() {
        this.colorGridView.removeAllViews();

        LayoutInflater inflater = LayoutInflater.from(this.getContext());

        for (final String color : this.colors) {
            View view = inflater.inflate(R.layout.color_selection, this, false);
            view.setBackgroundColor(Color.parseColor(color));
            // view.findViewById(R.id.icon).setVisibility(color.equals(this.getSelectedColor()) ? VISIBLE : GONE);
            view.setOnClickListener((mView) -> setSelectedColor(color));

            this.colorGridView.addView(view);
        }
    }

    public void setColors(String[] colors) {
        this.setColors(Arrays.asList(colors));
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
        this.resetColorSelectors();
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
        this.selectedColor = color;
        this.updateSelectedColor();
        this.scrollToIndex(this.colors.indexOf(color));

        if (this.onColorSelectedListener != null) {
            this.onColorSelectedListener.onColorSelected(color);
        }
    }

    public void setSelectedColor(int index) {
        this.setSelectedColor(this.colors.get(index));
    }

    public String getSelectedColor() {
        return this.selectedColor;
    }

    public int getSelectedColorIndex() {
        int index = this.colors.indexOf(this.selectedColor);
        return index >= 0 ? index : 0;
    }

    protected void updateSelectedColor() {
        int currentIndex = this.getSelectedColorIndex();
        for (int index = 0; index < this.colorGridView.getChildCount(); index++) {
            View view = this.colorGridView.getChildAt(index);

            view.findViewById(R.id.icon).setVisibility(index == currentIndex ? VISIBLE : GONE);
        }
    }

    public void scrollToIndex(int index) {
        if (this.colorGridView.getChildCount() == 0) {
            return;
        }

        int width = this.colorGridView.getChildAt(0).getWidth();
        this.scrollView.smoothScrollTo(index * width, 0);
    }

    public interface OnColorSelectedListener {
        void onColorSelected(String color);
    }
}
