package cn.creable.surveyOnUCMap;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import cn.creable.ucmap.openGIS.UCFeatureLayer;

import com.annimon.stream.function.Supplier;
import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Map;

import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;


/**
 * Created by BluceLee on 2016-11-29.
 * <p>
 * 添加要素和编辑要素界面
 */

public class ModifyFeatureInfo {
    public static void show(final Context context, final Feature feature, final String[] fields, String[] vs,final UCFeatureLayer layer) {
        GVS.getInstance().voices.clear();
        GVS.getInstance().videos.clear();
        final Map<String, String> map = new HashMap<>();
        View view = View.inflate(context, R.layout.modifyfeatureinfo, null);
        LinearLayout layout = (LinearLayout) view.findViewById(R.id.ll_modifyfeatureinfo_contents);
        for (int i = 0; i < fields.length; i++) {
            final String key = fields[i];
            if ("photo".equals(key)) {
                View v = View.inflate(context, R.layout.modifyfeatureinfo_photo, null);
                Button btn = (Button) v.findViewById(R.id.btn_modifyfeatureinfo_photos);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        GVS.getInstance().vp = GVS.getInstance().photoPath + System.currentTimeMillis() + ".jpg";
                        File file = new File(GVS.getInstance().vp);
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                        Activity act = (Activity) context;
                        act.startActivityForResult(intent, 1);
                    }
                });
                HorizontalListView hlv = (HorizontalListView) v.findViewById(R.id.hlv_modifyfeatureinfo_photo);
                GVS.getInstance().pa = new PhotoAdapter(context);
                if (vs != null && vs[i] != null && !"".equals(vs[i])) {
                    GVS.getInstance().pa.setPhotos(vs[i].split(";"));
                }
                hlv.setAdapter(GVS.getInstance().pa);
                hlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        final String path = (String) parent.getItemAtPosition(position);
                        final File f = new File(GVS.getInstance().photoPath + path);
                        if (f.exists()) {
                            ShowPhoto.show(context, GVS.getInstance().photoPath + path, true, new Supplier() {
                                @Override
                                public Object get() {
                                    f.delete();
                                    GVS.getInstance().pa.removePhoto(path);
                                    return null;
                                }
                            });
                        } else {
                            BluToast.makeText(context, "该图片不存在！", BluToast.LENGTH_SHORT).show();
                        }
                    }
                });
                layout.addView(v);
            } else if ("video".equals(key)) {
                View v = View.inflate(context, R.layout.modifyfeatureinfo_video, null);
                Button btn = (Button) v.findViewById(R.id.btn_modifyfeatureinfo_videos);
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        GVS.getInstance().vp = GVS.getInstance().videoPath + System.currentTimeMillis() + ".mp4";
                        File file = new File(GVS.getInstance().vp);
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        Intent intent = new Intent();
                        intent.setAction("android.media.action.VIDEO_CAPTURE");
                        intent.addCategory("android.intent.category.DEFAULT");
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, Uri.fromFile(file));
                        Activity act = (Activity) context;
                        act.startActivityForResult(intent, 3);
                    }
                });
                final TextView tv = (TextView) v.findViewById(R.id.tv_modifyfeatureinfo_videos);
                if (vs != null && vs[i] != null && !"".equals(vs[i])) {
                    String[] strs = vs[i].split(";");
                    for (String s : strs) {
                        GVS.getInstance().videos.add(s);
                    }
                    tv.setText(strs[0]);
                }
                GVS.getInstance().tv_video = tv;
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GVS.getInstance().videos.size() > 0) {
                            final String[] videos = new String[GVS.getInstance().videos.size()];
                            for (int i = 0; i < GVS.getInstance().videos.size(); i++) {
                                videos[i] = GVS.getInstance().videos.get(i);
                            }
                            final NormalListDialog dialog = new NormalListDialog(context, videos);
                            dialog.title("请选择视频文件").titleTextSize_SP(18).titleBgColor(Color.parseColor("#6495ED"))
                                    .itemPressColor(Color.parseColor("#85D3EF")).itemTextColor(Color.parseColor("#303030"))
                                    .itemTextSize(14).cornerRadius(0).widthScale(0.5f).vMargin(85).show();
                            dialog.setOnOperItemClickL(new OnOperItemClickL() {
                                @Override
                                public void onOperItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                    dialog.dismiss();
                                    tv.setText(videos[position]);
                                    if (new File(GVS.getInstance().videoPath + videos[position]).exists()) {
                                        PlayVideo.play(context, GVS.getInstance().videoPath + videos[position], true, new Supplier() {
                                            @Override
                                            public Object get() {
                                                GVS.getInstance().videos.remove(videos[position]);
                                                if (GVS.getInstance().videos.size() > 0) {
                                                    tv.setText(GVS.getInstance().videos.get(0));
                                                } else {
                                                    tv.setText("");
                                                }
                                                return null;
                                            }
                                        });
                                    } else {
                                        BluToast.makeText(context, "该视频不存在！", BluToast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
                layout.addView(v);
            } else if ("voice".equals(key)) {
                View v = View.inflate(context, R.layout.modifyfeatureinfo_voice, null);
                Button btn = (Button) v.findViewById(R.id.btn_modifyfeatureinfo_voices);
                final TextView tv = (TextView) v.findViewById(R.id.tv_modifyfeatureinfo_voices);
                if (vs != null && vs[i] != null && !"".equals(vs[i])) {
                    String[] strs = vs[i].split(";");
                    for (String s : strs) {
                        GVS.getInstance().voices.add(s);
                    }
                    tv.setText(strs[0]);
                }
                tv.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (GVS.getInstance().voices.size() > 0) {
                            final String[] voices = new String[GVS.getInstance().voices.size()];
                            for (int i = 0; i < GVS.getInstance().voices.size(); i++) {
                                voices[i] = GVS.getInstance().voices.get(i);
                            }
                            final NormalListDialog dialog = new NormalListDialog(context, voices);
                            dialog.title("请选择录音文件").titleTextSize_SP(18).titleBgColor(Color.parseColor("#6495ED"))
                                    .itemPressColor(Color.parseColor("#85D3EF")).itemTextColor(Color.parseColor("#303030"))
                                    .itemTextSize(14).cornerRadius(0).widthScale(0.5f).vMargin(85).show();
                            dialog.setOnOperItemClickL(new OnOperItemClickL() {
                                @Override
                                public void onOperItemClick(AdapterView<?> parent, View view, final int position, long id) {
                                    dialog.dismiss();
                                    tv.setText(voices[position]);
                                    if (new File(GVS.getInstance().voicesPath + voices[position]).exists()) {
                                        new PlayVoice().play(context, GVS.getInstance().voicesPath + voices[position], true, new Supplier() {
                                            @Override
                                            public Object get() {
                                                GVS.getInstance().voices.remove(voices[position]);
                                                if (GVS.getInstance().voices.size() == 0) {
                                                    tv.setText("");
                                                } else {
                                                    tv.setText(new File(GVS.getInstance().voices.get(0)).getName());
                                                }
                                                return null;
                                            }
                                        });
                                    } else {
                                        BluToast.makeText(context, "该录音文件不存在！", BluToast.LENGTH_SHORT).show();
                                    }
                                }
                            });
                        }
                    }
                });
                GVS.getInstance().tv_voice = tv;
                btn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        String path = GVS.getInstance().voicesPath + System.currentTimeMillis() + ".mp3";
                        final File file = new File(path);
                        if (!file.getParentFile().exists()) {
                            file.getParentFile().mkdirs();
                        }
                        new BluRecorder().take(context, path, new Supplier() {
                            @Override
                            public Object get() {
                                GVS.getInstance().voices.add(file.getName());
                                GVS.getInstance().tv_voice.setText(file.getName());
                                return null;
                            }
                        });
                    }
                });
                layout.addView(v);
            } else {
                if (vs != null && vs[i] != null && !"".equals(vs)) {
                    map.put(fields[i], vs[i]);
                }
                View v = View.inflate(context, R.layout.modifyfeatureinfo_edit, null);
                TextView tv = (TextView) v.findViewById(R.id.tv_modifyfeatureinfo_edit);
                tv.setText(key + ":");
                final EditText et = (EditText) v.findViewById(R.id.et_modifyfeatureinfo_edit);
                if (vs != null) {
                    et.setText(vs[i]);
                }
                et.addTextChangedListener(new TextWatcher() {
                    @Override
                    public void beforeTextChanged(CharSequence s, int start, int count, int after) {

                    }

                    @Override
                    public void onTextChanged(CharSequence s, int start, int before, int count) {
                        map.put(key, s.toString());
                    }

                    @Override
                    public void afterTextChanged(Editable s) {

                    }
                });
                layout.addView(v);
            }
        }
        Button btn_cancel = (Button) view.findViewById(R.id.btn_modifyfeatureinfo_cancel);
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //editTool.cancel();
                DialogUtils.finishDialog(110);
            }
        });
        Button btn_ok = (Button) view.findViewById(R.id.btn_modifyfeatureinfo_ok);
        btn_ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String[] values = new String[fields.length];
                for (int i = 0; i < fields.length; i++) {
                    if ("photo".equals(fields[i])) {
                        String sv = "";
                        for (String s : GVS.getInstance().pa.getPhotos()) {
                            sv += s + ";";
                        }
                        values[i] = sv;
                    } else if ("video".equals(fields[i])) {
                        String sv = "";
                        for (String s : GVS.getInstance().videos) {
                            sv += s + ";";
                        }
                        values[i] = sv;
                    } else if ("voice".equals(fields[i])) {
                        String sv = "";
                        for (String s : GVS.getInstance().voices) {
                            sv += s + ";";
                        }
                        values[i] = sv;
                    } else {
                        if (map.containsKey(fields[i])) {
                            values[i] = map.get(fields[i]);
                        } else {
                            values[i] = "";
                        }
                    }
                }
                Hashtable<String,Object> newFeature=new Hashtable<String,Object>();
				newFeature.put("geometry", feature.geometry());
				try {
					for (int i=0;i<fields.length;++i)
					{
						Field f=layer.getField(fields[i]);
						if (f.type()==Byte.class)
							newFeature.put(fields[i], Byte.parseByte(values[i]));
						else if (f.type()==Short.class)
							newFeature.put(fields[i], Short.parseShort(values[i]));
						else if (f.type()==Integer.class)
							newFeature.put(fields[i], Integer.parseInt(values[i]));
						else if (f.type()==Long.class)
							newFeature.put(fields[i], Long.parseLong(values[i]));
						else if (f.type()==Float.class)
							newFeature.put(fields[i], Float.parseFloat(values[i]));
						else if (f.type()==Double.class)
							newFeature.put(fields[i], Double.parseDouble(values[i]));
						else if (f.type()==java.sql.Date.class)
						{
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							newFeature.put(fields[i], format.parse(values[i]));
						}
						else if (f.type()==java.sql.Time.class)
						{
							SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
							newFeature.put(fields[i], new java.sql.Time(format.parse(values[i]).getTime()));
						}
						else if (f.type()==String.class)
							newFeature.put(fields[i], values[i]);
					}
					Hashtable<String,Object> values1=new Hashtable<String,Object>();
                	for (Field f:feature.schema())
                		if (feature.get(f.name())!=null)
                			values1.put(f.name(), feature.get(f.name()));
					Feature ft=layer.updateFeature(feature, newFeature);
					UndoRedo.getInstance().addUndo(EditOperation.UpdateFeature, layer, new BasicFeature(null,values1), ft);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
//                ft.setValues(values);
//                editTool.confirm();
                DialogUtils.finishDialog(110);
            }
        });
        DialogUtils.showFullScreenDialog(context, 110, true, view, 250, 85, 250, 40, new Supplier() {
            @Override
            public Object get() {
                //editTool.cancel();
                return null;
            }
        });
    }
}
