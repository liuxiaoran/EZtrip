package com.eztrip;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by liuxiaoran on 2015/2/27.
 * 首页
 */
public class MainFragment extends Fragment {

    public static Context context;

    public static MainFragment newInstance(Context context) {
        MainFragment.context = context;
        return new MainFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {


        return null;
    }
}
