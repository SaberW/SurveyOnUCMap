package cn.creable.surveyOnUCMap;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Vector;

import org.gdal.ogr.ogr;
import org.jeo.data.Cursor;
import org.jeo.vector.Feature;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.MultiPolygon;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.view.GestureDetector.OnGestureListener;
import android.view.MotionEvent;
import cn.creable.ucmap.openGIS.Arithmetic;
import cn.creable.ucmap.openGIS.GeometryType;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCScreenLayer;
import cn.creable.ucmap.openGIS.UCScreenLayerListener;

public class AddFeatureTool2 implements OnGestureListener,IMapTool  {
	
	private UCMapView mapView;
	private UCFeatureLayer layer;
//	private UCScreenLayer slayer;
	private UCMarkerLayer mlayer;
	private Bitmap pointImage;
//	private Bitmap crossImage;
	
	private GeometryFactory gf=new GeometryFactory();
	private Vector<Coordinate> coords;
	private Feature feature;
	
	private UCFeatureLayer[] snapLayers;
	private Point snapResult;
	private double snapDistance;
	
	private LineString snapLine;
	private int snapIndex;
	private LineString snapLinePrev;
	private int snapIndexPrev;
	private boolean snapAutoMode;
	
	public AddFeatureTool2(UCMapView mapView,UCFeatureLayer layer,Bitmap pointImage)
	{
		this.mapView=mapView;
		this.layer=layer;
		this.pointImage=pointImage;
		mapView.setListener(this, null);
	}
	
	public void openSnap(UCFeatureLayer[] snapLayers,double snapDistance,boolean autoMode)
	{
		this.snapLayers=snapLayers;
		this.snapDistance=snapDistance;
		this.snapAutoMode=autoMode;
	}
	
	public void closeSnap()
	{
		this.snapLayers=null;
		this.snapDistance=0;
		this.snapAutoMode=false;
	}
	
	public double snap(UCFeatureLayer layer,Point point,double distance)
	{
		double z=0.01;
		Cursor<Feature> cursor=layer.searchFeature(null, 0, 0, point.getX()-z,point.getX()+z,point.getY()-z,point.getY()+z);
		try {
			double dis=distance;
			String type;
			snapIndexPrev=snapIndex;
			snapLinePrev=snapLine;
			for (Feature f : cursor)
			{
				type=f.geometry().getGeometryType();
	        	if (type==GeometryType.Point)
	        	{
	        		double cur=Arithmetic.Distance(point, (com.vividsolutions.jts.geom.Point)f.geometry());
	        		if (cur<dis)
	        		{
	        			dis=cur;
	        			snapResult=(Point)f.geometry();
	        		}
	        	}
	        	else if (type==GeometryType.LineString)
	        	{
	        		LineString line=(LineString)f.geometry();
	        		int number=line.getNumPoints();
	        		for (int j=0;j<number;j++) {
	        			double cur=Arithmetic.Distance(point, line.getPointN(j));
		        		if (cur<dis)
		        		{
		        			dis=cur;
		        			snapResult=line.getPointN(j);
		        			snapLine=line;
		        			snapIndex=j;
		        		}
	        		}
	        	}
	        	else if (type==GeometryType.Polygon)
	        	{
	        		Polygon pg=(Polygon)f.geometry();
	        		LineString line=pg.getExteriorRing();
	        		int number=line.getNumPoints();
	        		for (int j=0;j<number;j++) {
	        			double cur=Arithmetic.Distance(point, line.getPointN(j));
		        		if (cur<dis)
		        		{
		        			dis=cur;
		        			snapResult=line.getPointN(j);
		        			snapLine=line;
		        			snapIndex=j;
		        		}
	        		}
	        		int numberRing=pg.getNumInteriorRing();
	        		for (int n=0;n<numberRing;n++) {
	        			line=pg.getInteriorRingN(n);
		        		number=line.getNumPoints();
		        		for (int j=0;j<number;j++) {
		        			double cur=Arithmetic.Distance(point, line.getPointN(j));
			        		if (cur<dis)
			        		{
			        			dis=cur;
			        			snapResult=line.getPointN(j);
			        			snapLine=line;
			        			snapIndex=j;
			        		}
		        		}
	        		}
	        	}
			}
			cursor.close();
			return dis;
		}catch (Exception ex){
			ex.printStackTrace();
			return -1;
		}
	}
	
	public void start()
	{
		mlayer=mapView.addMarkerLayer(null);
		//String dir=Environment.getExternalStorageDirectory().getPath();
//		slayer=mapView.addScreenLayer(crossImage,0,0, new UCScreenLayerListener() {
//
//			@Override
//			public boolean onItemSingleTapUp(UCScreenLayer lyr) {
//				return false;
//			}
//
//			@Override
//			public boolean onItemLongPress(UCScreenLayer lyr) {
//				mlayer.removeAllItems();
//				coords.clear();
//				coords=null;
//				feature=null;
//				mapView.refresh();
//				return true;
//			}
//			
//		});
		
		mapView.refresh();
	}
	
	public void stop()
	{
//		if (slayer!=null) mapView.deleteLayer(slayer);
		if (mlayer!=null) mapView.deleteLayer(mlayer);
		mapView.setListener(null, null);
		mapView=null;
		mlayer=null;
		snapLayers=null;
	}

	@Override
	public boolean onDown(MotionEvent e) {
		
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		UndoRedo.getInstance().addUndo(EditOperation.AddFeature, layer, null, feature);
		mlayer.removeAllItems();
		coords.clear();
		coords=null;
		feature=null;
		mapView.refresh();
	}

