package cn.creable.surveyOnUCMap;

import java.io.File;

import com.flyco.dialog.listener.OnOperItemClickL;
import com.flyco.dialog.widget.NormalListDialog;

import android.app.Fragment;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

public class ShowAttributeFragment extends Fragment {


    private String[] fields;
    private String[] values;
    private Context context;


    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {

        GVS.getInstance().voices.clear();
        GVS.getInstance().videos.clear();

        final MainActivity activity = (MainActivity) getActivity();
        context = activity;
        this.fields = activity.fields;
        this.values = activity.values;
        View view = inflater.inflate(R.layout.showfeatureinfo, container, false);

        ImageView iv = (ImageView) view.findViewById(R.id.iv_showfeatureinfo_close);

        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.closeMenu();
            }
        });

		LinearLayout layout = (LinearLayout) view.findViewById(R.id.ll_showfeatureinfo_contents);
		for (int i = 0; i < fields.length; i++) {
			final String key = fields[i];
			if ("photo".equals(key)) {
				View v = View.inflate(context, R.layout.showfeatureinfo_photo, null);
				HorizontalListView hlv = (HorizontalListView) v.findViewById(R.id.hlv_showfeatureinfo_photo);
				GVS.getInstance().pa = new PhotoAdapter(context);
				if (values != null && values[i] != null && !"".equals(values[i])) {
					GVS.getInstance().pa.setPhotos(values[i].split(";"));
				}
				hlv.setAdapter(GVS.getInstance().pa);
				hlv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
					@Override
					public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
						final String path = (String) parent.getItemAtPosition(position);
						final File f = new File(GVS.getInstance().photoPath + path);
						if (f.exists()) {
							ShowPhoto.show(context, GVS.getInstance().photoPath + path, false, null);
						} else {
							BluToast.makeText(context, "该图片不存在！", BluToast.LENGTH_SHORT).show();
						}
					}
				});
				layout.addView(v);
			} else if ("video".equals(key)) {
				View v = View.inflate(context, R.layout.showfeatureinfo_video, null);
				final TextView tv = (TextView) v.findViewById(R.id.tv_showfeatureinfo_videos);
				if (values != null && values[i] != null && !"".equals(values[i])) {
					String[] strs = values[i].split(";");
					for (String s : strs) {
						GVS.getInstance().videos.add(s);
					}
					tv.setText(strs[0]);
				}
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
									.itemPressColor(Color.parseColor("#85D3EF"))
									.itemTextColor(Color.parseColor("#303030")).itemTextSize(14).cornerRadius(0)
									.widthScale(0.5f).vMargin(85).show();
							dialog.setOnOperItemClickL(new OnOperItemClickL() {
								@Override
								public void onOperItemClick(AdapterView<?> parent, View view, final int position,
										long id) {
									dialog.dismiss();
									tv.setText(videos[position]);
									if (new File(GVS.getInstance().videoPath + videos[position]).exists()) {
										PlayVideo.play(context, GVS.getInstance().videoPath + videos[position], false,
												null);
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
				View v = View.inflate(context, R.layout.showfeatureinfo_voice, null);
				final TextView tv = (TextView) v.findViewById(R.id.tv_showfeatureinfo_voices);
				if (values != null && values[i] != null && !"".equals(values[i])) {
					String[] strs = values[i].split(";");
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
									.itemPressColor(Color.parseColor("#85D3EF"))
									.itemTextColor(Color.parseColor("#303030")).itemTextSize(14).cornerRadius(0)
									.widthScale(0.5f).vMargin(85).show();
							dialog.setOnOperItemClickL(new OnOperItemClickL() {
								@Override
								public void onOperItemClick(AdapterView<?> parent, View view, final int position,
										long id) {
									dialog.dismiss();
									tv.setText(voices[position]);
									if (new File(GVS.getInstance().voicesPath + voices[position]).exists()) {
										new PlayVoice().play(context, GVS.getInstance().voicesPath + voices[position],
												false, null);
									} else {
										BluToast.makeText(context, "该录音文件不存在！", BluToast.LENGTH_SHORT).show();
									}
								}
							});
						}
					}
				});
				layout.addView(v);
			} else {
				View v = View.inflate(context, R.layout.showfeatureinfo_text, null);
				TextView tv = (TextView) v.findViewById(R.id.tv_showfeatureinfo_text);
				if (values != null && values[i] != null) {
					tv.setText(key + ":" + values[i]);
				} else {
					tv.setText(key + ":");
				}
				layout.addView(v);

			}

		}
        return view;
    }
}
