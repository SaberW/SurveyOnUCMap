package cn.creable.surveyOnUCMap;

import java.util.Vector;

import org.jeo.vector.Feature;

import android.os.Handler;
import android.os.Message;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCMapView;

public class EditFeatureAttTool implements UCFeatureLayerListener,IMapTool {
	
	private UCMapView mapView;
	private Handler handler;
	
	public Vector<UCFeatureLayer> layers;
	public UCFeatureLayer layer;
	public Feature feature;
	
	public EditFeatureAttTool(UCMapView mapView,Handler handler,Vector<UCFeatureLayer> layers)
	{
		this.mapView=mapView;
		this.handler=handler;
		this.layers=layers;
		for (UCFeatureLayer layer:layers)
			layer.setListener(this);
	}

	@Override
	public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature, double distance) {
		if (distance>30) return false;
		this.layer=layer;
		this.feature=feature;
		if (handler!=null)
		{
			Message msg=new Message();
			msg.what=1;
			msg.obj=this;
			handler.sendMessage(msg);
		}
		layer.setListener(this);
		return true;
	}

	@Override
	public boolean onItemLongPress(UCFeatureLayer layer, Feature feature, double distance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop() {
		mapView=null;
		for (UCFeatureLayer layer:layers)
			layer.setListener(null);
		layers=null;
		layer=null;
	}

}
