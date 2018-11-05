package cn.creable.surveyOnUCMap;


import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by BluceLee on 2016-11-29.
 *
 * ”“…œΩ«µØ≥ˆ≤Àµ•  ≈‰∆˜
 */

public class ShowItemsAdapter extends BaseAdapter {
    private Context context;
    private String[] items;
    private int[] icons;

    public ShowItemsAdapter(Context context, String[] items, int[] icons) {
        this.context = context;
        this.items = items;
        this.icons = icons;
    }

    @Override
    public int getCount() {
        return items.length;
    }

    @Override
    public Object getItem(int position) {
        return items[position];
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.showitems_item, null);
        }
        ImageView pv= (ImageView) convertView.findViewById(R.id.pv_showitems_item);
        pv.setImageResource(icons[position]);
       // PrintView pv = (PrintView) convertView.findViewById(R.id.pv_showitems_item);
        //pv.setIconTextRes(icons[position]);
        TextView tv = (TextView) convertView.findViewById(R.id.tv_showitems_item);
        tv.setText(items[position]);
        return convertView;
    }
}
