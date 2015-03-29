package com.eztrip.main;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.baidu.location.BDLocation;
import com.baidu.location.BDLocationListener;
import com.eztrip.MyContext;
import com.eztrip.R;
import com.eztrip.citylist.CityList;
import com.eztrip.model.Clock;
import com.eztrip.model.RouteData;

import java.util.ArrayList;

import utils.BaiduLocationHelper.LocationHelper;

/**
 * Created by liuxiaoran on 2015/2/27.
 * 首页
 */
public class MainFragment extends Fragment {

    public static Context context;

    private final static int REQUEST_SELECT_CITY_CODE = 1;

    private LinearLayout recommandLayout;

    public final static String TAG = "MainFragment";
    private MyLocationListener locationListener;

    private TextView start_tv, destination_tv;

    private String[] recommandRouteTitle = {"北京", "桂林", "三亚"};
    private int[] recommandRouteImageResource = new int[]{R.drawable.main_fragment_recommand_image1,
            R.drawable.main_fragment_recommand_image2, R.drawable.main_fragment_recommand_image3};

    public static MainFragment newInstance(Context context) {
        MainFragment.context = context;
        return new MainFragment();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.main_fragment_home, null);

        start_tv = (TextView) view.findViewById(R.id.start_place_tv);
        destination_tv = (TextView) view.findViewById(R.id.destination_tv);

        destination_tv.setOnClickListener(new CickListener());


        locationListener = new MyLocationListener();
        LocationHelper.registerHelper(context, locationListener);

        // 创建下面的几条推荐路线
        recommandLayout = (LinearLayout) view.findViewById(R.id.recommand_route_linearlayout);

        for (int i = 0; i < recommandRouteImageResource.length; i++) {
            View view1 = inflater.inflate(R.layout.main_fragment_recommand_layout, null);
            ImageView imageView1 = (ImageView) view1.findViewById(R.id.main_fragment_recommand_iv);
            imageView1.setImageResource(recommandRouteImageResource[i]);
            TextView textView1 = (TextView) view1.findViewById(R.id.main_fragment_recommand_tv);
            textView1.setText(recommandRouteTitle[i]);
            view1.setTag(i);
            view1.setOnClickListener(new CickListener());
            recommandLayout.addView(view1);

        }


