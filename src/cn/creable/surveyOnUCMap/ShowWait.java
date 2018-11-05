package cn.creable.surveyOnUCMap;


import android.content.Context;
import android.view.View;
import android.widget.TextView;


/**
 * Created by BluceLee on 2016-11-29.
 * 显示等待动画
 */

public class ShowWait {
    public static void show(Context context, String msg) {
        View view = View.inflate(context, R.layout.wait, null);
        TextView tv = (TextView) view.findViewById(R.id.tv_wait);
        tv.setText(msg);
        DialogUtils.show(context, 901, false, view, 250, 50, null);
    }

    //结束等待
    public static void finish() {
        DialogUtils.finishDialog(901);
    }
}
