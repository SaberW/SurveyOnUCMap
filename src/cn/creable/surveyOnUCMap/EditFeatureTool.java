package cn.creable.surveyOnUCMap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.LinearRing;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import cn.creable.ucmap.openGIS.GeometryType;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCMapView;

public class EditFeatureTool implements OnGestureListener,UCFeatureLayerListener,IMapTool{
	
	private UCMapView mMapView;
	private Vector<UCFeatureLayer> layers;
	private UCFeatureLayer curLayer;
	private Feature feature,oldFeature;
	private Vector<Feature> controlPoints=new Vector<Feature>();
	private int[] innerRings;
	private int current;
	
	private int controlPointSize=30;
	
	private GeometryFactory gf=new GeometryFactory();
	
	public EditFeatureTool(UCMapView mapView,Vector<UCFeatureLayer> layers)
	{
		mMapView=mapView;
		this.layers=layers;
		for (UCFeatureLayer layer:layers)
			layer.setListener(this);
		mMapView.setListener(this, null);
	}

	@Override
	public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature, double distance) {
		if (distance>30) return false;
		mMapView.getMaskLayer().clear();
		this.feature=feature;
		this.curLayer=layer;
		Hashtable<String,Object> value=new Hashtable<String,Object>();
    	for (Field f:feature.schema())
    		if (feature.get(f.name())!=null)
    			value.put(f.name(), feature.get(f.name()));
		this.oldFeature=new BasicFeature(null,value);
		controlPoints.clear();
		switch (feature.geometry().getGeometryType())
		{
		case GeometryType.Point:
			Point pt=(Point) feature.geometry();
			ArrayList<Object> values=new ArrayList<Object>();
			values.add(gf.createPoint(new Coordinate(pt.getX(),pt.getY())));
			controlPoints.add(new BasicFeature(null,values));
			break;
		case GeometryType.LineString:
			LineString line=(LineString)feature.geometry();
			for (int i=0;i<line.getNumPoints();++i)
			{
				ArrayList<Object> values1=new ArrayList<Object>();
				values1.add(gf.createPoint(new Coordinate(line.getCoordinateN(i).x,line.getCoordinateN(i).y)));
				controlPoints.add(new BasicFeature(null,values1));
			}
			break;
		case GeometryType.Polygon:
			Polygon pg=(Polygon)feature.geometry();
			LineString ring=pg.getExteriorRing();
			int size=ring.getNumPoints()-1;
			for (int i=0;i<size;++i)
			{
				ArrayList<Object> values1=new ArrayList<Object>();
				values1.add(gf.createPoint(new Coordinate(ring.getCoordinateN(i).x,ring.getCoordinateN(i).y)));
				controlPoints.add(new BasicFeature(null,values1));
			}
			innerRings=null;
			if (pg.getNumInteriorRing()>0)
			{//这里处理内部ring
				innerRings=new int[pg.getNumInteriorRing()];
				for (int j=0;j<pg.getNumInteriorRing();++j)
				{
					ring=pg.getInteriorRingN(j);
					size=ring.getNumPoints()-1;
					innerRings[j]=size;
					for (int i=0;i<size;++i)
					{
						ArrayList<Object> values1=new ArrayList<Object>();
						values1.add(gf.createPoint(new Coordinate(ring.getCoordinateN(i).x,ring.getCoordinateN(i).y)));
						controlPoints.add(new BasicFeature(null,values1));
					}
				}
			}
			break;
		default:
			return false;
		}
		mMapView.getMaskLayer().setData(controlPoints, controlPointSize, 2, "#88FF0000", "#88FF0000");
		mMapView.refresh();
		mMapView.move(false);
		System.out.println("itemTapUp");
		return true;
	}

	@Override
	public boolean onItemLongPress(UCFeatureLayer layer, Feature feature, double distance) {
		System.out.println("onItemLongPress");
		return false;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		System.out.println(String.format("onDown e=(%f,%f)",e.getX(),e.getY()));
		if (current!=-1)
			UndoRedo.getInstance().addUndo(EditOperation.UpdateFeature, curLayer, oldFeature, feature);
		current=-1;
		for (int i=0;i<controlPoints.size();++i)
		{
			Point pt=(Point)controlPoints.get(i).geometry();
			Point screenPt=mMapView.fromMapPoint(pt.getX(), pt.getY());
			if (Math.abs(screenPt.getX()-e.getX())<(controlPointSize) &&
				Math.abs(screenPt.getY()-e.getY())<(controlPointSize))
			{
				current=i;
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onFling(MotionEvent e1, MotionEvent e2, float x, float y) {
		System.out.println("onFling");
		return false;
	}

	@Override
	public void onLongPress(MotionEvent e) {
		System.out.println("onLongPress");
	}

	@Override
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float x, float y) {
		//System.out.println(String.format("onScroll e1=(%f,%f) e2=(%f,%f) x=%f y=%f", e1.getX(),e1.getY(),e2.getX(),e2.getY(),x,y));
		if (current>=0)
		{
			Point pt=mMapView.toMapPoint(e2.getX(), e2.getY());
			controlPoints.get(current).put("geometry", gf.createPoint(new Coordinate(pt.getX(),pt.getY())));
			switch (feature.geometry().getGeometryType())
			{
			case GeometryType.Point:
				Hashtable<String,Object> values=new Hashtable<String,Object>();
				for (Field f:feature.schema())
				{
					Object obj=feature.get(f.name());
					if (obj!=null) values.put(f.name(), obj);
				}
				values.put("geometry",gf.createPoint(new Coordinate(pt.getX(),pt.getY())));
				feature=curLayer.updateFeature(feature, values);
				break;
			case GeometryType.LineString:
				Hashtable<String,Object> values1=new Hashtable<String,Object>();
				for (Field f:feature.schema())
				{
					Object obj=feature.get(f.name());
					if (obj!=null) values1.put(f.name(), obj);
				}
				Coordinate[] coords=new Coordinate[controlPoints.size()];
				for (int i=0;i<controlPoints.size();++i)
				{
					Point pt1=(Point)controlPoints.get(i).geometry();
					coords[i]=new Coordinate(pt1.getX(),pt1.getY());
				}
				values1.put("geometry",gf.createLineString(coords));
				feature=curLayer.updateFeature(feature, values1);
				break;
			case GeometryType.Polygon:
				Hashtable<String,Object> values11=new Hashtable<String,Object>();
				for (Field f:feature.schema())
				{
					Object obj=feature.get(f.name());
					if (obj!=null) values11.put(f.name(), obj);
				}
				if (innerRings==null)
				{
					Coordinate[] coords1=new Coordinate[controlPoints.size()+1];
					for (int i=0;i<controlPoints.size();++i)
					{
						Point pt1=(Point)controlPoints.get(i).geometry();
						coords1[i]=new Coordinate(pt1.getX(),pt1.getY());
					}
					coords1[controlPoints.size()]=coords1[0];//闭合
					values11.put("geometry",gf.createPolygon(gf.createLinearRing(coords1)));
				}
				else
				{
					int total=0;
					for (int i=0;i<innerRings.length;++i)
						total+=innerRings[i];
					int size=controlPoints.size()-total;
					Coordinate[] coords1=new Coordinate[size+1];
					int i;
					for (i=0;i<size;++i)
					{
						Point pt1=(Point)controlPoints.get(i).geometry();
						coords1[i]=new Coordinate(pt1.getX(),pt1.getY());
					}
					coords1[size]=coords1[0];//闭合
					LinearRing extRing=gf.createLinearRing(coords1);
					LinearRing inRing[]=new LinearRing[innerRings.length];
					for (int j=0;j<innerRings.length;++j)
					{
						coords1=new Coordinate[innerRings[j]+1];
						int pos=i+innerRings[j];
						int pointer=0;
						for (;i<pos;++i)
						{
							Point pt1=(Point)controlPoints.get(i).geometry();
							coords1[pointer++]=new Coordinate(pt1.getX(),pt1.getY());
						}
						coords1[pointer]=coords1[0];//闭合
						inRing[j]=gf.createLinearRing(coords1);
					}
					values11.put("geometry", gf.createPolygon(extRing, inRing));
				}
				
				feature=curLayer.updateFeature(feature, values11);
				break;
			}
			mMapView.refresh();
		}
		return false;
	}

	@Override
	public void onShowPress(MotionEvent e) {
		System.out.println("onShowPress");
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		
		System.out.println("onSingleTapUp");
		mMapView.move(true);
		mMapView.getMaskLayer().clear();
		controlPoints.clear();
		current=-1;
		mMapView.refresh();
		return false;
	}

	@Override
	public void stop() {
		if (current!=-1)
			UndoRedo.getInstance().addUndo(EditOperation.UpdateFeature, curLayer, oldFeature, feature);
		for (UCFeatureLayer layer:layers)
			layer.setListener(null);
		mMapView.setListener(null, null);
		layers=null;
		mMapView.move(true);
		mMapView.getMaskLayer().clear();
		controlPoints.clear();
		current=-1;
	}

}
