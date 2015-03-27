package com.eztrip.TravelHelper;

import android.content.Context;
import android.support.v4.app.Fragment;

/**
 * Created by liuxiaoran on 15/3/24.
 */
public class TravelHelpFragment extends Fragment {

    private static Context context;

    public static TravelHelpFragment newInstance(Context context) {
        TravelHelpFragment.context = context;
        return new TravelHelpFragment();
    }

}
