package com.eztrip.findspot;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.eztrip.MyContext;
import com.eztrip.R;
import com.eztrip.citylist.CityList;

import java.util.ArrayList;

import utils.FindSpotService;

/**
 * Created by liuxiaoran on 2015/2/25.
 */
public class FindSpotMainFragment extends Fragment {

    public static Context context;
    public static String TAG = "FindSpotMainFragment";
    public ArrayList spotsList;
    public LinearLayout searchlayout;

    public LinearLayout mainLayout;

    public TextView actionBarDestination;

    //在actionbar上显示，选择城市
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

        searchView.setSubmitButtonEnabled(true);//是否显示确认搜索按钮
        searchView.setIconifiedByDefault(false);//设置展开后图标的样式,这里只有两种,一种图标在搜索框外,一种在搜索框内
        searchView.setIconified(false);//设置
        searchView.clearFocus();//清除焦点
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {

                searchlayout.setVisibility(View.VISIBLE);
                mainLayout.setVisibility(View.INVISIBLE);

                FindSpotService.getSearchSceneryList(spotsList, s, FindSpotMainFragment.this);


//                Intent intent = new Intent(getActivity(), ShowScenerySpot.class);
//                intent.putExtra("query", s);
//                intent.putExtra("isSearch", true);
//                startActivity(intent);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

    }

    //当得到结果 弹出dialog
    public void popQueryListDialog() {
        searchlayout.setVisibility(View.INVISIBLE);
        mainLayout.setVisibility(View.VISIBLE);
        MaterialDialog materialDialog = new MaterialDialog.Builder(getActivity()).title("查询结果")
                .adapter(new QueryListDialogAdapter(getActivity(), spotsList)).build();
        ListView listView = materialDialog.getListView();
        if (listView != null) {
            listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                    Toast.makeText(MainActivity.this, "Clicked item " + position, Toast.LENGTH_SHORT).show();
                }
            });
        }

        materialDialog.show();


    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//必须在oncreate中setHasOptionsMenu（）表示愿意增添item到actionbar中，否则fragment接受不到oncreateoptionmenu函数

        //初始化spotlist
        spotsList = new ArrayList();

        // 防止fragment重叠
        getFragmentManager().popBackStack();
        //将自定义view显示在actionbar中
//        ActionBar actionBar = ((ActionBarActivity) getActivity()).getSupportActionBar();
//
//        View customView = getActivity().getLayoutInflater().inflate(R.layout.actionbar_customview, null);
//        actionBarDestination = (TextView) customView.findViewById(R.id.actionbar_custom_view_destination_city);
//
//        customView.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                if (v.getId() == R.id.actionbar_custom_view_destination_city) {
//                    // 点击了选择城市的textview
//                    Intent intent = new Intent(getActivity(), CityList.class);
//                    startActivityForResult(intent, 1);
//
//                }
//            }
//        });
//        actionBar.setCustomView(customView, new ActionBar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT,
//                Gravity.RIGHT));
//        int flags = ActionBar.DISPLAY_SHOW_CUSTOM;
//        int change = actionBar.getDisplayOptions() ^ flags;
//        actionBar.setDisplayOptions(change, flags);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        String city = data.getStringExtra("city");
        actionBarDestination.setText(city);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        String[] levelData = {"5A级", "4A级", "3A级", "A级", "A级"};

        View view = inflater.inflate(R.layout.findspot_fragment_main, null);


        Spinner spinner = (Spinner) view.findViewById(R.id.findspot_level_spn);
        searchlayout = (LinearLayout) view.findViewById(R.id.findspot_searchlayout);
        mainLayout = (LinearLayout) view.findViewById(R.id.findspot_mainlayout);
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, levelData);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);
        spinner.setPrompt("请选择等级：");

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int position, long l) {
                changeLevelResultFragment(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });


        // 设置spinner 和 推荐按钮的宽度

        int width = MyContext.newInstance(getActivity()).getScreenWidth();
        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) spinner.getLayoutParams();
        params.width = (width - 20) / 2;
        spinner.setLayoutParams(params);

        Button recommandBtn = (Button) view.findViewById(R.id.findspot_recommand_btn);
        RelativeLayout.LayoutParams params2 = (RelativeLayout.LayoutParams) recommandBtn.getLayoutParams();
        params2.width = (width - 20) / 2;
        recommandBtn.setLayoutParams(params2);

        return view;
    }

    private void changeLevelResultFragment(int position) {
        //向fragment传递数据
        Bundle bundle = new Bundle();
        bundle.putString("level", Integer.toString(5 - position));
        // 创建Fragment对象
        LevelResultFragment levelResultFragment = new LevelResultFragment();

        // 向Fragment传入参数
        levelResultFragment.setArguments(bundle);

        android.support.v4.app.FragmentManager manager = getFragmentManager();
        manager.beginTransaction().replace(R.id.findspot_framelayout, levelResultFragment).commit();
    }


}


