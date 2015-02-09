package com.eztrip.navigator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.eztrip.R;


/**
 * Created by Steve on 15-02-08.
 */
public class DrawerListViewAdapter extends ArrayAdapter<String> {

    private Context context;
    private String[] values;   //各模块标题
    private int[] icons;       //各模块icon

    public DrawerListViewAdapter(Context context, String[] values, int[] icons) {
        super(context, R.layout.drawer_list_item, values);
        this.context = context;
        this.values = values;
        this.icons = icons;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View rowView = convertView;
        if (rowView == null) {
            LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            rowView = inflater.inflate(R.layout.drawer_list_item, parent, false);
            ViewHolder viewHolder = new ViewHolder();

            viewHolder.textView = (TextView) rowView.findViewById(R.id.left_drawer_text);
            viewHolder.imageView = (ImageView) rowView.findViewById(R.id.left_drawer_image);
            rowView.setTag(viewHolder);  //保存views到viewHolder中

        }

        ViewHolder viewHolder = (ViewHolder) rowView.getTag();  //取得viewHolder中的views
        viewHolder.textView.setText(values[position]);
        viewHolder.imageView.setImageResource(icons[position]);

        return rowView;
    }

    /**
     * 用于提高ListView的性能
     *
     * @link http://www.vogella.com/tutorials/AndroidListView/article.html#adapterperformance_holder
     * 由于 findViewById() 方法较费时间，所以使用此内部类保存ListView中相应的views
     * 可以通过setTag()方法保存views到ViewHolder类中，再通过getTag()方法取得views，避免了重复使用findViewById()
     */
    static class ViewHolder {
        TextView textView;
        ImageView imageView;
    }
}