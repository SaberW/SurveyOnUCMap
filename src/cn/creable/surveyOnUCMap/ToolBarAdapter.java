package cn.creable.surveyOnUCMap;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

/**
 * Created by BluceLee on 2016-12-02.
 *
 * µ◊≤øtoolbar  ≈‰∆˜
 */

public class ToolBarAdapter extends BaseAdapter {
    private Context context;
    private ToolBarMenu[] menus;

    public ToolBarAdapter(Context context, ToolBarMenu[] menus) {
        this.context = context;
        this.menus = menus;
    }

    @Override
    public int getCount() {
        return menus.length;
    }

    @Override
    public Object getItem(int position) {
        return menus[position].getName();
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.toolbar_item, null);
        }
        ToolBarMenu menu = menus[position];
        ImageView iv = (ImageView) convertView.findViewById(R.id.iv_toolbar_item_devider);
        if (menu.isShowDevider()) {
            iv.setVisibility(View.VISIBLE);
        } else {
            iv.setVisibility(View.INVISIBLE);
        }
        ImageView pv= (ImageView) convertView.findViewById(R.id.pv_toolbar_item);
        pv.setImageResource(menu.getIcon());
        //PrintView pv = (PrintView) convertView.findViewById(R.id.pv_toolbar_item);
        //pv.setIconTextRes(menu.getIcon());
        TextView tv = (TextView) convertView.findViewById(R.id.tv_toolbar_item);
        tv.setText(menu.getName());
        return convertView;
    }
}