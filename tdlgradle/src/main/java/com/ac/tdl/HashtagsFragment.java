package com.ac.tdl;

/**
 * Created by aaronte on 2014-05-25.
 */
import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class HashtagsFragment extends Fragment {

    public HashtagsFragment(){}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View rootView = inflater.inflate(R.layout.fragment_hashtags, container, false);

        return rootView;
    }
}