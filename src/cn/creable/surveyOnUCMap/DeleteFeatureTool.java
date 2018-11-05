package cn.creable.surveyOnUCMap;

import java.util.Vector;

import org.jeo.vector.Feature;

import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCMapView;

public class DeleteFeatureTool implements UCFeatureLayerListener,IMapTool{
	
	private UCMapView mMapView;
	private Vector<UCFeatureLayer> layers;
	
	public DeleteFeatureTool(UCMapView mapView,Vector<UCFeatureLayer> layers)
	{
		mMapView=mapView;
		this.layers=layers;
		for (UCFeatureLayer layer:layers)
			layer.setListener(this);
	}

	@Override
	public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature, double distance) {
		if (distance>30) return false;
		layer.deleteFeature(feature);
		UndoRedo.getInstance().addUndo(EditOperation.DeleteFeature, layer, feature, null);
		mMapView.refresh();
		return false;
	}

	@Override
	public boolean onItemLongPress(UCFeatureLayer layer, Feature feature, double distance) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void stop() {
		mMapView=null;
		for (UCFeatureLayer layer:layers)
			layer.setListener(null);
		layers=null;
	}

}
