package com.eztrip.routemaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Instrumentation;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.eztrip.MainActivity;
import com.eztrip.findspot.FindSpotMainFragment;
import com.eztrip.findspot.LevelResultFragment;
import com.eztrip.main.MainFragment;
import com.eztrip.R;
import com.eztrip.citylist.CityList;
import com.eztrip.model.RouteData;
import com.eztrip.model.ScenerySpot;
import com.eztrip.model.TravelBag;
import com.eztrip.routemaker.adapter.BasicSettingsSpotAdapter;
import com.eztrip.routemaker.adapter.DietSettingsAdapter;
import com.eztrip.routemaker.adapter.SpotSettingsAdapter;
import com.eztrip.routemaker.adapter.TimeSettingsAdapter;

import java.util.ArrayList;
import java.util.Calendar;

import se.emilsjolander.stickylistheaders.StickyListHeadersListView;
import utils.RouteAutoGenerator;


/**
 * Created by Steve on 2015/2/4.
 * 制定旅行线路的界面操作类
 */
public class RouteMakerFragment extends Fragment {

    private ArrayList<Fragment> fragments;
    private int currStep;
    private final String titleHead = "发起旅行-";
    private String[] titles = new String[]{"基本设置", "景点及住宿设置", "饮食设置", "时间安排微调", "最后一步"};
    public final int REQUEST_CODE_SEARCH_CITY = 1;
    private FragmentManager fragmentManager;
    private boolean selectSpots = false;
    private static Context context;

    private ArrayList<RouteData.SpotTemp> spotList;//it is not sorted by time
    
    public static RouteMakerFragment newInstance(Context context) {
        RouteMakerFragment.context = context;
        return new RouteMakerFragment();
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);//必须在oncreate中setHasOptionsMenu（）表示愿意增添item到actionbar中，否则fragment接受不到oncreateoptionmenu函数
        ProgressDialogController.init(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.routemaker_fragment, container, false);