	@Override
	public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onShowPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
	}
	
	private void onClick(Point pt)
	{
		if (layer.getGeometryType()==ogr.wkbPoint || layer.getGeometryType()==ogr.wkbMultiPoint)
		{
			Hashtable<String,Object> values=new Hashtable<String,Object>();
			values.put("geometry", pt);
			Feature ft=layer.addFeature(values);
			UndoRedo.getInstance().addUndo(EditOperation.AddFeature, layer, null, ft);
		}
		else if (layer.getGeometryType()==ogr.wkbLineString || layer.getGeometryType()==ogr.wkbMultiLineString)
		{
			mlayer.addBitmapItem(pointImage, pt.getX(),pt.getY(),"","");
			if (coords==null)
			{
				coords=new Vector<Coordinate>();
				coords.add(new Coordinate(pt.getX(),pt.getY()));
			}
			else
			{
				coords.add(new Coordinate(pt.getX(),pt.getY()));
				if (feature==null) {
					Coordinate[] cds=new Coordinate[coords.size()];
					coords.copyInto(cds);
					Hashtable<String,Object> values=new Hashtable<String,Object>();
					values.put("geometry",gf.createLineString(cds));
					feature=layer.addFeature(values);
				}
				else
				{
					Coordinate[] cds=new Coordinate[coords.size()];
					coords.copyInto(cds);
					Hashtable<String,Object> values=new Hashtable<String,Object>();
					values.put("geometry",gf.createLineString(cds));
					layer.updateFeature(feature, values);
				}
			}
		}
		else if (layer.getGeometryType()==ogr.wkbPolygon || layer.getGeometryType()==ogr.wkbMultiPolygon)
		{
			mlayer.addBitmapItem(pointImage, pt.getX(),pt.getY(),"","");
			if (coords==null)
				coords=new Vector<Coordinate>();
			coords.add(new Coordinate(pt.getX(),pt.getY()));
			if (coords.size()>2)
			{
				if (feature==null) {
					Coordinate[] cds=new Coordinate[coords.size()+1];
					//coords.copyInto(cds);
					for (int k=0;k<coords.size();++k)
					{
						cds[k]=new Coordinate(coords.elementAt(k).x,coords.elementAt(k).y);
					}
					cds[coords.size()]=new Coordinate(cds[0].x,cds[0].y);//cds[0];
					Hashtable<String,Object> values=new Hashtable<String,Object>();
					Polygon pg=gf.createPolygon(gf.createLinearRing(cds));
					System.out.println("add="+pg);
					values.put("geometry",pg);
					feature=layer.addFeature(values);
				}
				else
				{
					Coordinate[] cds=new Coordinate[coords.size()+1];
					//coords.copyInto(cds);
					for (int k=0;k<coords.size();++k)
					{
						cds[k]=new Coordinate(coords.elementAt(k).x,coords.elementAt(k).y);
					}
					cds[coords.size()]=new Coordinate(cds[0].x,cds[0].y);//cds[0];
					Hashtable<String,Object> values=new Hashtable<String,Object>();
					Polygon pg=gf.createPolygon(gf.createLinearRing(cds));
					System.out.println("update="+pg);
					values.put("geometry",pg);
					layer.updateFeature(feature, values);
				}
			}
		}
	}

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		Point pt=mapView.toMapPoint(e.getX(),e.getY());
		if (snapLayers!=null)
		{	
			snapResult=null;
			double dis=Double.MAX_VALUE;
			for (UCFeatureLayer flayer : snapLayers)
			{
				dis=snap(flayer,pt,dis);
			}
			if (snapResult!=null)
			{
				Point point=mapView.fromMapPoint(snapResult.getX(), snapResult.getY());
				Point center=gf.createPoint(new Coordinate(e.getX(),e.getY()));
				double distance=Arithmetic.Distance(center, point);
				if (distance<snapDistance)
				{//如果小于设定的最小距离，则snap到result上
					if (snapAutoMode && snapLinePrev!=null && snapLinePrev==snapLine && snapIndexPrev!=snapIndex)
					{
						if (snapIndexPrev<snapIndex)
						{
							if (((snapIndex-snapIndexPrev)*2)<snapLine.getNumPoints())
							{
								for (int k=snapIndexPrev+1;k<=snapIndex;++k)
									onClick(snapLine.getPointN(k));
							}
							else if (snapLine.isClosed())
							{
								for (int k=snapIndexPrev;k>=0;--k)
									onClick(snapLine.getPointN(k));
								for (int k=snapLine.getNumPoints()-2;k>=snapIndex;--k)
									onClick(snapLine.getPointN(k));
							}
							else
							{
								onClick(snapResult);
							}
						}
						else
						{
							if (((snapIndexPrev-snapIndex)*2)<snapLine.getNumPoints())
							{
								for (int k=snapIndexPrev-1;k>=snapIndex;--k)
									onClick(snapLine.getPointN(k));
							}
							else if (snapLine.isClosed())
							{
								for (int k=snapIndexPrev;k<snapLine.getNumPoints();++k)
									onClick(snapLine.getPointN(k));
								for (int k=1;k<=snapIndex;++k)
									onClick(snapLine.getPointN(k));
							}
							else
							{
								onClick(snapResult);
							}
						}
					}
					else
					{
						onClick(snapResult);
					}
				}
				else
				{
					snapResult=null;
					snapLinePrev=null;
					snapLine=null;
					onClick(pt);
				}
			}
			else
			{
				onClick(pt);
			}
		}
		else
		{
			onClick(pt);
		}
		
		mapView.refresh();
		return true;
	}

}
