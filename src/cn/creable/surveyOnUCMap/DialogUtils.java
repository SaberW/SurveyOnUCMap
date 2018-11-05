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
 * �Ի��������
 */

public class DialogUtils {

    private static Map<Integer, Dialog> dialogs;//���ڹ������еĶԻ���

    static {
        dialogs = new HashMap<Integer, Dialog>();
    }

    /**
     * �����Ի���
     *
     * @param context
     * @param id      �Ի���id
     * @param model   ����Ի����ⲿ�Ƿ�ɹرնԻ���
     * @param view    �Ի�������ʾ�Ĳ���
     * @param width   �Ի�����
     * @param height  �Ի���߶�
     * @param s       �������ⲿ��ȡ������ȡ����ʱ��ִ�еĻص�
     */
    public static void show(Context context, final int id, boolean model, View view, int width, int height, final Supplier s) {
        if (dialogs.containsKey(id)) {
            BluToast.makeText(context, "IDΪ" + id + "�ĶԻ����Ѿ�����,���޸ĶԻ���ID!", BluToast.LENGTH_SHORT).show();
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
     * ����ȫ���ĶԻ���
     *
     * @param context
     * @param id      �Ի���id
     * @param model   ����ⲿ�Ƿ��ȡ��
     * @param view    �Ի�����ʾ�Ĳ���
     * @param s       ȡ���ص�
     */
    public static void showFullScreenDialog(Context context, final int id, boolean model, View view, final Supplier s) {
        if (dialogs.containsKey(id)) {
            BluToast.makeText(context, "IDΪ" + id + "�ĶԻ����Ѿ�����,���޸ĶԻ���ID!", BluToast.LENGTH_SHORT).show();
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
     * ����ȫ���Ի���
     *
     * @param context
     * @param id           �Ի���id
     * @param model        ����ⲿ�Ƿ��ȡ��
     * @param view         �Ի�����ʾ�Ĳ���
     * @param marginLeft   �Ի��������ߵľ���
     * @param marginTop    �Ի�������ϱߵľ���
     * @param marginRight  �Ի�������ұߵľ���
     * @param marginBottom �Ի�������±ߵľ���
     * @param s            ȡ���ص�
     */
    public static void showFullScreenDialog(Context context, final int id, boolean model, View view, int marginLeft, int marginTop, int marginRight, int marginBottom, final Supplier s) {
        if (dialogs.containsKey(id)) {
            BluToast.makeText(context, "IDΪ" + id + "�ĶԻ����Ѿ�����,���޸ĶԻ���ID!", BluToast.LENGTH_SHORT).show();
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

    //���ضԻ���
    public static void closeDialog(int id) {
        if (dialogs.containsKey(id)) {
            Dialog dialog = dialogs.get(id);
            dialog.dismiss();
        }
    }

    //������ʾ�Ի���
    public static void showDialog(int id) {
        if (dialogs.containsKey(id)) {
            dialogs.get(id).show();
        }
    }

    //�رնԻ���
    public static void finishDialog(int id) {
        if (dialogs.containsKey(id)) {
            dialogs.get(id).dismiss();
            dialogs.remove(id);
        }
    }

    //�ر����жԻ���
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
