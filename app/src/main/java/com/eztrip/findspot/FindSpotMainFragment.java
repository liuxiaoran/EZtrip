package com.eztrip.findspot;

import android.app.FragmentManager;
import android.app.SearchManager;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.SearchView;
import android.widget.Spinner;

import com.eztrip.R;

/**
 * Created by liuxiaoran on 2015/2/25.
 */
public class FindSpotMainFragment extends Fragment {

    public static Context context;
    public static String TAG = "FindSpotMainFragment";
    public static FindSpotMainFragment newInstance(Context context) {
        FindSpotMainFragment.context = context;
        return new FindSpotMainFragment();
    }

    /**
     * 在进入这个fragment时，有系统的搜索框可以搜索景点
     *
     * @param menu
     * @param inflater
     */
    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.search, menu);
        // Associate searchable configuration with the SearchView 
        SearchManager searchManager = (SearchManager) context.getSystemService(Context.SEARCH_SERVICE);
        SearchView searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setSearchableInfo(searchManager.getSearchableInfo(getActivity().getComponentName()));


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//必须在oncreate中setHasOptionsMenu（）表示愿意增添item到actionbar中，否则fragment接受不到oncreateoptionmenu函数
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        String[] levelData = {"A级", "AA级", "AAA级", "4A级", "5A级"};

        View view = inflater.inflate(R.layout.findspot_fragment_main, null);

        Spinner spinner = (Spinner) view.findViewById(R.id.findspot_level_spn);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, levelData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                changeLevelResultFragment(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
        return view;
    }

    private void changeLevelResultFragment(int position) {
        //向fragment传递数据
        Bundle bundle = new Bundle();
        bundle.putString("level", Integer.toString(position + 1));
        // 创建Fragment对象
        LevelResultFragment levelResultFragment = new LevelResultFragment();

        // 向Fragment传入参数
        levelResultFragment.setArguments(bundle);

        android.support.v4.app.FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.findspot_framelayout, levelResultFragment).commit();
    }
}