        initView();
        setTitle();
        return view;
    }

    private void setTitle() {
        ((MainActivity)getActivity()).setActionbarTitle(titleHead + titles[currStep]);
    }

    private void initView() {
        fragments = new ArrayList<>();
        fragmentManager = ((MainActivity)getActivity()).getCurrentFragmentManager();
        Fragment basicSettings = new Fragment() {
            private View view;
            RelativeLayout selectCity
                    ,
                    addSpot
                    ,
                    traffic
                    ,
                    diet
                    ,
                    dayLength;
            TextView city
                    ,
                    city2
                    ,
                    trafficTV
                    ,
                    dayTV
                    ,
                    dietTV;
            Button nextStep;
            private ListView spotListView;
            private BasicSettingsSpotAdapter adapter;
            private boolean[] dietStatus;
            private EditText dayET;

            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                view = inflater.inflate(R.layout.routemaker_basicsettings, container, false);
                initView();
                return view;
            }

            private void initView() {
                city = (TextView) view.findViewById(R.id.routemaker_basicsettings_city);
                city2 = (TextView) view.findViewById(R.id.routemaker_basicsettings_city2);
                selectCity = (RelativeLayout) view.findViewById(R.id.routemaker_basicsettings_city_change);
                spotListView = (ListView) view.findViewById(R.id.routemaker_basicsettings_spotlist);
                addSpot = (RelativeLayout) view.findViewById(R.id.routemaker_basicsettings_spot_add);
                traffic = (RelativeLayout) view.findViewById(R.id.routemaker_basicsettings_traffic);
                trafficTV = (TextView) view.findViewById(R.id.routemaker_basicsettings_traffic_textview);
                dietTV = (TextView) view.findViewById(R.id.routemaker_basicsettings_diet_textview);
                nextStep = (Button) view.findViewById(R.id.routemaker_basicsettings_next_step);
                dayLength = (RelativeLayout) view.findViewById(R.id.routemaker_basicsettings_day);
                dayTV = (TextView) view.findViewById(R.id.routemaker_basicsettings_day_textview);
                diet = (RelativeLayout) view.findViewById(R.id.routemaker_basicsettings_diet);
                dietStatus = new boolean[3];
                for (boolean i : dietStatus)
                    i = false;
                selectCity.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(getActivity(), CityList.class);
                        startActivityForResult(intent, REQUEST_CODE_SEARCH_CITY);
                    }
                });
                addSpot.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO 跳转至选择景点页面
                        selectSpots = true;
                        Fragment findSpotMainFragment = FindSpotMainFragment.newInstance(getActivity());
                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
                        fragmentTransaction.replace(R.id.routemaker_fragment_content,findSpotMainFragment);
                        fragmentTransaction.addToBackStack("findspot");
                        fragmentTransaction.commit();
                    }
                });
                nextStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if(TravelBag.getInstance().getScenerySpotList().size() == 0) {
                            Toast.makeText(getActivity(),"请至少选择一个景点",Toast.LENGTH_SHORT).show();
                        }else {
                            ProgressDialogController.show();
                            new GenerateSpotListAsyncTask().execute();
                        }
                    }
                });
                diet.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getActivity()).setTitle(getActivity().getResources().getString(R.string.routemaker_dietsettings_time))
                                .setMultiChoiceItems(new String[]{getActivity().getResources().getString(R.string.routemaker_dietsettings_breakfast), getActivity().getResources().getString(R.string.routemaker_dietsettings_lunch), getActivity().getResources().getString(R.string.routemaker_dietsettings_dinner)}, dietStatus,
                                        new DialogInterface.OnMultiChoiceClickListener() {
                                            @Override
                                            public void onClick(DialogInterface dialog, int which, boolean isChecked) {
                                                dietStatus[which] = isChecked;
                                            }
                                        }).setNegativeButton("取消", null)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        StringBuilder text = new StringBuilder();
                                        if (dietStatus[0])
                                            text = text.append(getActivity().getResources().getString(R.string.routemaker_dietsettings_breakfast));
                                        if (dietStatus[1])
                                            text = text.append((text.toString().equals("") ? "" : "，") + getActivity().getResources().getString(R.string.routemaker_dietsettings_lunch));
                                        if (dietStatus[2])
                                            text = text.append((text.toString().equals("") ? "" : "，") + getActivity().getResources().getString(R.string.routemaker_dietsettings_dinner));
                                        if (text.toString().equals(""))
                                            text = text.append(getActivity().getResources().getString(R.string.nothing));
                                        dietTV.setText(text.toString());
                                        dialog.dismiss();
                                    }
                                }).show();
                    }
                });
                traffic.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        new AlertDialog.Builder(getActivity()).setTitle(getActivity().getResources().getString(R.string.routemaker_basicsettings_traffic_hint))
                                .setSingleChoiceItems(new String[]{getActivity().getResources().getString(R.string.routemaker_trafficsettings_public), getActivity().getResources().getString(R.string.routemaker_trafficsettings_private)}, 0,
                                        new DialogInterface.OnClickListener() {

                                            public void onClick(DialogInterface dialog, int which) {
                                                trafficTV.setText(which == 0 ? getActivity().getResources().getString(R.string.routemaker_trafficsettings_public) : getActivity().getResources().getString(R.string.routemaker_trafficsettings_private));
                                                dialog.dismiss();
                                            }
                                        }).setNegativeButton("取消", null)
                                .show();
                    }
                });
                dayLength.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dayET = new EditText(getActivity());
                        dayET.setKeyListener(new DigitsKeyListener(false, true));
                        new AlertDialog.Builder(getActivity()).setTitle(getActivity().getResources().getString(R.string.routemaker_basicsettings_favoritespot_day) + "（1-10天）")
                                .setView(dayET)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        if (dayET.getText().toString().equals("") || Integer.parseInt(dayET.getText().toString()) < 1 || Integer.parseInt(dayET.getText().toString()) > 10)
                                            Toast.makeText(getActivity(), "请设置旅行天数在1-10之间", Toast.LENGTH_LONG).show();
                                        else
                                            dayTV.setText(dayET.getText().toString());
                                    }
                                })
                                .setNegativeButton("取消", null)
                                .show();
                    }
                });
                initSpotList("北京");
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                if (resultCode == CityList.RESULT_CODE_SEARCH_CITY) {
                    String cityName = data.getStringExtra("city");
                    city.setText(cityName);
                    city2.setText(cityName);
                    initSpotList(cityName);
                }
                adapter = new BasicSettingsSpotAdapter(getActivity(), spotListView);
                spotListView.setAdapter(adapter);
                adaptListViewHeight(spotListView, adapter);
            }

            private void initSpotList(String city) {
                adapter = new BasicSettingsSpotAdapter(getActivity(), spotListView);
                spotListView.setAdapter(adapter);
                adaptListViewHeight(spotListView, adapter);
            }

            class GenerateSpotListAsyncTask extends GeneratorTask {
                @Override
                protected Object doInBackground(Void... params) {
                    String cityName = city.getText().toString();
                    int day;
                    try {
                        day = Integer.parseInt(dayTV.getText().toString());
                    } catch (Exception e) {
                        day = 1;
                    }
                    String trafficInfo = trafficTV.getText().toString();
                    String dietInfo = dietTV.getText().toString();
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    return RouteAutoGenerator.executeBasicSettings(cityName,TravelBag.getInstance().getScenerySpotList(), day, trafficInfo, dietInfo, getActivity());
                }

                @Override
                protected void onPostExecute(Object result) {
                    super.onPostExecute(result);
                    MyHandler handler = new MyHandler(TravelBag.getInstance().getScenerySpotList().size() * (TravelBag.getInstance().getScenerySpotList().size() - 1) / 2 + 1);
                    Log.e("size",Integer.toString(TravelBag.getInstance().getScenerySpotList().size()));
                    spotList = (ArrayList<RouteData.SpotTemp>)result;
                    RouteAutoGenerator.getSpotTimeAndHotel(handler,spotList,getActivity());
                }
            }
        };
        final Fragment spotSettings = new Fragment() {
            private LinearLayout hintLayout;
            private TextView warning;
            private View view;
//            private Button regenerate;
            private Button nextStep;
            private StickyListHeadersListView stickyListHeadersListView;
            private SpotSettingsAdapter adapter;
            //will be updated later
//            private ListView newSpotListView;
//            private BasicSettingsSpotAdapter newSpotAdapter;
//            private RelativeLayout addSpotsLayout;

            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                view = inflater.inflate(R.layout.routemaker_spotsettings, container, false);
                initView();
                return view;
            }

            private void initView() {
                warning = (TextView) view.findViewById(R.id.routemaker_spotsettings_warning);
                if(!RouteData.spotSettingsHint.equals("")) {
                    if(RouteData.spotSettingsHint.equals("TooBusy")) {
                        warning.setText("您的计划可能过于繁忙，请尝试增加旅行天数或减少游览景点。");
                    }else if(RouteData.spotSettingsHint.equals("NotBusy")) {
                        warning.setText("您的日程安排比较清闲，推荐您减少旅行天数或增加游览景点。");
                    }
                }else {
                    warning.setVisibility(View.GONE);
                }
                hintLayout = (LinearLayout) view.findViewById(R.id.routemaker_spotsettings_change_hint);
                hintLayout.setVisibility(View.GONE);
//                regenerate = (Button) view.findViewById(R.id.routemaker_spotsettings_regeneration);
                nextStep = (Button) view.findViewById(R.id.routemaker_spotsettings_next_step);
                stickyListHeadersListView = (StickyListHeadersListView) view.findViewById(R.id.routemaker_spotsettings_spotlist);
//                newSpotListView = (ListView) view.findViewById(R.id.routemaker_spotsettings_newspotlist);
//                addSpotsLayout = (RelativeLayout)view.findViewById(R.id.routemaker_spotsettings_spot_add);
//                addSpotsLayout.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        FindSpotMainFragment levelResultFragment = FindSpotMainFragment.newInstance(context);
//                        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
//                        fragmentTransaction.replace(R.id.routemaker_fragment_content,levelResultFragment);
//                        fragmentTransaction.addToBackStack(null);
//                        fragmentTransaction.commit();
//                    }
//                });
//                regenerate.setOnClickListener(new View.OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        //TODO: 重新生成计划
//                        RouteAutoGenerator.regenerateSpotSettings(getActivity());
//                        initListView();
//
//                    }
//                });
                nextStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProgressDialogController.show();
                        int count = 0;
                        for(int i = 0; i < RouteData.spotTempInfo.size(); i++) {
                            if(!RouteData.spotTempInfo.get(i).type.equals(RouteData.ActivityType.NONE))
                                count++;
                        }
                        RouteAutoGenerator.getLatLngInfo(new MyHandler(count),getActivity());

//                        new GenerateDietListAsyncTask().execute();
                    }
                });
                initListView();
            }

            private void initListView() {
                //假数据
                adapter = new SpotSettingsAdapter(getActivity());
                stickyListHeadersListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
//                newSpotAdapter = new BasicSettingsSpotAdapter(getActivity(), TravelBag.getInstance().getScenerySpotList(), newSpotListView);
//                newSpotListView.setAdapter(newSpotAdapter);
//                newSpotAdapter.notifyDataSetChanged();

            }

            @Override
            public void onResume() {
                super.onResume();
                adapter = new SpotSettingsAdapter(getActivity());
                stickyListHeadersListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
//                newSpotAdapter = new BasicSettingsSpotAdapter(getActivity(), TravelBag.getInstance().getScenerySpotList(), newSpotListView);
//                newSpotListView.setAdapter(newSpotAdapter);
//                newSpotAdapter.notifyDataSetChanged();
//                float totalHeaderHeight = RouteData.dayLength * 3 * getActivity().getResources().getDimension(R.dimen.day_header_height);
//                float totalItemHeight = (adapter.getCount() - RouteData.dayLength * 3) * getActivity().getResources().getDimension(R.dimen.spot_item_height);
//                ViewGroup.LayoutParams params = stickyListHeadersListView.getLayoutParams();
//                params.height = (int) (totalHeaderHeight + totalItemHeight);
//                stickyListHeadersListView.setLayoutParams(params);
//                float newSpotsHeight = TravelBag.getInstance().getScenerySpotList().size() * getActivity().getResources().getDimension(R.dimen.item_height_default);
//                ViewGroup.LayoutParams params2 = newSpotListView.getLayoutParams();
//                params2.height = (int)newSpotsHeight;
//                newSpotListView.setLayoutParams(params2);
            }

            class GenerateDietListAsyncTask extends GeneratorTask {
                protected String doInBackground(Void... params) {
                    return RouteAutoGenerator.executeSpotSettings(getActivity(),new MyHandler(0));
                }

                @Override
                protected void onPostExecute(Object result) {
                    super.onPostExecute(result);
                }
            }
        };
        Fragment dietSettings = new Fragment() {
            private Button nextStep;
            private View view;
            private StickyListHeadersListView stickyListHeadersListView;
            private DietSettingsAdapter adapter;

            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                view = inflater.inflate(R.layout.routemaker_dietsettings, container, false);
                initView();
                return view;
            }

            private void initView() {
                nextStep = (Button) view.findViewById(R.id.routemaker_dietsettings_next_step);
                stickyListHeadersListView = (StickyListHeadersListView) view.findViewById(R.id.routemaker_dietsettings_dietlist);
                nextStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
//                        new GenerateTimeListAsyncTask().execute();
                        ProgressDialogController.show();
                        RouteAutoGenerator.executeDietSettings();
                        RouteAutoGenerator.getTrafficTimes(getActivity(),new MyHandler(1 + RouteData.singleEvents.size() / 2));
                    }
                });
                initListView();
            }

            private void initListView() {
                adapter = new DietSettingsAdapter(getActivity());
                stickyListHeadersListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }

            class GenerateTimeListAsyncTask extends GeneratorTask {
                @Override
                protected String doInBackground(Void... params) {
                    return RouteAutoGenerator.executeDietSettings();
                }
            }

            @Override
            public void onResume() {
                super.onResume();
                initListView();
            }
        };
        Fragment timeSettings = new Fragment() {
            private Button nextStep;
            private View view;
            private StickyListHeadersListView stickyListHeadersListView;
            private TimeSettingsAdapter adapter;

            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                view = inflater.inflate(R.layout.routemaker_timesettings, container, false);
                initView();
                return view;
            }

            private void initView() {
                stickyListHeadersListView = (StickyListHeadersListView) view.findViewById(R.id.routemaker_timesettings_list);
                nextStep = (Button) view.findViewById(R.id.routemaker_timesettings_next_step);
                nextStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nextStep();
                    }
                });
                initListView();
            }

            private void initListView() {
                adapter = new TimeSettingsAdapter(getActivity());
                stickyListHeadersListView.setAdapter(adapter);
                adapter.notifyDataSetChanged();
            }
        };
        Fragment finishSettings = new Fragment() {
            private Button nextStep;
            private RelativeLayout changeDate
                    ,
                    changeName;
            private View view;
            private TextView date
                    ,
                    name
                    ,
                    hint;
            private int startYear
                    ,
                    startMonth
                    ,
                    startDay;
            private DatePickerDialog dialog;
            private EditText nameET;
            private Switch aSwitch;

            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                view = inflater.inflate(R.layout.routemaker_finishsettings, container, false);
                initView();
                return view;
            }

            private void initView() {
                nextStep = (Button) view.findViewById(R.id.routemaker_timesettings_next_step);
                changeDate = (RelativeLayout) view.findViewById(R.id.routemaker_finishsettings_settime);
                changeName = (RelativeLayout) view.findViewById(R.id.routemaker_finishisettings_name_layout);
                name = (TextView) view.findViewById(R.id.routemaker_finishisettings_name);
                date = (TextView) view.findViewById(R.id.routemaker_finishisettings_start_date);
                hint = (TextView) view.findViewById(R.id.routemaker_finishisettings_start_date_hint);
                aSwitch = (Switch)view.findViewById(R.id.switch1);
                hint.setVisibility(View.GONE);
                RouteData.startDay = Calendar.getInstance();
                RouteData.startDay.add(Calendar.DAY_OF_YEAR, 1);
                startYear = RouteData.startDay.get(Calendar.YEAR);
                startMonth = RouteData.startDay.get(Calendar.MONTH);
                startDay = RouteData.startDay.get(Calendar.DAY_OF_MONTH);
                updateDateDisplay();
                aSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                    @Override
                    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                        if(isChecked) {
                            Calendar currentDay = Calendar.getInstance();
                            if (!currentDay.before(RouteData.startDay))
                                hint.setVisibility(View.VISIBLE);
                            else
                                hint.setVisibility(View.GONE);
                        }else {
                            hint.setVisibility(View.GONE);
                        }
                    }
                });
                changeDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog = new DatePickerDialog(getActivity(), mDateSetListener, startYear, startMonth, startDay);
                        dialog.show();
                    }
                });
                changeName.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        nameET = new EditText(getActivity());
                        new AlertDialog.Builder(getActivity())
                                .setTitle(getActivity().getResources().getString(R.string.routemaker_finishsettings_name))
                                .setView(nameET)
                                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        name.setText(nameET.getText().toString());
                                    }
                                }).setNegativeButton("取消", null)
                                .show();
                    }
                });
                nextStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String startTime = date.getText().toString();
                        String names = name.getText().toString();
