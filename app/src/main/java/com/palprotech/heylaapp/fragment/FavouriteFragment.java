package com.palprotech.heylaapp.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.palprotech.heylaapp.R;

/**
 * Created by Narendar on 28/10/17.
 */

public class FavouriteFragment extends Fragment {

    private View rootView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_favourite, container, false);

        return rootView;
    }
}
