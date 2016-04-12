package ca.sbstn.androidb.fragment;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import ca.sbstn.androidb.R;
import ca.sbstn.androidb.activity.AndroiDB;

public class SplashScreenFragment extends Fragment {
    public SplashScreenFragment() {}

    public static SplashScreenFragment newInstance() {
        SplashScreenFragment fragment = new SplashScreenFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.splash_screen, null);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        ((AndroiDB) getActivity()).setShowActionbar(false);
    }

    @Override
    public void onDetach() {
        super.onDetach();

        ((AndroiDB) getActivity()).setShowActionbar(true);
    }
}
