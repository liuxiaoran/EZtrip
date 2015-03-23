package com.eztrip.routemaker;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.text.method.DigitsKeyListener;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.baidu.mapapi.search.core.SearchResult;
import com.baidu.mapapi.search.route.DrivingRouteResult;
import com.baidu.mapapi.search.route.OnGetRoutePlanResultListener;
import com.baidu.mapapi.search.route.PlanNode;
import com.baidu.mapapi.search.route.RoutePlanSearch;
import com.baidu.mapapi.search.route.TransitRoutePlanOption;
import com.baidu.mapapi.search.route.TransitRouteResult;
import com.baidu.mapapi.search.route.WalkingRouteResult;
import com.eztrip.MainActivity;
import com.eztrip.MainFragment;
import com.eztrip.R;
import com.eztrip.citylist.CityList;
import com.eztrip.model.Clock;
import com.eztrip.model.RouteData;
import com.eztrip.routemaker.adapter.BasicSettingsSpotAdapter;
import com.eztrip.routemaker.adapter.DietSettingsAdapter;
import com.eztrip.routemaker.adapter.SpotSettingsAdapter;
import com.eztrip.routemaker.adapter.TimeSettingsAdapter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.logging.Handler;

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
    private final int REQUEST_CODE_SEARCH_CITY = 1;
    private FragmentManager fragmentManager;

    private ArrayList<RouteData.SpotTemp> spotList;//it is not sorted by time

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
        ((MainActivity) getActivity()).setActionbarTitle(titleHead + titles[currStep]);
    }

    private void initView() {
        fragments = new ArrayList<>();
        fragmentManager = getActivity().getSupportFragmentManager();
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
            private ArrayList<HashMap<String, String>> spots;
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
                        Toast.makeText(getActivity(), city.getText(), Toast.LENGTH_SHORT).show();
                    }
                });
                nextStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProgressDialogController.show();
                       new GenerateSpotListAsyncTask().execute();
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
                } else if (resultCode == 2)//添加新景点
                {
                    addOneSpot(data.getStringExtra("spot"), data.getStringExtra("address"));
                }
            }

            private void initSpotList(String city) {
                //get a set of spots of the city user collected from the server
                //ArrayList<HashMap<String,String>> favoriteSpot = RouteMakerService.getSpotsCollection(city,getActivity());
                ArrayList<HashMap<String, String>> favoriteSpot = new ArrayList<>();
                spots = (ArrayList<HashMap<String, String>>) favoriteSpot.clone();
                //test case:
                HashMap<String, String> test1 = new HashMap<>();
                test1.put("name", "雍和宫");
                test1.put("address", "北京市东城区雍和宫大街12号");
                HashMap<String, String> test2 = new HashMap<>();
                test2.put("name", "天坛");
                test2.put("address", "北京市崇文区天坛内东里7号");
                HashMap<String, String> test3 = new HashMap<>();
                test3.put("name", "恭王府");
                test3.put("address", "北京市西城区柳荫街甲14号");
                spots.add(test1);
                spots.add(test2);
                spots.add(test3);


                adapter = new BasicSettingsSpotAdapter(getActivity(), spots, spotListView);
                spotListView.setAdapter(adapter);
                adaptListViewHeight(spotListView, adapter);
            }

            private void addOneSpot(String spotName, String address) {
                HashMap<String, String> test1 = new HashMap<>();
                test1.put("name", spotName);
                test1.put("address", address);
                spots.add(test1);
                adapter.notifyDataSetChanged();
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

                    return RouteAutoGenerator.executeBasicSettings(cityName, spots, day, trafficInfo, dietInfo, getActivity());
                }

                @Override
                protected void onPostExecute(Object result) {
                    super.onPostExecute(result);
                    MyHandler handler = new MyHandler(spots.size() + 1);
                    spotList = (ArrayList<RouteData.SpotTemp>)result;
                    RouteAutoGenerator.getSpotTimeAndHotel(handler,spotList,getActivity());
                }
            }
        };
        final Fragment spotSettings = new Fragment() {
            private LinearLayout hintLayout;
            private TextView hint;
            private View view;
            private Button regenerate
                    ,
                    nextStep;
            private StickyListHeadersListView stickyListHeadersListView;
            private SpotSettingsAdapter adapter;
            private ListView newSpotListView;
            private ArrayList<HashMap<String, String>> newSpots;
            private BasicSettingsSpotAdapter newSpotAdapter;

            @Override
            public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
                view = inflater.inflate(R.layout.routemaker_spotsettings, container, false);
                initView();
                return view;
            }

            private void initView() {
                hint = (TextView) view.findViewById(R.id.routemaker_spotsettings_hint);
                hintLayout = (LinearLayout) view.findViewById(R.id.routemaker_spotsettings_change_hint);
                hintLayout.setVisibility(View.GONE);
                regenerate = (Button) view.findViewById(R.id.routemaker_spotsettings_regeneration);
                nextStep = (Button) view.findViewById(R.id.routemaker_spotsettings_next_step);
                stickyListHeadersListView = (StickyListHeadersListView) view.findViewById(R.id.routemaker_spotsettings_spotlist);
                newSpotListView = (ListView) view.findViewById(R.id.routemaker_spotsettings_newspotlist);
                regenerate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        //TODO: 重新生成计划
                        RouteAutoGenerator.regenerateSpotSettings(getActivity(), newSpots);
                        initListView();

                    }
                });
                nextStep.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ProgressDialogController.show();
                        RouteAutoGenerator.executeSpotSettings(getActivity(),new MyHandler());
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
                newSpots = new ArrayList<>();
                newSpotAdapter = new BasicSettingsSpotAdapter(getActivity(), newSpots, newSpotListView);
                newSpotListView.setAdapter(newSpotAdapter);
                newSpotAdapter.notifyDataSetChanged();
                float totalHeaderHeight = RouteData.dayLength * 3 * getActivity().getResources().getDimension(R.dimen.day_header_height);
                float totalItemHeight = (adapter.getCount() - RouteData.dayLength * 3) * getActivity().getResources().getDimension(R.dimen.spot_item_height);
                ViewGroup.LayoutParams params = stickyListHeadersListView.getLayoutParams();
                params.height = (int) (totalHeaderHeight + totalItemHeight);
                stickyListHeadersListView.setLayoutParams(params);
            }

            @Override
            public void onActivityResult(int requestCode, int resultCode, Intent data) {
                //TODO 更新旅店信息
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
                        RouteAutoGenerator.executeDietSettings(getActivity());
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
                    return RouteAutoGenerator.executeDietSettings(getActivity());
                }
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
//                RouteData.setSingleEventsInstance(3);
//                RouteData.singleEvents.get(0).type = RouteData.ActivityType.SPOT;
//                RouteData.singleEvents.get(0).startTime = new Clock(9, 0);
//                RouteData.singleEvents.get(0).finishTime = new Clock(11, 0);
//                RouteData.singleEvents.get(0).detail = "景点";
//                RouteData.singleEvents.get(0).day = 0;
//                RouteData.singleEvents.get(1).type = RouteData.ActivityType.DIET;
//                RouteData.singleEvents.get(1).startTime = new Clock(11, 0);
//                RouteData.singleEvents.get(1).finishTime = new Clock(12, 0);
//                RouteData.singleEvents.get(1).detail = "就餐";
//                RouteData.singleEvents.get(1).day = 1;
//                RouteData.singleEvents.get(2).type = RouteData.ActivityType.ACCOMMODATION;
//                RouteData.singleEvents.get(2).startTime = new Clock(17, 0);
//                RouteData.singleEvents.get(2).finishTime = new Clock(19, 0);
//                RouteData.singleEvents.get(2).detail = "住宿";
//                RouteData.singleEvents.get(2).day = 1;
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
                hint.setVisibility(View.GONE);
                RouteData.startDay = Calendar.getInstance();
                RouteData.startDay.add(Calendar.DAY_OF_YEAR, 1);
                startYear = RouteData.startDay.get(Calendar.YEAR);
                startMonth = RouteData.startDay.get(Calendar.MONTH);
                startDay = RouteData.startDay.get(Calendar.DAY_OF_MONTH);
                updateDateDisplay();
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
                        String name = nameET.getText().toString();
                        if(hint.getVisibility() == View.VISIBLE)
                            Toast.makeText(getActivity(),hint.getText().toString(),Toast.LENGTH_LONG).show();
                        else
                            RouteAutoGenerator.executeFinishSettings(new MyHandler(1), getActivity(), startTime, name);
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
            fragmentManager.beginTransaction().replace(R.id.routemaker_fragment_content, fragments.get(nextStep)).commit();
            setTitle();
        }
    }

    private int getFragment(int direction, int currentStep) {
        if (currentStep == 5) {
            currStep = 5 + direction * 3;
            return currStep;
        } else {
            if (currentStep == 0 && direction == -1) {
                Toast.makeText(getActivity(), "已经是第一步", Toast.LENGTH_SHORT).show();
                return -1;
            } else if (currentStep == 4 && direction == 1) {
                Toast.makeText(getActivity(), "完成", Toast.LENGTH_SHORT).show();
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
            fragmentManager.beginTransaction().replace(R.id.routemaker_fragment_content, fragments.get(currStep)).commit();
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
//        int itemHeight = getActivity().getResources().getDimension(R.dimen.)
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
                            nextStep();
                            ProgressDialogController.dismiss();
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
                            fragmentManager.beginTransaction().replace(R.id.routemaker_fragment_content, MainFragment.newInstance(getActivity())).commit();
                        }
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
//                Toast.makeText(getActivity(), result, Toast.LENGTH_LONG).show();
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
        }

        public static void show(){
            ProgressDialogController.progressDialog.show();
        }

        public static void dismiss() {
            ProgressDialogController.progressDialog.dismiss();
        }
    }
}