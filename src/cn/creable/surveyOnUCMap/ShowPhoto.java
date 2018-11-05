package cn.creable.surveyOnUCMap;

import android.content.Context;
import android.net.Uri;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.annimon.stream.function.Supplier;
import com.davemorrissey.labs.subscaleview.ImageSource;
import com.davemorrissey.labs.subscaleview.SubsamplingScaleImageView;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;

import java.io.File;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import cn.creable.ucmap.openGIS.UCMapView;

/**
 * Created by BluceLee on 2016-10-20.
 */

public class ShowPhoto {
    /**
     * Ԥ����Ƭ
     * path����Ƭ·��
     * isdelete���Ƿ���ʾɾ����ť
     * s�����ɾ����ť��ִ�еĻص�
     */
    public static void show(final Context context, String path, boolean isdelete, final Supplier s) {
        if (new File(path).exists()) {
            View view = View.inflate(context, R.layout.showphoto, null);
            TextView tv = (TextView) view.findViewById(R.id.tv_showphoto_title);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogUtils.finishDialog(261);
                }
            });
            SubsamplingScaleImageView ssiv = (SubsamplingScaleImageView) view.findViewById(R.id.ssiv_showphoto);
            ssiv.setMaxScale(5f);
            ssiv.setImage(ImageSource.uri(Uri.fromFile(new File(path))));
            ssiv.setRotation(ssiv.getRotation() + BluBitMapUtils.getBitmapDegree(path));
            if (isdelete) {
                Button btn = (Button) view.findViewById(R.id.btn_showphoto_delete);
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final NormalDialog dialog = new NormalDialog(context);
                        dialog.content("ȷ��Ҫɾ��������Ƭ��").widthScale(0.5f).vMargin(85).show();
                        dialog.setOnBtnClickL(new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                            }
                        }, new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                                DialogUtils.finishDialog(261);
                                if (s != null) {
                                    s.get();
                                }
                            }
                        });
                    }
                });
            }
            DialogUtils.showFullScreenDialog(context, 261, true, view, null);
        } else {
            BluToast.makeText(context, "���ļ������ڣ�", BluToast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ԥ����ͼ��ͼ
     * mc����ͼ������
     * info������ĵ�ͼ��Ϣ
     */
    public static void show(final Context context, final UCMapView mc, String path, final MapInfo info) {
        if (new File(path).exists()) {
            View view = View.inflate(context, R.layout.showphoto, null);
            TextView tv = (TextView) view.findViewById(R.id.tv_showphoto_title);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogUtils.finishDialog(261);
                }
            });
            SubsamplingScaleImageView ssiv = (SubsamplingScaleImageView) view.findViewById(R.id.ssiv_showphoto);
            ssiv.setMaxScale(5f);
            ssiv.setImage(ImageSource.uri(Uri.fromFile(new File(path))));
            ssiv.setRotation(ssiv.getRotation() + BluBitMapUtils.getBitmapDegree(path));
            Button btn = (Button) view.findViewById(R.id.btn_showphoto_delete);
            btn.setVisibility(View.VISIBLE);
            btn.setText("ת����ͼ");
            btn.setCompoundDrawables(null, null, null, null);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogUtils.finishDialog(261);
                    for (FeatureState state : info.getStates()) {
                        GVS.getInstance().layers.get(state.getFeatureName()).setVisible(state.isVisible());
                    }
                    mc.moveTo(info.getxCenter(), info.getyCenter(), info.getScale());
//                    mc.getDisplay().getDisplayTransformation().setZoom(info.getScale());
//                    IEnvelope extent = mc.getExtent();
//                    IPoint pt = new Point(info.getxCenter(), info.getyCenter());
//                    extent.centerAt(pt);
//                    mc.slideAnimation(pt.getX(), pt.getY());
//                    mc.refreshSync(extent);
                }
            });
            DialogUtils.showFullScreenDialog(context, 261, true, view, null);
        } else {
            BluToast.makeText(context, "���ļ������ڣ�", BluToast.LENGTH_SHORT).show();
        }
    }

    /**
     * Ԥ����Ƭ
     * mc����ͼ������
     * bf���ļ�������洫��������Ӧ�ļ�����
     */
    public static void show(final Context context, final UCMapView mc, final BluFile bf) {
        if (new File(bf.getPath()).exists()) {
            View view = View.inflate(context, R.layout.showphoto, null);
            TextView tv = (TextView) view.findViewById(R.id.tv_showphoto_title);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    DialogUtils.finishDialog(261);
                }
            });
            SubsamplingScaleImageView ssiv = (SubsamplingScaleImageView) view.findViewById(R.id.ssiv_showphoto);
            ssiv.setMaxScale(5f);
            ssiv.setImage(ImageSource.uri(Uri.fromFile(new File(bf.getPath()))));
            ssiv.setRotation(ssiv.getRotation() + BluBitMapUtils.getBitmapDegree(bf.getPath()));
            Button btn = (Button) view.findViewById(R.id.btn_showphoto_delete);
            btn.setVisibility(View.VISIBLE);
            btn.setText("��λҪ��");
            btn.setCompoundDrawables(null, null, null, null);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowWait.show(context, "���Ժ򡣡���");
                    final String[] layername = {""};