//                        if(hint.getVisibility() == View.VISIBLE)
//                            Toast.makeText(getActivity(),hint.getText().toString(),Toast.LENGTH_LONG).show();
//                        else {
                            RouteData.name = names;
                            if(aSwitch.isChecked()) {
                                RouteData.startDay.set(startYear,startMonth,startDay);
                            }else {
                                RouteData.startDay = null;
                            }
                            ProgressDialogController.show();
                            RouteAutoGenerator.executeFinishSettings(new MyHandler(1), getActivity(), startTime, names);
//                        }
                    }
                });
            }

            private DatePickerDialog.OnDateSetListener mDateSetListener = new DatePickerDialog.OnDateSetListener() {
                public void onDateSet(DatePicker view, int year, int monthOfYear,
                                      int dayOfMonth) {
                    startYear = year;
                    startMonth = monthOfYear;
                    startDay = dayOfMonth;
                    updateDateDisplay();
                }
            };

            private void updateDateDisplay() {
                date.setText(startYear + "-" + (((startMonth + 1)<10)?("0" + Integer.toString(startMonth + 1)):Integer.toString(startMonth + 1)) + "-" + ((startDay<10)?("0" + Integer.toString(startDay)):Integer.toString(startDay)));
                Calendar currentDay = Calendar.getInstance();
                RouteData.startDay.set(startYear, startMonth, startDay);
                if (!currentDay.before(RouteData.startDay))
                    hint.setVisibility(View.VISIBLE);
                else
                    hint.setVisibility(View.GONE);
            }

            class FinishRouteAsyncTask extends GeneratorTask {
                @Override
                protected String doInBackground(Void... params) {
                    return null;
                }
            }
        };
        Fragment dietInfo = new Fragment();
        fragments.add(basicSettings);
        fragments.add(spotSettings);
        fragments.add(dietSettings);
        fragments.add(timeSettings);
        fragments.add(finishSettings);
        fragments.add(dietInfo);

        currStep = 0;

        // Set up the drawer.
        getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.routemaker_fragment_content, fragments.get(currStep)).commit();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_routemaker, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.routemaker_last_step) {
            moveToLastStep();
        }
        return super.onOptionsItemSelected(item);
    }

    public void moveToLastStep() {
        int nextStep = getFragment(-1, currStep);
        if (nextStep >= 0) {
            fragmentManager.popBackStack();
            setTitle();
        }
    }

    private int getFragment(int direction, int currentStep) {
        if (currentStep == 5) {
            currStep = 5 + direction * 3;
            return currStep;
        } else {
            if(selectSpots) {
                selectSpots = false;
                fragmentManager.popBackStack();
                return -1;
            }else if (currentStep == 0 && direction == -1) {
                Toast.makeText(getActivity(), "已经是第一步", Toast.LENGTH_SHORT).show();
                fragmentManager.popBackStack();
                return -1;
            } else if (currentStep == 4 && direction == 1) {
                Toast.makeText(getActivity(), "路线设计完成", Toast.LENGTH_SHORT).show();
                return -1;
            } else {
                currStep = direction + currentStep;
                return currStep;
            }
        }
    }

    public void nextStep() {
        currStep = getFragment(1, currStep);
        if (currStep != -1) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.routemaker_fragment_content, fragments.get(currStep));
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
            setTitle();
        }
    }

