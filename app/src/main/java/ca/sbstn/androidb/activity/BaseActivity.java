package ca.sbstn.androidb.activity;

import android.animation.ArgbEvaluator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.application.*;
import ca.sbstn.androidb.application.AndroiDB;
import ca.sbstn.androidb.util.Colours;

/**
 * Created by tyler on 21/04/16.
 */
public class BaseActivity extends AppCompatActivity {
    protected Toolbar toolbar;
    protected SharedPreferences sharedPreferences;
    protected FragmentManager fragmentManager;
    private ValueAnimator actionbarAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.toolbar = (Toolbar) this.findViewById(R.id.toolbar);
        this.setSupportActionBar(this.toolbar);

        this.fragmentManager = this.getSupportFragmentManager();
        this.sharedPreferences = this.getSharedPreferences(AndroiDB.SHARED_PREFS_KEY, Context.MODE_PRIVATE);
    }

    public SharedPreferences getSharedPreferences() {
        return this.sharedPreferences;
    }

    public void putContextFragment(Fragment fragment, boolean addToBackStack) {
        this.putContextFragment(fragment, addToBackStack, R.anim.slide_in, R.anim.slide_out);
    }

    public void putContextFragment(Fragment fragment, boolean addToBackStack, int animIn, int animOut) {
        FragmentTransaction fragmentTransaction = this.fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.context_fragment, fragment);

        if (addToBackStack) {
            fragmentTransaction.addToBackStack(null);
        }

        fragmentTransaction.setCustomAnimations(animIn, animOut);
        fragmentTransaction.commit();
    }

    public void putDetailsFragment(Fragment fragment, boolean replaceContext) {
        if (this.findViewById(R.id.details_fragment) == null) {
            this.putContextFragment(fragment, true);
        } else {
            Fragment currentDetails = this.fragmentManager.findFragmentById(R.id.details_fragment);

            if (replaceContext) {
                if (currentDetails != null) {
                    this.fragmentManager.popBackStackImmediate(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                    this.fragmentManager.beginTransaction()
                            .remove(currentDetails)
                            .commit();

                    this.fragmentManager.executePendingTransactions();

                    this.fragmentManager.beginTransaction()
                            .add(R.id.context_fragment, currentDetails)
                            //.addToBackStack(null)
                            .commit();
                }
            }

            this.fragmentManager.beginTransaction()
                    .replace(R.id.details_fragment, fragment)
                    //.addToBackStack(null)
                    .commit();
        }
    }

    public void setToolbarTitle(String title) {
        if (this.getSupportActionBar() != null) {
           this.getSupportActionBar().setTitle(title);
        } else this.toolbar.setTitle(title);
    }

    public void setToolbarSubtitle(String subtitle) {
        if (this.getSupportActionBar() != null) {
            this.getSupportActionBar().setSubtitle(subtitle);
        } else this.toolbar.setSubtitle(subtitle);
    }

    public void setToolbarColor(String color, boolean animate, boolean setStatusBar) {
        this.setToolbarColor(Color.parseColor(color), animate, setStatusBar);
    }

    public void setToolbarColor(String color, boolean setStatusBar) {
        this.setToolbarColor(color, false, setStatusBar);
    }

    public void setToolbarColor(String color) {
        this.setToolbarColor(Color.parseColor(color));
    }

    public void setToolbarColor(int color) {
        this.setToolbarColor(color, false);
    }

    public void setToolbarColor(int color, boolean animate) {
        this.setToolbarColor(color, animate, false);
    }

    public void setToolbarColor(int color, boolean animate, final boolean setStatusBar) {
        if (animate) {
            Drawable toolbarBackground = this.toolbar.getBackground();
            int currentColor;

            if (toolbarBackground instanceof ColorDrawable) {
                currentColor = ((ColorDrawable) toolbarBackground).getColor();
            } else {
                this.toolbar.setBackgroundColor(color);
                return;
            }

            if (this.actionbarAnimator != null && this.actionbarAnimator.isRunning()) {
                currentColor = (Integer) this.actionbarAnimator.getAnimatedValue();
                this.actionbarAnimator.cancel();
            }

            if (currentColor == color) return;

            this.actionbarAnimator = ValueAnimator.ofObject(new ArgbEvaluator(), currentColor, color);
            this.actionbarAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                @Override
                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                    toolbar.setBackgroundColor((Integer) valueAnimator.getAnimatedValue());
                    if (setStatusBar) {
                        getWindow().setStatusBarColor(Colours.darken((Integer) valueAnimator.getAnimatedValue()));
                    }
                }
            });

            this.actionbarAnimator.setDuration(500);
            this.actionbarAnimator.setStartDelay(0);
            this.actionbarAnimator.start();
        } else {
            this.toolbar.setBackgroundColor(color);

            if (setStatusBar) {
                getWindow().setStatusBarColor(Colours.darken(color));
            }
        }
    }
}
