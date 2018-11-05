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
     * 预览照片
     * path：照片路径
     * isdelete：是否显示删除按钮
     * s：点击删除按钮后执行的回调
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
                        dialog.content("确定要删除这张照片吗？").widthScale(0.5f).vMargin(85).show();
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
            BluToast.makeText(context, "该文件不存在！", BluToast.LENGTH_SHORT).show();
        }
    }

    /**
     * 预览地图截图
     * mc：地图控制器
     * info：保存的地图信息
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
            btn.setText("转到地图");
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
            BluToast.makeText(context, "该文件不存在！", BluToast.LENGTH_SHORT).show();
        }
    }

    /**
     * 预览照片
     * mc：地图控制器
     * bf：文件管理界面传过来的相应文件对象
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
            btn.setText("定位要素");
            btn.setCompoundDrawables(null, null, null, null);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowWait.show(context, "请稍候。。。");
                    final String[] layername = {""};
//                    Task.callInBackground(new Callable<FeatureInfo>() {
//                        @Override
//
//                        public FeatureInfo call() throws Exception {
//                            String where = String.format("%s like '%%%s%%'", "图片", bf.getName());
//                            FeatureInfo[] infos = mc.searchFeature("执法临时点", null, where, null, null, null);
//                            if (infos == null) {
//                                infos = mc.searchFeature("执法临时线", null, where, null, null, null);
//                                if (infos == null) {
//                                    infos = mc.searchFeature("执法临时面", null, where, null, null, null);
//                                    if (infos == null) {
//                                        infos = mc.searchFeature("巡查临时面", null, where, null, null, null);
//                                        if (infos == null) {
//                                            infos = mc.searchFeature("巡查临时线", null, where, null, null, null);
//                                            if (infos == null) {
//                                                infos = mc.searchFeature("巡查临时点", null, where, null, null, null);
//                                                if (infos == null) {
//                                                    return null;
//                                                } else {
//                                                    layername[0] = "巡查临时点";
//                                                    return infos[0];
//                                                }
//                                            } else {
//                                                layername[0] = "巡查临时线";
//                                                return infos[0];
//                                            }
//                                        } else {
//                                            layername[0] = "巡查临时面";
//                                            return infos[0];
//                                        }
//                                    } else {
//                                        layername[0] = "执法临时面";
//                                        return infos[0];
//                                    }
//                                } else {
//                                    layername[0] = "执法临时线";
//                                    return infos[0];
//                                }
//                            } else {
//                                layername[0] = "执法临时点";
//                                return infos[0];
//                            }
//                        }
//                    }).onSuccess(new Continuation<FeatureInfo, Object>() {
//                        @Override
//                        public Object then(Task<FeatureInfo> task) throws Exception {
//                            ShowWait.finish();
//                            if (task.getResult() == null) {
//                                BluToast.makeText(context, "没有搜索到匹配的要素！", BluToast.LENGTH_SHORT).show();
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
            BluToast.makeText(context, "该文件不存在！", BluToast.LENGTH_SHORT).show();
        }
    }
        }
