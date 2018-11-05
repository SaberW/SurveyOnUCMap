package cn.creable.surveyOnUCMap;

import android.content.Context;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.annimon.stream.function.Supplier;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;
import com.pili.pldroid.player.AVOptions;
import com.pili.pldroid.player.widget.PLVideoTextureView;

import java.io.File;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import cn.creable.ucmap.openGIS.UCMapView;

/**
 * Created by BluceLee on 2016-10-20.
 */

/**
 * Created by BluceLee on 2016-10-20.
 */

/**
 * ������Ƶ
 * path���ļ�·��
 * isdelete���Ƿ���ʾɾ����ť
 * s�����ɾ����ť��ִ�еĲ���
 */
public class PlayVideo {
    public static void play(final Context context, final String path, boolean isdelete, final Supplier s) {
        if (new File(path).exists()) {
            View view = View.inflate(context, R.layout.playvideo, null);
            final PLVideoTextureView vv = (PLVideoTextureView) view.findViewById(R.id.plvtv_playvideo);
            TextView tv = (TextView) view.findViewById(R.id.tv_playvideo_title);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vv.stopPlayback();
                    DialogUtils.finishDialog(483);
                }
            });
            AVOptions options = new AVOptions();
            options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
            options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000);
            options.setInteger(AVOptions.KEY_LIVE_STREAMING, 0);
            options.setInteger(AVOptions.KEY_MEDIACODEC, 1);
            options.setInteger(AVOptions.KEY_START_ON_PREPARED, 0);
            vv.setAVOptions(options);
            MediaController mc = new MediaController(context, false, false);
            vv.setMediaController(mc);
            vv.setVideoPath(path);
            //vv.setDisplayOrientation(270);
            vv.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_PAVED_PARENT);
            vv.start();
            if (isdelete) {
                Button btn = (Button) view.findViewById(R.id.btn_playvideo_delete);
                btn.setVisibility(View.VISIBLE);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        final NormalDialog dialog = new NormalDialog(context);
                        dialog.content("ȷ��Ҫɾ�����¼���ļ���").widthScale(0.5f).vMargin(85).show();
                        dialog.setOnBtnClickL(new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                            }
                        }, new OnBtnClickL() {
                            @Override
                            public void onBtnClick() {
                                dialog.dismiss();
                                vv.stopPlayback();
                                new File(path).delete();
                                DialogUtils.finishDialog(483);
                                if (s != null) {
                                    s.get();
                                }
                            }
                        });
                    }
                });
            }
            DialogUtils.showFullScreenDialog(context, 483, true, view, new Supplier() {
                @Override
                public Object get() {
                    vv.stopPlayback();
                    return null;
                }
            });
        } else {
            BluToast.makeText(context, "���ļ������ڣ�", BluToast.LENGTH_SHORT).show();
        }
    }

    /**
     * ������Ƶ
     * mapControl����ͼ������
     * bf���ļ�������洫��������Ӧ�ļ�����
     */
    public static void play(final Context context, final UCMapView mapControl, final BluFile bf) {
        if (new File(bf.getPath()).exists()) {
            View view = View.inflate(context, R.layout.playvideo, null);
            final PLVideoTextureView vv = (PLVideoTextureView) view.findViewById(R.id.plvtv_playvideo);
            TextView tv = (TextView) view.findViewById(R.id.tv_playvideo_title);
            tv.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    vv.stopPlayback();
                    DialogUtils.finishDialog(483);
                }
            });
            AVOptions options = new AVOptions();
            options.setInteger(AVOptions.KEY_PREPARE_TIMEOUT, 10 * 1000);
            options.setInteger(AVOptions.KEY_GET_AV_FRAME_TIMEOUT, 10 * 1000);
            options.setInteger(AVOptions.KEY_LIVE_STREAMING, 0);
            options.setInteger(AVOptions.KEY_MEDIACODEC, 1);
            options.setInteger(AVOptions.KEY_START_ON_PREPARED, 0);
            vv.setAVOptions(options);
            final MediaController mc = new MediaController(context, false, false);
            vv.setMediaController(mc);
            vv.setVideoPath(bf.getPath());
            vv.setDisplayOrientation(270);
            vv.setDisplayAspectRatio(PLVideoTextureView.ASPECT_RATIO_PAVED_PARENT);
            vv.start();
            Button btn = (Button) view.findViewById(R.id.btn_playvideo_delete);
            btn.setText("��λҪ��");
            btn.setCompoundDrawables(null, null, null, null);
            btn.setVisibility(View.VISIBLE);
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    ShowWait.show(context, "���Ժ򡣡���");
                    final String[] layername = {""};
//                    Task.callInBackground(new Callable<FeatureInfo>() {
//                        @Override
//                        public FeatureInfo call() throws Exception {
//                            String where = String.format("%s like '%%%s%%'", "��Ƶ", bf.getName());
//                            FeatureInfo[] infos = mapControl.searchFeature("��ʱ��", null, where, null, null, null);
//                            if (infos == null) {
//                                infos = mapControl.searchFeature("��ʱ��", null, where, null, null, null);
//                                if (infos == null) {
//                                    infos = mapControl.searchFeature("��ʱ��", null, where, null, null, null);
//                                    if (infos == null) {
//                                        return null;
//                                    } else {
//                                        layername[0] = "��ʱ��";
//                                        return infos[0];
//                                    }
//                                } else {
//                                    layername[0] = "��ʱ��";
//                                    return infos[0];
//                                }
//                            } else {
//                                layername[0] = "��ʱ��";
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
//                                vv.stopPlayback();
//                                DialogUtils.finishDialog(483);
//                                float current_scale = mapControl.getDisplay().getDisplayTransformation().getZoom();
//                                ILayer searchlayer = GVS.getInstance().layers.get(layername[0]);
//                                float max_scale = searchlayer.getMaximumScale();
//                                if (current_scale > max_scale) {
//                                    mapControl.getDisplay().getDisplayTransformation().setZoom(max_scale / 2);
//                                }
//                                IEnvelope extent = mapControl.getExtent();
//                                IPoint pt = new Point((task.getResult().xmin + task.getResult().xmax) / 2, (task.getResult().ymin + task.getResult().ymax) / 2);
//                                extent.centerAt(pt);
//                                mapControl.slideAnimation(pt.getX(), pt.getY());
//                                mapControl.refreshSync(extent);
//                                mapControl.flashFeature(task.getResult().layerID, task.getResult().objectID);
//                            }
//                            return null;
//                        }
//                    }, Task.UI_THREAD_EXECUTOR);
                }
            });
            DialogUtils.showFullScreenDialog(context, 483, true, view, new Supplier() {
                @Override
                public Object get() {
                    vv.stopPlayback();
                    return null;
                }
            });
        } else {
            BluToast.makeText(context, "���ļ������ڣ�", BluToast.LENGTH_SHORT).show();
        }
    }
}
