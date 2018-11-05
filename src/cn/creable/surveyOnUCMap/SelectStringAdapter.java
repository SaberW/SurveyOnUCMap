package cn.creable.surveyOnUCMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.List;

/**
 * Created by blucelee on 2017/3/27.
 */

public class SelectStringAdapter extends BaseAdapter {
    private Context context;
    private List<String> items;

    public SelectStringAdapter(Context context, List<String> items) {
        this.context = context;
        this.items = items;
    }

    @Override
    public int getCount() {
        return items.size();
    }

    @Override
    public Object getItem(int position) {
        return items.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.items, null);
        }
        TextView tv = (TextView) convertView.findViewById(R.id.tv_items);
        tv.setText(items.get(position));
        return convertView;
    }
}
