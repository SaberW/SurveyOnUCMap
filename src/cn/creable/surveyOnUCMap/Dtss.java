package cn.creable.surveyOnUCMap;

import android.app.Fragment;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.annimon.stream.Stream;
import com.annimon.stream.function.Consumer;
import com.kennyc.view.MultiStateView;
import com.vividsolutions.jts.geom.Envelope;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.concurrent.Callable;

import org.jeo.data.Cursor;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;

import bolts.Continuation;
import bolts.Task;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCLayer;

/**
 * Created by blucelee on 2016/11/26.
 * 地图搜索界面
 */

public class Dtss extends Fragment {
    private MainActivity activity;
    private MultiStateView msv;
    private ListView lv;
    private TextView tv_empty;
    private String layerName = "";
    
    private UCLayer[] layers;

    UCFeatureLayer getLayer(String layerName)
    {
        for (UCLayer layer:layers)
        {
            if (layer.getName().equals(layerName) && layer instanceof UCFeatureLayer)
                return (UCFeatureLayer) layer;
        }
        return null;
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        activity = (MainActivity) getActivity();
        View view = inflater.inflate(R.layout.dtss, container, false);
        ImageView iv = (ImageView) view.findViewById(R.id.iv_dtss_close);
        iv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.closeMenu();
            }
        });
        Drawable drawable = getResources().getDrawable(R.drawable.combobox);
        drawable.setBounds(0, 0, 28, 16);
        final List<String> layernames = new ArrayList<>();
        
        int size=activity.layers.size();
        layers=new UCLayer[size];
        for (int i=0;i<size;++i)
        {
        	layers[i]=activity.layers.get(i).layer;
        }
        
        Stream.of(layers).forEach(new Consumer<UCLayer>() {
            @Override
            public void accept(UCLayer iLayer) {
//                if (iLayer instanceof IFeatureLayer) {
                    layernames.add(iLayer.getName());
//                }
            }
        });
        final TextView tv_layername = (TextView) view.findViewById(R.id.tv_dtss_layername);
        tv_layername.setCompoundDrawables(null, null, drawable, null);
        tv_layername.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectString.select(activity, "请选择要搜索的图层", layernames, new Consumer<String>() {
                    @Override
                    public void accept(String s) {
                        tv_layername.setText(s);
                    }
                });
            }
        });
        final TextView tv_searchcolumn = (TextView) view.findViewById(R.id.tv_dtss_searchcolumnname);
        tv_searchcolumn.setCompoundDrawables(null, null, drawable, null);
        tv_searchcolumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String layername = tv_layername.getText().toString().trim();
                if ("".equals(layername) || "请选择要搜索的图层".equals(layername)) {
                    BluToast.makeText(activity, "请先选择要搜索的图层！", BluToast.LENGTH_SHORT).show();
                } else {
                    SelectString.select(activity, "请选择要搜索的字段", DBUtils.getLayerColumns(getLayer(layername)),
                            new Consumer<String>() {
                                @Override
                                public void accept(String s) {
                                    tv_searchcolumn.setText(s);
                                }
                            });
                }
            }
        });
        final TextView tv_showcolumn = (TextView) view.findViewById(R.id.tv_dtss_showcolumnname);
        tv_showcolumn.setCompoundDrawables(null, null, drawable, null);
        tv_showcolumn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String layername = tv_layername.getText().toString().trim();
                if ("".equals(layername) || "请选择要搜索的图层".equals(layername)) {
                    BluToast.makeText(activity, "请先选择要搜索的图层！", BluToast.LENGTH_SHORT).show();
                } else {
                    SelectString.select(activity, "请选择要显示的字段", DBUtils.getLayerColumns(getLayer(layername)),
                            new Consumer<String>() {
                                @Override
                                public void accept(String s) {
                                    tv_showcolumn.setText(s);
                                }
                            });
                }
            }
        });
        final EditText et = (EditText) view.findViewById(R.id.et_dtss);
        msv = (MultiStateView) view.findViewById(R.id.msv_dtss);//这是一个多状态控件，可以在空数据、正在加载、内容、错误四个界面中来回切换
        tv_empty = (TextView) msv.getView(MultiStateView.VIEW_STATE_EMPTY).findViewById(R.id.tv_empty_view);
        tv_empty.setText("");
        lv = (ListView) msv.getView(MultiStateView.VIEW_STATE_CONTENT).findViewById(R.id.lv_dtss);
        TextView tv_ss = (TextView) view.findViewById(R.id.tv_dtss_ss);
        tv_ss.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String layername = tv_layername.getText().toString().trim();
                if ("".equals(layername) || "请选择要搜索的图层".equals(layername)) {
                    BluToast.makeText(activity, "请先选择要搜索的图层！", BluToast.LENGTH_SHORT).show();
                } else {
                    String searchColumnName = tv_searchcolumn.getText().toString().trim();
                    if ("".equals(searchColumnName) || "请选择要搜索的字段".equals(searchColumnName)) {
                        BluToast.makeText(activity, "请先选择要搜索的字段！", BluToast.LENGTH_SHORT).show();
                    } else {
                        int index = 0;
                        String showColumnName = tv_showcolumn.getText().toString().trim();
                        if ("".equals(showColumnName) || "请选择要显示的字段".equals(showColumnName)) {
                            showColumnName=searchColumnName;
                        } else {
                        }
                        msv.setViewState(MultiStateView.VIEW_STATE_LOADING);
                        layerName = layername;
                        search(getLayer(layername),searchColumnName, et.getText().toString().trim(),showColumnName);
                    }
                }
            }
        });
        TextView tv_clear = (TextView) view.findViewById(R.id.tv_dtss_clear);
        tv_clear.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                msv.setViewState(MultiStateView.VIEW_STATE_EMPTY);
            }
        });
        lv.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                //activity.closeMenu();
                Feature f = (Feature) adapterView.getItemAtPosition(i);
                Vector<Feature> features=new Vector<Feature>();
        		features.add(f);
                activity.mView.getMaskLayer().setData(features, 10, 2, "#EEFF0000", "#88FF0000");
        		activity.mView.refresh(1000,f.geometry().getEnvelopeInternal());
            }
        });
        return view;
    }

    //查询
    private void search(final UCFeatureLayer layer,final String field, final String key,final String displayField) {
        //后台查询结束后刷新UI
        Task.callInBackground(new Callable<Cursor<Feature>>() {
            @Override
            public Cursor<Feature> call() throws Exception {
                String where = String.format("%s like '%%%s%%'", field, key);
                return layer.searchFeature(where, 1000, 0, 0, 0, 0, 0);
            }
        }).continueWith(new Continuation<Cursor<Feature>, Object>() {
            @Override
            public Object then(Task<Cursor<Feature>> task) throws Exception {
                if (task.isFaulted() || task.getResult() == null || task.getResult().hasNext()==false) {
                    tv_empty.setText("没有符合条件的数据，请重新查询！");
                    msv.setViewState(MultiStateView.VIEW_STATE_EMPTY);//如果搜索数据为空则切换到空数据界面
                } else {
                	Vector<Feature> features=new Vector<Feature>();
					Cursor<Feature> cursor=task.getResult();
					while (cursor.hasNext()) {
						Feature ft = cursor.next();
						features.add(ft);
					}
					cursor.close();
                    lv.setAdapter(new FeatureInfoAdapter(activity, features,layer.getField(displayField)));
                    msv.setViewState(MultiStateView.VIEW_STATE_CONTENT);//显示搜索结果
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }
}