//                    Task.callInBackground(new Callable<FeatureInfo>() {
//                        @Override
//
//                        public FeatureInfo call() throws Exception {
//                            String where = String.format("%s like '%%%s%%'", "ͼƬ", bf.getName());
//                            FeatureInfo[] infos = mc.searchFeature("ִ����ʱ��", null, where, null, null, null);
//                            if (infos == null) {
//                                infos = mc.searchFeature("ִ����ʱ��", null, where, null, null, null);
//                                if (infos == null) {
//                                    infos = mc.searchFeature("ִ����ʱ��", null, where, null, null, null);
//                                    if (infos == null) {
//                                        infos = mc.searchFeature("Ѳ����ʱ��", null, where, null, null, null);
//                                        if (infos == null) {
//                                            infos = mc.searchFeature("Ѳ����ʱ��", null, where, null, null, null);
//                                            if (infos == null) {
//                                                infos = mc.searchFeature("Ѳ����ʱ��", null, where, null, null, null);
//                                                if (infos == null) {
//                                                    return null;
//                                                } else {
//                                                    layername[0] = "Ѳ����ʱ��";
//                                                    return infos[0];
//                                                }
//                                            } else {
//                                                layername[0] = "Ѳ����ʱ��";
//                                                return infos[0];
//                                            }
//                                        } else {
//                                            layername[0] = "Ѳ����ʱ��";
//                                            return infos[0];
//                                        }
//                                    } else {
//                                        layername[0] = "ִ����ʱ��";
//                                        return infos[0];
//                                    }
//                                } else {
//                                    layername[0] = "ִ����ʱ��";
//                                    return infos[0];
//                                }
//                            } else {
//                                layername[0] = "ִ����ʱ��";
//                                return infos[0];
//                            }
//                        }
//                    }).onSuccess(new Continuation<FeatureInfo, Object>() {
//                        @Override
//                        public Object then(Task<FeatureInfo> task) throws Exception {
//                            ShowWait.finish();
//                            if (task.getResult() == null) {
//                                BluToast.makeText(context, "û��������ƥ���Ҫ�أ�", BluToast.LENGTH_SHORT).show();
//                            } else {
//                                DialogUtils.finishDialog(261);
//                                float current_scale = mc.getDisplay().getDisplayTransformation().getZoom();
//                                ILayer searchlayer = GVS.getInstance().layers.get(layername[0]);
//                                float max_scale = searchlayer.getMaximumScale();
//                                if (current_scale > max_scale) {
//                                    mc.getDisplay().getDisplayTransformation().setZoom(max_scale / 2);
//                                }
//                                IEnvelope extent = mc.getExtent();
//                                IPoint pt = new Point((task.getResult().xmin + task.getResult().xmax) / 2, (task.getResult().ymin + task.getResult().ymax) / 2);
//                                extent.centerAt(pt);
//                                mc.slideAnimation(pt.getX(), pt.getY());
//                                mc.refreshSync(extent);
//                                mc.flashFeature(task.getResult().layerID, task.getResult().objectID);
//                            }
//                            return null;
//                        }
//                    }, Task.UI_THREAD_EXECUTOR);
                }
            });
            DialogUtils.showFullScreenDialog(context, 261, true, view, null);
        } else {
            BluToast.makeText(context, "���ļ������ڣ�", BluToast.LENGTH_SHORT).show();
        }
    }
        }