        return view;
    }

    class MyLocationListener implements BDLocationListener {


        @Override
        public void onReceiveLocation(BDLocation bdLocation) {

            if (bdLocation == null)
                return;
            else {

                start_tv.setText(start_tv.getText() + bdLocation.getCity());

                MyContext.startCity = bdLocation.getCity();
//                Log.v(TAG, bdLocation.getProvince()); //获取省份信息
//                Log.v(TAG, bdLocation.getCity());     //获取城市信息
//                Log.v(TAG, bdLocation.getDistrict()); //获取区县信息


            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == 1) {

            String city = data.getStringExtra("city");
            Log.v(TAG, city);
            destination_tv.setText(destination_tv.getText().toString().substring(0, 2) + " " + city);
            //在MyContext中存储目的城市
            MyContext.destinationCity = city;

        }

    }

    class CickListener implements View.OnClickListener {

        @Override
        public void onClick(View v) {
            ArrayList<RouteData.SingleEvent> routeList = new ArrayList<>();
            if (v.getId() == R.id.start_place_tv) {

            } else if (v.getId() == R.id.destination_tv) {
                Intent intent = new Intent(getActivity(), CityList.class);
                startActivityForResult(intent, REQUEST_SELECT_CITY_CODE);
            } else if (((int) v.getTag()) == 0) {

                RouteData.SingleEvent singleEvent1 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "天安门", "北京市的中心、故宫的南端，与天安门广场隔长安街相望。是明清两代北京皇城的正门");
                RouteData.SingleEvent singleEvent2 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "毛主席纪念堂", "位于天安门广场， 人民英雄纪念碑南面，坐落在原中华门旧址。");
                RouteData.SingleEvent singleEvent3 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "故宫", "位于北京中轴线的中心，是明、清两代的皇宫，占地面积约为72万平方米，建筑面积约为15万平方米，是世界上现存规模最大、保存最为完整的木质结构的宫殿型建筑");
                RouteData.SingleEvent singleEvent4 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "北海公园", "位于北京市中心区，城内景山西侧，在故宫的西北面，与中海、南海合称三海。");

                RouteData.SingleEvent singleEvent5 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "王府井", "南起东长安街，北至中国美术馆，全长约1600米，是北京最有名的商业街");
                RouteData.SingleEvent singleEvent6 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "颐和园", "北京市古代皇家园林，前身为清漪园，坐落在北京西郊，距城区十五公里，占地约二百九十公顷，与圆明园毗邻");
                RouteData.SingleEvent singleEvent7 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "老北京庙会自由活动", "是汉族民间宗教及岁时风俗，一般在春节，元宵节等节日举行");


                RouteData.SingleEvent singleEvent8 = new RouteData.SingleEvent(3, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "天坛", "位于故宫东南方，占地273公顷，约为故宫的4倍。");
                RouteData.SingleEvent singleEvent9 = new RouteData.SingleEvent(3, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(11, 0), "北京前门大街", "位于京城中轴线，北起前门月亮湾，南至天桥路口，与天桥南大街相连");
                routeList.add(singleEvent1);

                routeList.add(singleEvent2);
                routeList.add(singleEvent3);
                routeList.add(singleEvent4);
                routeList.add(singleEvent5);
                routeList.add(singleEvent6);
                routeList.add(singleEvent7);
                routeList.add(singleEvent8);
                routeList.add(singleEvent9);


            } else if ((int) v.getTag() == 1) {
                //http://dujia.lvmama.com/group/361528
                RouteData.SingleEvent singleEvent1 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(10, 30), "南溪山", "两峰对峙，高突险峻，犹如两扇白色屏风，有“南溪玉屏”之称。人行壁下，常常可以听到风吹崖壁发出的声音，堪称桂林山水一绝");

                RouteData.SingleEvent singleEvent2 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(13, 0), new Clock(13, 30), "漓江风光", "沿途游客零距离亲近漓江，聆听漓江荡漾之声，观赏桂林漓江两岸如诗如画的风景");
                RouteData.SingleEvent singleEvent3 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(14, 0), new Clock(15, 30), "叠彩山", "饱览“千山环野立，一水抱城流”的桂林市的全景风貌");

                RouteData.SingleEvent singleEvent4 = new RouteData.SingleEvent(2, RouteData.ActivityType.TRAFFIC, new Clock(9, 0), new Clock(10, 30), "阳朔", "游览坐落于十里画廊黄金地段的国家AAAA级景区");
                RouteData.SingleEvent singleEvent5 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(10, 30), new Clock(11, 00), "阳朔大印人文景观园", "拥有三大看点：看千年渔民文化<鱼鹰捕鱼>，看神仙<八仙贺寿秀>和看<七项世界吉尼期纪录>");
                RouteData.SingleEvent singleEvent6 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(14, 0), new Clock(14, 45), "月亮山", "人称“小漓江”，不是漓江胜似漓江");
                RouteData.SingleEvent singleEvent7 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(14, 45), new Clock(15, 30), "阳朔西街", "以尽情享受小镇幽静的环境和独具情韵的休闲时光，体验浪漫的异国风情");

                RouteData.SingleEvent singleEvent8 = new RouteData.SingleEvent(3, RouteData.ActivityType.TRAFFIC, new Clock(9, 0), new Clock(10, 30), "木龙湖.东盟园", "是以东盟10国美食、风情、文化为背景的主题公园，领略美丽的东南亚风情演艺，欣赏秀丽的木龙湖风光");
                RouteData.SingleEvent singleEvent9 = new RouteData.SingleEvent(3, RouteData.ActivityType.SPOT, new Clock(10, 30), new Clock(11, 15), "榕、杉湖景区", "东方威尼斯环城水系桂林最大的中心公园");

                routeList.add(singleEvent1);
                routeList.add(singleEvent2);
                routeList.add(singleEvent3);
                routeList.add(singleEvent4);
                routeList.add(singleEvent5);
                routeList.add(singleEvent6);
                routeList.add(singleEvent7);
                routeList.add(singleEvent8);
                routeList.add(singleEvent9);

            } else if ((int) v.getTag() == 2) {
                RouteData.SingleEvent singleEvent1 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(9, 0), new Clock(10, 30), "蜈支洲岛");
                RouteData.SingleEvent singleEvent2 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(13, 0), new Clock(13, 30), "亚龙湾热带天堂森林公园");
                RouteData.SingleEvent singleEvent3 = new RouteData.SingleEvent(1, RouteData.ActivityType.SPOT, new Clock(14, 0), new Clock(15, 30), "大东海国家旅游度假区");

                RouteData.SingleEvent singleEvent4 = new RouteData.SingleEvent(2, RouteData.ActivityType.TRAFFIC, new Clock(9, 0), new Clock(10, 30), "南山佛教文化苑");
                RouteData.SingleEvent singleEvent5 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(10, 30), new Clock(11, 00), "三亚兰花大世界");
                RouteData.SingleEvent singleEvent6 = new RouteData.SingleEvent(2, RouteData.ActivityType.SPOT, new Clock(14, 0), new Clock(14, 45), "天涯海角");

                RouteData.SingleEvent singleEvent7 = new RouteData.SingleEvent(3, RouteData.ActivityType.TRAFFIC, new Clock(9, 0), new Clock(10, 30), "椰田古寨");
                RouteData.SingleEvent singleEvent8 = new RouteData.SingleEvent(3, RouteData.ActivityType.SPOT, new Clock(10, 30), new Clock(11, 15), "奥特莱斯文化旅游区");
                RouteData.SingleEvent singleEvent9 = new RouteData.SingleEvent(3, RouteData.ActivityType.TRAFFIC, new Clock(9, 0), new Clock(10, 30), "日月湾南海渔村");
                routeList.add(singleEvent1);
                routeList.add(singleEvent2);
                routeList.add(singleEvent3);
                routeList.add(singleEvent4);
                routeList.add(singleEvent5);
                routeList.add(singleEvent6);
                routeList.add(singleEvent7);
                routeList.add(singleEvent8);
                routeList.add(singleEvent9);

            }
            RecommendRouteDetailFragment fragment = new RecommendRouteDetailFragment();
            Bundle bundle = new Bundle();
            bundle.putSerializable("recommand_route", routeList);
            fragment.setArguments(bundle);

            android.support.v4.app.FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.hide(MainFragment.this);
            transaction.add(R.id.container, fragment);
            transaction.addToBackStack(null);
            transaction.commit();

        }
    }


}
