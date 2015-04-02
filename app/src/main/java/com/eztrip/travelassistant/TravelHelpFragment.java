package com.eztrip.travelassistant;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.eztrip.R;
import com.eztrip.model.Clock;
import com.eztrip.model.RouteData;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Created by liuxiaoran on 15/3/24.
 * 主要是通过viewpager 实现的分tab显示的界面
 */
public class TravelHelpFragment extends Fragment {

    private static Context context;
    private ViewPager viewPager;
    private TextView pagerTab1, pagerTab2;
    private View pagerTabDivider1, pagerTabDivider2;

    // 状态TextView，如果是当天,显示实时时间
    private TextView statusTv;

    private TextView currentStatusTv, nextStatusTv;

    //标示已经进行到了第几步
    private int step = 0;

    private int year;

    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {

            if (msg.what == 1) {
                long sysTime = System.currentTimeMillis();
                CharSequence sysTimeStr = DateFormat.format("HH:mm:ss", sysTime);  // HH 代表24小时制， hh代表12小时制
                statusTv.setText(sysTimeStr); //更新时间
            } else if (msg.what == 2) {

                long sysTime = System.currentTimeMillis();
                Date currDate = new Date(sysTime);

                for (int i = 0; i < RouteData.singleEvents.size(); i++) {
                    RouteData.SingleEvent event = RouteData.singleEvents.get(i);
                    Clock startClock = event.startTime;
                    Clock finishClock = event.finishTime;
                    Date startDate = new Date(year, startClock.hour, startClock.minute);
                    Date finishDate = new Date(year, finishClock.hour, finishClock.minute);
                    int compare1 = currDate.compareTo(startDate);
                    int compare2 = currDate.compareTo(finishDate);
                    if (compare1 > 0 && compare2 < 0) {
                        currentStatusTv.setText(event.detail);
                        if (i + 1 <= RouteData.singleEvents.size() - 1)
                            nextStatusTv.setText(RouteData.singleEvents.get(i + 1).detail);
                        else {
                            nextStatusTv.setText("今天的旅行计划即将结束");
                        }

                    }
                }


            }
        }


    };


    public static TravelHelpFragment newInstance(Context context) {
        TravelHelpFragment.context = context;
        return new TravelHelpFragment();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.travel_assistant_viewpager, null);

        pagerTabDivider1 = (View) view.findViewById(R.id.pager_tab1_divider);
        pagerTabDivider2 = (View) view.findViewById(R.id.pager_tab2_divider);

        initPagerTabTextView(view);
        initViewPager(view, inflater);


        return view;
    }

    private void initViewPager(View view, LayoutInflater inflater) {
        viewPager = (ViewPager) view.findViewById(R.id.vPager);
        ArrayList<View> views = new ArrayList<View>();

        View view1 = inflater.inflate(R.layout.realtime_remind_layout, null);


        createView1(view);
        View view2 = inflater.inflate(R.layout.realtime_remind_layout, null);
        views.add(view1);
        views.add(view2);
        viewPager.setAdapter(new MyViewPagerAdapter(views));
        viewPager.setCurrentItem(0);
        viewPager.setOnPageChangeListener(new MyOnPageChangeListener());
    }

    // 创建实时状态这个tab
    private void createView1(View view) {

        statusTv = (TextView) view.findViewById(R.id.travel_assistant_status_tv);
        currentStatusTv = (TextView) view.findViewById(R.id.travel_assistant_current_status);
        nextStatusTv = (TextView) view.findViewById(R.id.travel_assistant_next_status);

        Date currentDate = new Date(System.currentTimeMillis());
        Calendar startCalendar = RouteData.startDay;

        //an int < 0 if this Date is less than the specified Date, 0 if they are equal, and an int > 0 if this Date is greater.
        int compare = currentDate.compareTo(startCalendar.getTime());
        if (compare < 0) {

            statusTv.setText("您最近得一次旅行在" + DateFormat.format("yyyy-MM-dd", startCalendar.getTime()));
        } else if (compare > 0) {
            statusTv.setText("您最近得一次旅行在" + DateFormat.format("yyyy-MM-dd", startCalendar.getTime()));
        } else {
            year = startCalendar.get(Calendar.YEAR);
            TimeThread timeThread = new TimeThread();
            timeThread.start();
        }


    }


    /**
     * 比较计划的日期与当前日期先后
     *
     * @param pYear
     * @param pMonth
     * @param pDay
     * @return -1 代表当前计划日期早于当前日期; 1 代表当前计划日期晚于当前日期；0 代表这是这天
     */
    private int compareDate(int pYear, int pMonth, int pDay) {

        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);
        if (pYear < year) {
            return -1;
        } else if (pYear > year) {
            return 1;

        } else {
            if (pMonth < month) {
                return -1;
            } else if (pMonth > month) {
                return 1;
            } else {
                if (pDay < day) {
                    return -1;
                } else if (pDay > day) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }

    class TimeThread extends Thread {

        long totalTime = 0;

        @Override
        public void run() {

            try {
                Thread.sleep(1000);
                totalTime += 1000;
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Message msg = new Message();
            msg.what = 1;  //消息(一个整型值)
            mHandler.sendMessage(msg);// 每隔1秒发送一个msg给mHandler

            if (totalTime % 60000 == 0) {
                totalTime = 0;
                Message msg1 = new Message();
                msg1.what = 2;
                mHandler.sendMessage(msg1);
            }
        }
    }

    private void initPagerTabTextView(View view) {
        pagerTab1 = (TextView) view.findViewById(R.id.pager_tab_1);
        pagerTab2 = (TextView) view.findViewById(R.id.pager_tab_2);

        pagerTab1.setOnClickListener(new MyOnClickListener(0));
        pagerTab2.setOnClickListener(new MyOnClickListener(1));
    }

    class MyViewPagerAdapter extends PagerAdapter {
        private List<View> mListViews;

        public MyViewPagerAdapter(List<View> mListViews) {
            this.mListViews = mListViews;
        }

        @Override
        public void destroyItem(ViewGroup container, int position, Object object) {
            container.removeView(mListViews.get(position));
        }


        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            container.addView(mListViews.get(position), 0);
            return mListViews.get(position);
        }

        @Override
        public int getCount() {
            return mListViews.size();
        }

        @Override
        public boolean isViewFromObject(View arg0, Object arg1) {
            return arg0 == arg1;
        }
    }

    public class MyOnPageChangeListener implements ViewPager.OnPageChangeListener {


        public void onPageScrollStateChanged(int arg0) {


        }

        public void onPageScrolled(int arg0, float arg1, int arg2) {


        }

        public void onPageSelected(int arg0) {

//            Animation animation = new TranslateAnimation(one*currIndex, one*arg0, 0, 0);//显然这个比较简洁，只有一行代码。
//            currIndex = arg0;
//            animation.setFillAfter(true);// True:图片停在动画结束位置
//            animation.setDuration(300);
//            cursorIv.startAnimation(animation);

            if (arg0 == 0) {

                pagerTabDivider1.setVisibility(View.VISIBLE);
                pagerTabDivider2.setVisibility(View.INVISIBLE);
            } else {

                pagerTabDivider1.setVisibility(View.INVISIBLE);
                pagerTabDivider2.setVisibility(View.VISIBLE);
            }
//            Toast.makeText(WeiBoActivity.this, "您选择了"+ viewPager.getCurrentItem()+"页卡", Toast.LENGTH_SHORT).show();
        }

    }

    /**
     * 头标点击监听 3
     */
    private class MyOnClickListener implements View.OnClickListener {
        private int index = 0;

        public MyOnClickListener(int i) {
            index = i;
        }

        public void onClick(View v) {
            viewPager.setCurrentItem(index);
        }

    }


}
