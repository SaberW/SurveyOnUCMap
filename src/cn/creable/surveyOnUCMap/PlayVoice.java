package cn.creable.surveyOnUCMap;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.annimon.stream.function.Supplier;
import com.flyco.dialog.listener.OnBtnClickL;
import com.flyco.dialog.widget.NormalDialog;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import bolts.Continuation;
import bolts.Task;
import cn.creable.ucmap.openGIS.UCMapView;

/**
 * Created by BluceLee on 2016-10-20.
 */

public class PlayVoice {
    private int flag = 0;
    private Handler handler;
    private Runnable runnable_play;
    private MediaPlayer mp;

    /**
     * 播放视频
     * path：录音文件路径
     * isDelete：是否显示删除按钮
     * s：点击删除按钮后执行的回调
     */
    public void play(final Context context, final String path, boolean isDelete, final Supplier s) {
        if (path == null || "".equals(path)) {
            BluToast.makeText(context, "文件路径不能为空！", BluToast.LENGTH_SHORT).show();
        } else {
            if (new File(path).exists()) {
                View view = View.inflate(context, R.layout.playvoice, null);
                TextView tv_title = (TextView) view.findViewById(R.id.tv_playvoice_filename);
                tv_title.setText(path);
                final SeekBar sb = (SeekBar) view.findViewById(R.id.sb_playvoice);
                final ImageView iv = (ImageView) view.findViewById(R.id.iv_playvoice_recorder);
                final ImageView iv_play = (ImageView) view.findViewById(R.id.iv_playvoice_play);
                final TextView tv_position = (TextView) view.findViewById(R.id.tv_playvoice_position);
                handler = new Handler();
                mp = new MediaPlayer();
                try {
                    mp.setDataSource(path);
                    mp.prepare();
                } catch (IOException e) {
                }
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        flag = 0;
                        iv_play.setVisibility(View.VISIBLE);
                        iv.setBackgroundResource(R.drawable.record_round_blue_bg);
                        tv_position.setText("00:00");
                        sb.setProgress(0);
                        sb.setEnabled(false);
                        handler.removeCallbacks(runnable_play);
                    }
                });
                runnable_play = new Runnable() {
                    @Override
                    public void run() {
                        int ll = mp.getCurrentPosition();
                        sb.setProgress(ll);
                        int ffz = ll / 1000 / 60;
                        int mmz = ll / 1000 % 60;
                        String ssfz = ffz + "";
                        if (ffz < 10) {
                            ssfz = "0" + ffz;
                        }
                        String ssmz = mmz + "";
                        if (mmz < 10) {
                            ssmz = "0" + mmz;
                        }
                        tv_position.setText(ssfz + ":" + ssmz);
                        handler.postDelayed(runnable_play, 500);
                    }
                };
                int l = mp.getDuration();
                sb.setMax(l);
                sb.setProgress(0);
                sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mp.seekTo(sb.getProgress());
                    }
                });
                sb.setEnabled(false);
                int fz = l / 1000 / 60;
                int mz = l / 1000 % 60;
                String sfz = fz + "";
                if (fz < 10) {
                    sfz = "0" + fz;
                }
                String smz = mz + "";
                if (mz < 10) {
                    smz = "0" + mz;
                }
                TextView tv_length = (TextView) view.findViewById(R.id.tv_playvoice_length);
                tv_length.setText(sfz + ":" + smz);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (flag == 0) {
                            mp.start();
                            handler.postDelayed(runnable_play, 500);
                            flag = 1;
                            sb.setEnabled(true);
                            iv_play.setVisibility(View.GONE);
                            iv.setBackgroundResource(R.drawable.record_round_red_bg);
                        } else {
                            mp.stop();
                            try {
                                mp.prepare();
                            } catch (IOException e) {
                            }
                            sb.setEnabled(false);
                            handler.removeCallbacks(runnable_play);
                            flag = 0;
                            iv_play.setVisibility(View.VISIBLE);
                            iv.setBackgroundResource(R.drawable.record_round_blue_bg);
                        }
                    }
                });
                TextView tv_back = (TextView) view.findViewById(R.id.tv_playvoice_title);
                tv_back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mp != null) {
                            mp.stop();
                            mp.release();
                        }
                        handler.removeCallbacks(runnable_play);
                        DialogUtils.finishDialog(7683);
                    }
                });
                if (isDelete) {
                    Button btn_delete = (Button) view.findViewById(R.id.btn_playvoice_delete);
                    btn_delete.setVisibility(View.VISIBLE);
                    btn_delete.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            final NormalDialog dialog = new NormalDialog(context);
                            dialog.content("确定要删除这个录音文件吗？").widthScale(0.5f).vMargin(85).show();
                            dialog.setOnBtnClickL(new OnBtnClickL() {
                                @Override
                                public void onBtnClick() {
                                    dialog.dismiss();
                                }
                            }, new OnBtnClickL() {
                                @Override
                                public void onBtnClick() {
                                    dialog.dismiss();
                                    new File(path).delete();
                                    if (mp != null) {
                                        mp.stop();
                                        mp.release();
                                    }
                                    handler.removeCallbacks(runnable_play);
                                    DialogUtils.finishDialog(7683);
                                    if (s != null) {
                                        s.get();
                                    }
                                }
                            });
                        }
                    });
                }
                DialogUtils.showFullScreenDialog(context, 7683, true, view, new Supplier() {
                    @Override
                    public Object get() {
                        if (mp != null) {
                            mp.stop();
                            mp.release();
                        }
                        handler.removeCallbacks(runnable_play);
                        return null;
                    }
                });
            } else {
                BluToast.makeText(context, "该文件不存在！", BluToast.LENGTH_SHORT).show();
            }
        }
    }

    /**
     * 播放录音
     * mc：地图控制器
     * bf：文件管理界面传过来的相应文件对象
     */
    public void play(final Context context, final UCMapView mc, final BluFile bf) {
        if (bf.getPath() == null || "".equals(bf.getPath())) {
            BluToast.makeText(context, "文件路径不能为空！", BluToast.LENGTH_SHORT).show();
        } else {
            if (new File(bf.getPath()).exists()) {
                View view = View.inflate(context, R.layout.playvoice, null);
                TextView tv_title = (TextView) view.findViewById(R.id.tv_playvoice_filename);
                tv_title.setText(bf.getPath());
                final SeekBar sb = (SeekBar) view.findViewById(R.id.sb_playvoice);
                final ImageView iv = (ImageView) view.findViewById(R.id.iv_playvoice_recorder);
                final ImageView iv_play = (ImageView) view.findViewById(R.id.iv_playvoice_play);
                final TextView tv_position = (TextView) view.findViewById(R.id.tv_playvoice_position);
                handler = new Handler();
                mp = new MediaPlayer();
                try {
                    mp.setDataSource(bf.getPath());
                    mp.prepare();
                } catch (IOException e) {
                }
                mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                    @Override
                    public void onCompletion(MediaPlayer mp) {
                        flag = 0;
                        iv_play.setVisibility(View.VISIBLE);
                        iv.setBackgroundResource(R.drawable.record_round_blue_bg);
                        tv_position.setText("00:00");
                        sb.setProgress(0);
                        sb.setEnabled(false);
                        handler.removeCallbacks(runnable_play);
                    }
                });
                runnable_play = new Runnable() {
                    @Override
                    public void run() {
                        int ll = mp.getCurrentPosition();
                        sb.setProgress(ll);
                        int ffz = ll / 1000 / 60;
                        int mmz = ll / 1000 % 60;
                        String ssfz = ffz + "";
                        if (ffz < 10) {
                            ssfz = "0" + ffz;
                        }
                        String ssmz = mmz + "";
                        if (mmz < 10) {
                            ssmz = "0" + mmz;
                        }
                        tv_position.setText(ssfz + ":" + ssmz);
                        handler.postDelayed(runnable_play, 500);
                    }
                };
                int l = mp.getDuration();
                sb.setMax(l);
                sb.setProgress(0);
                sb.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
                    @Override
                    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

                    }

                    @Override
                    public void onStartTrackingTouch(SeekBar seekBar) {

                    }

                    @Override
                    public void onStopTrackingTouch(SeekBar seekBar) {
                        mp.seekTo(sb.getProgress());
                    }
                });
                sb.setEnabled(false);
                int fz = l / 1000 / 60;
                int mz = l / 1000 % 60;
                String sfz = fz + "";
                if (fz < 10) {
                    sfz = "0" + fz;
                }
                String smz = mz + "";
                if (mz < 10) {
                    smz = "0" + mz;
                }
                TextView tv_length = (TextView) view.findViewById(R.id.tv_playvoice_length);
                tv_length.setText(sfz + ":" + smz);
                iv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (flag == 0) {
                            mp.start();
                            handler.postDelayed(runnable_play, 500);
                            flag = 1;
                            sb.setEnabled(true);
                            iv_play.setVisibility(View.GONE);
                            iv.setBackgroundResource(R.drawable.record_round_red_bg);
                        } else {
                            mp.stop();
                            try {
                                mp.prepare();
                            } catch (IOException e) {
                            }
                            sb.setEnabled(false);
                            handler.removeCallbacks(runnable_play);
                            flag = 0;
                            iv_play.setVisibility(View.VISIBLE);
                            iv.setBackgroundResource(R.drawable.record_round_blue_bg);
                        }
                    }
                });
                TextView tv_back = (TextView) view.findViewById(R.id.tv_playvoice_title);
                tv_back.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (mp != null) {
                            mp.stop();
                            mp.release();
                        }
                        handler.removeCallbacks(runnable_play);
                        DialogUtils.finishDialog(7683);
                    }
                });
                Button btn_delete = (Button) view.findViewById(R.id.btn_playvoice_delete);
                btn_delete.setText("定位要素");
                btn_delete.setCompoundDrawables(null, null, null, null);
                btn_delete.setVisibility(View.VISIBLE);
                btn_delete.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        ShowWait.show(context, "请稍候。。。");
                        final String[] layername = {""};
