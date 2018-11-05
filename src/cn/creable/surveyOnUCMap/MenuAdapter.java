package cn.creable.surveyOnUCMap;

import android.content.Context;
import android.graphics.Color;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class MenuAdapter extends BaseAdapter {
    private Context context;
    private String[] menus;
    private int[] icons;
    private int currentIndex = -1;
    
    public static View view;

    public MenuAdapter(Context context, String[] menus, int[] icons) {
        this.context = context;
        this.menus = menus;
        this.icons = icons;
    }

    public void setDates(String[] menus, int[] icons) {
        this.menus = menus;
        this.icons = icons;
    }

    public void setCurrentIndex(int index) {
        this.currentIndex = index;
        notifyDataSetChanged();
    }

    public String getString(int index) {
        return menus[index];
    }

    public int getIcon(int index) {
        return icons[index];
    }

    public int getCurrentIndex() {
        return currentIndex;
    }

    @Override
    public int getCount() {
        return menus.length;
    }

    @Override
    public Object getItem(int i) {
        return menus[i];
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = View.inflate(context, R.layout.menu_item, null);
            MenuHolder holder = new MenuHolder();
            holder.layout = (LinearLayout) view.findViewById(R.id.ll_menu);
            holder.iv = (ImageView) view.findViewById(R.id.iv_menu);
            holder.tv = (TextView) view.findViewById(R.id.tv_menu);
            view.setTag(holder);
        }
        MenuHolder holder = (MenuHolder) view.getTag();
        if ("底图选择".equals(menus[i]))
        	MenuAdapter.view=view;
        if ("图层控制".equals(menus[i]) || "地图搜索".equals(menus[i]) || "文件管理".equals(menus[i])) {
            if (currentIndex == i) {
                holder.layout.setBackgroundColor(Color.parseColor("#ffd1d1d1"));
            } else {
                holder.layout.setBackgroundColor(Color.parseColor("#ffffffff"));
            }
        } else {
            holder.layout.setBackgroundResource(R.drawable.menu_item_pressed_style);
        }
        holder.iv.setImageResource(icons[i]);
        holder.tv.setText(menus[i]);
        return view;
    }

    public class MenuHolder {
        private LinearLayout layout;
        public ImageView iv;
        public TextView tv;
    }
}