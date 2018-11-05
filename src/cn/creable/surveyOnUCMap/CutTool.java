package cn.creable.surveyOnUCMap;

import java.util.Hashtable;
import java.util.Random;
import java.util.Vector;

import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCStyle;
import cn.creable.ucmap.openGIS.UCVectorLayer;

public class CutTool implements OnGestureListener,UCFeatureLayerListener,IMapTool{
	
	private UCMapView mMapView;
	private UCFeatureLayer layer;
	private Vector<Coordinate> coords=new Vector<Coordinate>();
	private Feature feature;
	private UCVectorLayer vlayer;
	private Geometry geo;
	
	public CutTool(UCMapView mapView,UCFeatureLayer layer)
	{
		mMapView=mapView;
		this.layer=layer;
		layer.setListener(this);
		mMapView.setListener(this, null);
		if (vlayer==null) vlayer=mMapView.addVectorLayer();
//		GeometryFactory gf=new GeometryFactory();
//		Coordinate[] cs=new Coordinate[5];
//		cs[0]=new Coordinate(114.175354,22.995077);
//		cs[1]=new Coordinate(114.186553,23.008408);
//		cs[2]=new Coordinate(113.834856231916,22.840168389611573);
//		cs[3]=new Coordinate(113.834856231916,23.18531263089262);
//		cs[4]=new Coordinate(114.175354,22.995077);
//		vlayer.addPolygon(gf.createPolygon(cs), 5, 0xFF991111,0xFF222299,0.5f);
		
//		Coordinate[] cs=new Coordinate[2];
//		cs[0]=new Coordinate(114.175354,22.995077);
//		cs[1]=new Coordinate(113.834856231916,23.18531263089262);
//		LineString line1=gf.createLineString(cs);
//		vlayer.addLine(line1, 3, 0xFF991111);
//		cs=new Coordinate[2];
//		cs[0]=new Coordinate(114.186553,23.008408);
//		cs[1]=new Coordinate(113.834856231916,22.840168389611573);
//		LineString line2=gf.createLineString(cs);
//		boolean flag=line1.intersects(line2);
//		vlayer.addLine(line2, 3, 0xFF991111);
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		cut();
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	Feature feature(String id,com.vividsolutions.jts.geom.Geometry geo)
	{
		Hashtable<String,Object> values=new Hashtable<String,Object>();
		values.put("uid", id);
		values.put("geometry", geo);
		Feature current=new BasicFeature(id,values);
		return current;
	}
	
	private Random rnd=new Random(System.currentTimeMillis());
	
	String randomColor()
	{
		return String.format("#88%02X%02X%02X", rnd.nextInt(256),rnd.nextInt(256),rnd.nextInt(256));
	}
	
	private boolean cut()
	{
		if (coords.size()<=0 || feature==null) return false;
		GeometryFactory gf=new GeometryFactory();
		Coordinate[] cs=new Coordinate[coords.size()];
		coords.copyInto(cs);
//		Coordinate[] cs=new Coordinate[2];
//		cs[1]=new Coordinate(114.175354,22.995077);
//		cs[0]=new Coordinate(114.186553,23.008408);
		LineString line=gf.createLineString(cs);
		Geometry[] result=null;
		try {
			result=Utils.cut(feature.geometry(), line);
		}
		catch(Exception ex)
		{
			System.out.println(line);
			System.out.println(feature.geometry());
			coords.clear();
			vlayer.remove(geo);
			mMapView.getMaskLayer().clear();
			mMapView.refresh();
			feature=null;
			mMapView.move(true);
			ex.printStackTrace();
		}
		if (result==null)
		{
			coords.clear();
			vlayer.remove(geo);
			mMapView.getMaskLayer().clear();
			mMapView.refresh();
			feature=null;
			mMapView.move(true);
			return false;
		}
//		Vector<Feature> features=new Vector<Feature>();
//		Vector<UCStyle> styles=new Vector<UCStyle>();
//		for (int i=0;i<result.length;i++)
//		{
//			features.add(feature(String.format("%d", i),result[i]));
//			styles.add(new UCStyle("uid='"+i+"'",30,0,"#880000FF", randomColor()));
//		}
//		mMapView.getMaskLayer().setData(features);
//		mMapView.getMaskLayer().setStyles(styles);
		UndoRedo.getInstance().beginAddUndo();
		for (Geometry geo:result)
		{
			Hashtable<String,Object> values=new Hashtable<String,Object>();
			String name;
        	for (Field f:feature.schema())
        	{
        		name=f.name();
        		if (name.equals("geometry"))
        			values.put(name, geo);
        		else if (feature.get(name)!=null)
        			values.put(name, feature.get(name));
        	}
			Feature ft=layer.addFeature(values);
			UndoRedo.getInstance().addUndo(EditOperation.AddFeature, layer, null, ft);
		}
		
		layer.deleteFeature(feature);
		UndoRedo.getInstance().addUndo(EditOperation.DeleteFeature, layer, feature, null);
		UndoRedo.getInstance().endAddUndo();
		vlayer.remove(geo);
		mMapView.getMaskLayer().clear();
		mMapView.refresh();
		feature=null;
		mMapView.move(true);
		return true;
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float x, float y) {
		if (feature==null) return false;
		if (Math.abs(x)<2 && Math.abs(y)<2 && coords.size()>1)
		{
			cut();
		}
		else if (Math.abs(x)>1 || Math.abs(y)>1)
		{
			Point pt=mMapView.toMapPoint(e2.getX(), e2.getY());
			if (coords.size()>0)
			{
				if (Math.abs(pt.getX()-coords.lastElement().x)<0.0000001 && Math.abs(pt.getY()-coords.lastElement().y)<0.0000001)
					return false;
			}
			coords.add(new Coordinate(pt.getX(),pt.getY()));
			if (coords.size()==2)
			{
				GeometryFactory gf=new GeometryFactory();
				Coordinate[] cs=new Coordinate[coords.size()];
				coords.copyInto(cs);
				geo=gf.createLineString(cs);
				vlayer.addLine(geo, 3, 0xFF991111);
			}
			else if (coords.size()>2)
			{
				GeometryFactory gf=new GeometryFactory();
				Coordinate[] cs=new Coordinate[coords.size()];
				coords.copyInto(cs);
				Geometry newgeo=gf.createLineString(cs);
				vlayer.updateGeometry(geo, newgeo);
				geo=newgeo;
			}
			mMapView.refresh();
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public boolean onSingleTapUp(MotionEvent arg0) {
		mMapView.getMaskLayer().clear();
		mMapView.move(true);
		return false;
	}

	@Override
	public boolean onItemLongPress(UCFeatureLayer arg0, Feature arg1, double arg2) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature, double distance) {
		if (distance>30) return false;
		mMapView.getMaskLayer().clear();
		Vector<Feature> features=new Vector<Feature>();
		features.add(feature);
		this.feature=feature;
		mMapView.getMaskLayer().setData(features, 10, 2, "#EEFF0000", "#88FF0000");
		mMapView.refresh();
		mMapView.move(false);
		coords.clear();
		return true;
	}

	@Override
	public void stop() {
		mMapView.deleteLayer(vlayer);
		layer.setListener(null);
		mMapView.setListener(null, null);
		layer=null;
		mMapView.move(true);
		mMapView.getMaskLayer().clear();
	}

}
