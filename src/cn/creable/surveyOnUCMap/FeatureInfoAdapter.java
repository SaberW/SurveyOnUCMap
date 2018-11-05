package cn.creable.surveyOnUCMap;

import java.text.SimpleDateFormat;
import java.util.Vector;

import org.jeo.vector.Feature;
import org.jeo.vector.Field;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

/**
 * Created by blucelee on 2016/11/29.
 * µØÍ¼ËÑË÷ÊÊÅäÆ÷
 */

public class FeatureInfoAdapter extends BaseAdapter {
    private Context context;
    private Vector<Feature> features;
    private Field f;


    public FeatureInfoAdapter(Context context, Vector<Feature> features,Field field) {
        this.context = context;
        this.features = features;
        this.f=field;
    }

    @Override
    public int getCount() {
        return features.size();
    }

    @Override
    public Object getItem(int i) {
        return features.elementAt(i);
    }

    @Override
    public long getItemId(int i) {
        return i;
    }

    @Override
    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = View.inflate(context, R.layout.showitems_item, null);
        }
        TextView tv = (TextView) view.findViewById(R.id.tv_showitems_item);
        
        Object value=features.elementAt(i).get(f.name());
		String valueString=null;
		if (f.type()==Byte.class)
			valueString=Byte.toString((Byte)value);
		else if (f.type()==Short.class)
			valueString=Short.toString((Short)value);
		else if (f.type()==Integer.class)
			valueString=Integer.toString((Integer)value);
		else if (f.type()==Long.class)
			valueString=Long.toString((Long)value);
		else if (f.type()==Float.class)
			valueString=Float.toString((Float)value);
		else if (f.type()==Double.class)
			valueString=Double.toString((Double)value);
		else if (f.type()==java.sql.Date.class)
		{
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			valueString=format.format((java.sql.Date)value);
		}
		else if (f.type()==java.sql.Time.class)
		{
			SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			valueString=format.format((java.sql.Time)value);
		}
		else if (f.type()==String.class)
			valueString=(String)value;
        
        tv.setText(valueString);
        return view;
    }
}