//                        Task.callInBackground(new Callable<FeatureInfo>() {
//                            @Override
//                            public FeatureInfo call() throws Exception {
//                                String where = String.format("%s like '%%%s%%'", "录音", bf.getName());
//                                FeatureInfo[] infos = mc.searchFeature("临时点", null, where, null, null, null);
//                                if (infos == null) {
//                                    infos = mc.searchFeature("临时线", null, where, null, null, null);
//                                    if (infos == null) {
//                                        infos = mc.searchFeature("临时面", null, where, null, null, null);
//                                        if (infos == null) {
//                                            return null;
//                                        } else {
//                                            layername[0] = "临时面";
//                                            return infos[0];
//                                        }
//                                    } else {
//                                        layername[0] = "临时线";
//                                        return infos[0];
//                                    }
//                                } else {
//                                    layername[0] = "临时点";
//                                    return infos[0];
//                                }
//                            }
//                        }).onSuccess(new Continuation<FeatureInfo, Object>() {
//                            @Override
//                            public Object then(Task<FeatureInfo> task) throws Exception {
//                                ShowWait.finish();
//                                if (task.getResult() == null) {
//                                    BluToast.makeText(context, "没有搜索到匹配的要素！", BluToast.LENGTH_SHORT).show();
//                                } else {
//                                    if (mp != null) {
//                                        mp.stop();
//                                        mp.release();
//                                    }
//                                    handler.removeCallbacks(runnable_play);
//                                    DialogUtils.finishDialog(7683);
//                                    float current_scale = mc.getDisplay().getDisplayTransformation().getZoom();
//                                    ILayer searchlayer = GVS.getInstance().layers.get(layername[0]);
//                                    float max_scale = searchlayer.getMaximumScale();
//                                    if (current_scale > max_scale) {
//                                        mc.getDisplay().getDisplayTransformation().setZoom(max_scale / 2);
//                                    }
//                                    IEnvelope extent = mc.getExtent();
//                                    IPoint pt = new Point((task.getResult().xmin + task.getResult().xmax) / 2, (task.getResult().ymin + task.getResult().ymax) / 2);
//                                    extent.centerAt(pt);
//                                    mc.slideAnimation(pt.getX(), pt.getY());
//                                    mc.refreshSync(extent);
//                                    mc.flashFeature(task.getResult().layerID, task.getResult().objectID);
//                                }
//                                return null;
//                            }
//                        }, Task.UI_THREAD_EXECUTOR);
                    }
                });
                DialogUtils.showFullScreenDialog(context, 7683, true, view, new Supplier() {
                    @Override
                    public Object get() {
                        if (mp != null) {
                            mp.stop();
                            mp.release();
                        }
                        handler.removeCallbacks(runnable_play);
                        return null;
                    }
                });
            } else {
                BluToast.makeText(context, "该文件不存在！", BluToast.LENGTH_SHORT).show();
            }
        }
    }
}