//    @Override
//    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
//            fragments.get(currStep).onActivityResult(requestCode, resultCode, data);
//    }

    public static void adaptListViewHeight(ListView listView, BaseAdapter adapter) {
        int totalHeight = 0;
        for (int i = 0; i < adapter.getCount(); i++) {
            View listItem = adapter.getView(i, null, listView);
            listItem.measure(0, 0);
            totalHeight += listItem.getMeasuredHeight();
        }
//        int itemHeight = activity.getResources().getDimension(R.dimen.)
        ViewGroup.LayoutParams params = listView.getLayoutParams();
        params.height = totalHeight + listView.getDividerHeight() * (adapter.getCount() - 1);
        listView.setLayoutParams(params);
        listView.setDividerHeight(0);
    }

    public class MyHandler extends android.os.Handler{
        private int count;
        public MyHandler(int a){
            this.count = a;
        }

        public MyHandler() {
            this.count = 0;
        }

        public void setCount(int a) {
            this.count = a;
        }

        public void addCount() {
            this.count++;
        }

        public int getCount() {
            return this.count;
        }

        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            Log.e("Count",Integer.toString(count));
            if(msg.getData().getBoolean("minus")){
                count--;
                if(count <= 0){
                    Log.e("success","success");
                    if(msg.getData().getString("source").equals("basic")){
                        for(int i = 0; i < spotList.size(); i++){
                            for(int j = 0; j < spotList.size(); j++)
                                RouteAutoGenerator.kSearch[i][j].destroy();
                        }
                        String result =  RouteAutoGenerator.generateSpotSettingsPlan(spotList, getActivity());
                        if(result.equals("success")) {
                            if(currStep == 0) {//处在BasicSettings环节
                                nextStep();
                                ProgressDialogController.dismiss();
                            }else {//处在SpotSettings环节
                                ProgressDialogController.dismiss();
                                fragmentManager.popBackStack();
                                fragmentManager.beginTransaction().replace(R.id.routemaker_fragment_content, fragments.get(currStep)).commit();
                            }
                        }else {
                            ProgressDialogController.dismiss();
                            Toast.makeText(getActivity(),"景点数量过多，不能生成合理的旅行计划，请减少景点数量或增加旅行天数",Toast.LENGTH_LONG).show();
                        }
                    }else if(msg.getData().getString("source").equals("spot")) {
                        nextStep();
                        ProgressDialogController.dismiss();
                    }else if(msg.getData().getString("source").equals("diet")) {
                        RouteAutoGenerator.arrangeTimeSettingsTime();
                        nextStep();
                        ProgressDialogController.dismiss();
                    }else if(msg.getData().getString("source").equals("finish")) {
                        if(msg.getData().getBoolean("success")) {
                            ProgressDialogController.dismiss();
                            fragmentManager.beginTransaction().replace(R.id.routemaker_fragment_content, MainFragment.newInstance(getActivity())).commit();
                            ((MainActivity)getActivity()).setActionbarTitle("首页");
                        }else {
                            Toast.makeText(getActivity(),"线路上传服务器失败",Toast.LENGTH_LONG).show();
                        }
                    }else if(msg.getData().getString("source").equals("latlnginfo")) {
                        RouteAutoGenerator.executeSpotSettings(getActivity(),new MyHandler());
                        ProgressDialogController.dismiss();
                    }
                }
            }
        }
    }

    class GeneratorTask extends AsyncTask<Void, Object, Object> {

        @Override
        protected void onProgressUpdate(Object... values) {
            super.onProgressUpdate(values);
        }

        @Override
        protected void onPostExecute(Object result) {
            super.onPostExecute(result);
//            if (result.equals(RouteAutoGenerator.success))
//                nextStep();
//            else
//                Toast.makeText(activity, result, Toast.LENGTH_LONG).show();
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Object doInBackground(Void... params) {
            return null;
        }
    }

    static class ProgressDialogController{
        public static ProgressDialog progressDialog;
        public static void init(Activity activity) {
            ProgressDialogController.progressDialog = new ProgressDialog(activity);
            ProgressDialogController.progressDialog.setProgress(ProgressDialog.STYLE_SPINNER);
            ProgressDialogController.progressDialog.setMessage("处理中，请稍等。");
            ProgressDialogController.progressDialog.setCancelable(false);
        }

        public static void show(){
            ProgressDialogController.progressDialog.show();
        }

        public static void dismiss() {
            ProgressDialogController.progressDialog.dismiss();
        }
    }
}