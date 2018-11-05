package cn.creable.surveyOnUCMap;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.annimon.stream.function.Supplier;

import java.util.HashMap;
import java.util.Map;


/**
 * Created by BluceLee on 2016-09-21.
 * 对话框管理器
 */

public class DialogUtils {

    private static Map<Integer, Dialog> dialogs;//用于管理所有的对话框

    static {
        dialogs = new HashMap<Integer, Dialog>();
    }

    /**
     * 弹出对话框
     *
     * @param context
     * @param id      对话框id
     * @param model   点击对话框外部是否可关闭对话框
     * @param view    对话框中显示的布局
     * @param width   对话框宽度
     * @param height  对话框高度
     * @param s       如果点击外部可取消，在取消的时候执行的回调
     */
    public static void show(Context context, final int id, boolean model, View view, int width, int height, final Supplier s) {
        if (dialogs.containsKey(id)) {
            BluToast.makeText(context, "ID为" + id + "的对话框已经存在,请修改对话框ID!", BluToast.LENGTH_SHORT).show();
        } else {
            final Dialog dialog = new Dialog(context, R.style.dialogblack);
            dialog.setCancelable(model);
            if (model) {
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog1) {
                        dialogs.remove(id);
                        if (s != null) {
                            s.get();
                        }
                    }
                });
            }
            dialog.setContentView(view);
            Window dialogWindow = dialog.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.setGravity(Gravity.CENTER);
            lp.x = 0;
            lp.y = 0;
            lp.width = dipToPx(context, width);
            lp.height = dipToPx(context, height);
            lp.alpha = 1.0f;
            dialogWindow.setAttributes(lp);
            dialogWindow.setWindowAnimations(R.style.dialoganimleft);
            dialogs.put(id, dialog);
            dialog.show();
        }
    }

    /**
     * 弹出全屏的对话框
     *
     * @param context
     * @param id      对话框id
     * @param model   点击外部是否可取消
     * @param view    对话框显示的布局
     * @param s       取消回调
     */
    public static void showFullScreenDialog(Context context, final int id, boolean model, View view, final Supplier s) {
        if (dialogs.containsKey(id)) {
            BluToast.makeText(context, "ID为" + id + "的对话框已经存在,请修改对话框ID!", BluToast.LENGTH_SHORT).show();
        } else {
            final Dialog dialog = new Dialog(context, R.style.dialogblack);
            dialog.setCancelable(model);
            if (model) {
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog1) {
                        dialogs.remove(id);
                        if (s != null) {
                            s.get();
                        }
                    }
                });
            }
            dialog.setContentView(view);
            Window dialogWindow = dialog.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.getDecorView().setPadding(0, 0, 0, 0);
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.alpha = 1.0f;
            dialogWindow.setAttributes(lp);
            dialogWindow.setWindowAnimations(R.style.dialoganimleft);
            dialogs.put(id, dialog);
            dialog.show();
        }
    }

    /**
     * 弹出全屏对话框
     *
     * @param context
     * @param id           对话框id
     * @param model        点击外部是否可取消
     * @param view         对话框显示的布局
     * @param marginLeft   对话框距离左边的距离
     * @param marginTop    对话框距离上边的距离
     * @param marginRight  对话框距离右边的距离
     * @param marginBottom 对话框距离下边的距离
     * @param s            取消回调
     */
    public static void showFullScreenDialog(Context context, final int id, boolean model, View view, int marginLeft, int marginTop, int marginRight, int marginBottom, final Supplier s) {
        if (dialogs.containsKey(id)) {
            BluToast.makeText(context, "ID为" + id + "的对话框已经存在,请修改对话框ID!", BluToast.LENGTH_SHORT).show();
        } else {
            final Dialog dialog = new Dialog(context, R.style.dialogblack);
            dialog.setCancelable(model);
            if (model) {
                dialog.setOnCancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialog1) {
                        dialogs.remove(id);
                        if (s != null) {
                            s.get();
                        }
                    }
                });
            }
            dialog.setContentView(view);
            Window dialogWindow = dialog.getWindow();
            WindowManager.LayoutParams lp = dialogWindow.getAttributes();
            dialogWindow.getDecorView().setPadding(marginLeft, marginTop, marginRight, marginBottom);
            lp.width = WindowManager.LayoutParams.MATCH_PARENT;
            lp.height = WindowManager.LayoutParams.MATCH_PARENT;
            lp.alpha = 1.0f;
            dialogWindow.setAttributes(lp);
            dialogWindow.setWindowAnimations(R.style.dialoganimleft);
            dialogs.put(id, dialog);
            dialog.show();
        }
    }

    private static int dipToPx(Context context, int value) {
        return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, (float) value, context.getResources().getDisplayMetrics());
    }

    public static boolean isContainsDialog(int id) {
        return dialogs.containsKey(id);
    }

    public static Dialog getDialog(int id) {
        return dialogs.get(id);
    }

    //隐藏对话框
    public static void closeDialog(int id) {
        if (dialogs.containsKey(id)) {
            Dialog dialog = dialogs.get(id);
            dialog.dismiss();
        }
    }

    //重新显示对话框
    public static void showDialog(int id) {
        if (dialogs.containsKey(id)) {
            dialogs.get(id).show();
        }
    }

    //关闭对话框
    public static void finishDialog(int id) {
        if (dialogs.containsKey(id)) {
            dialogs.get(id).dismiss();
            dialogs.remove(id);
        }
    }

    //关闭所有对话框
    public static void finishAllDialog() {
        Stream.of(dialogs).forEach(new Consumer<Map.Entry<Integer, Dialog>>() {
            @Override
            public void accept(Map.Entry<Integer, Dialog> integerDialogEntry) {
                integerDialogEntry.getValue().dismiss();
            }
        });
        dialogs.clear();
    }
}
