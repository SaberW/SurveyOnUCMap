package cn.creable.surveyOnUCMap;

import android.content.Context;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.annimon.stream.function.Consumer;
import java.util.List;

/**
 * Created by blucelee on 2017/3/27.
 */

public class SelectString {
    public static void select(Context context, String title, List<String> items, final Consumer<String> consumer) {
        View view = View.inflate(context, R.layout.selectstring, null);
        TextView tv_title = (TextView) view.findViewById(R.id.tv_selectstring);
        tv_title.setText(title);
        ListView lv = (ListView) view.findViewById(R.id.lv_selectstring);
        lv.setAdapter(new SelectStringAdapter(context, items));
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String item = (String) parent.getItemAtPosition(position);
                if (consumer != null) {
                    consumer.accept(item);
                }
                DialogUtils.finishDialog(593);
            }
        });
        DialogUtils.show(context, 593, true, view, 300, 400, null);
    }
}
