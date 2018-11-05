package cn.creable.surveyOnUCMap;

import android.view.GestureDetector.OnGestureListener;

import java.util.Vector;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.Point;

import android.graphics.Bitmap;
import android.view.MotionEvent;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCMarker;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCVectorLayer;

public class MeasureTool implements OnGestureListener,IMapTool{
	
	private UCMapView mMapView;
	private UCMarkerLayer mlayer;
	private UCVectorLayer vlayer;
	private Vector<Point> points=new Vector<Point>();
	private Bitmap pointImage;
	private int type;
	private Geometry geo;
	private UCMarker lastText;

	public MeasureTool(UCMapView mapView,Bitmap pointImage,int type)
	{
		this.mMapView=mapView;
		this.pointImage=pointImage;
		this.type=type;
		this.lastText=null;
	}

	@Override
	public boolean onDown(MotionEvent arg0) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean onFling(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void onLongPress(MotionEvent arg0) {
		// TODO Auto-generated method stub
		
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

	@Override
	public boolean onSingleTapUp(MotionEvent e) {
		//添加图片点符号
		Point pt=mMapView.toMapPoint(e.getX(), e.getY());
		mlayer.addBitmapItem(pointImage, pt.getX(),pt.getY(),"","");
		points.add(pt);
		if (type==0)
		{
			//添加文字
			String string=null;
	        if (points.size()==1)
	            string="起点";
	        else
	        {
	            double distance=getResult();
	            if (1<=distance && distance<1000) string=String.format("%.1f米",distance);
	            else if (distance>=1000) string=String.format("%.1f千米",distance/1000);
	            else if (0.01<=distance && distance<1) string=String.format("%.1f厘米",distance*100);
	            else string=String.format("%.1f毫米",distance*1000);
	        }
	        mlayer.addTextItem(string,android.graphics.Paint.Align.LEFT,50,0,pt.getX(),pt.getY(),"",20,0);
			//添加/修改图形
			if (points.size()==2)
			{
				GeometryFactory gf=new GeometryFactory();
				Coordinate[] coords=new Coordinate[points.size()];
				for (int i=0;i<points.size();++i)
				{
					coords[i]=new Coordinate(points.get(i).getX(),points.get(i).getY());
				}
				geo=gf.createLineString(coords);
				vlayer.addLine(geo, 3, 0xFF991111);
			}
			else if (points.size()>2)
			{
				GeometryFactory gf=new GeometryFactory();
				Coordinate[] coords=new Coordinate[points.size()];
				for (int i=0;i<points.size();++i)
				{
					coords[i]=new Coordinate(points.get(i).getX(),points.get(i).getY());
				}
				Geometry newgeo=gf.createLineString(coords);
				vlayer.updateGeometry(geo, newgeo);
				geo=newgeo;
			}
		}
		else if (type==1 && points.size()>2)
		{
			if (lastText!=null) mlayer.removeItem(lastText);
			//添加文字
			double area=getResult();
			String string=null;
            if (area<100000) string=String.format("%.1f平方米",area);
            else string=String.format("%.1f平方公里",area/1000/1000);
            int nn=points.size();
            double x = 0,y = 0;
            for (int i = 0; i < nn; ++i) {
                x += points.get(i).getX();
                y += points.get(i).getY();
            }
    		x=x / nn;
    		y=y / nn;
    		lastText=mlayer.addTextItem(string,android.graphics.Paint.Align.CENTER,50,0,x,y,"",0,0);
    		//添加/修改图形
    		if (points.size()==3)
    		{
    			GeometryFactory gf=new GeometryFactory();
				Coordinate[] coords=new Coordinate[points.size()+1];
				int i;
				for (i=0;i<points.size();++i)
				{
					coords[i]=new Coordinate(points.get(i).getX(),points.get(i).getY());
				}
				coords[i]=new Coordinate(points.get(0).getX(),points.get(0).getY());
				geo=gf.createPolygon(gf.createLinearRing(coords));
				vlayer.addPolygon(geo, 5, 0xFF991111,0xFF222299,0.5f);
    		}
    		else
    		{
    			GeometryFactory gf=new GeometryFactory();
				Coordinate[] coords=new Coordinate[points.size()+1];
				int i;
				for (i=0;i<points.size();++i)
				{
					coords[i]=new Coordinate(points.get(i).getX(),points.get(i).getY());
				}
				coords[i]=new Coordinate(points.get(0).getX(),points.get(0).getY());
				Geometry newgeo=gf.createPolygon(gf.createLinearRing(coords));
				vlayer.updateGeometry(geo, newgeo);
				geo=newgeo;
    		}
		}
		mMapView.refresh();
		return true;
	}
	
	private double getResult() {
		int size=points.size();
        if (size<2) return 0;
        if (type==0)
        {
            Point pt1=points.get(0);
            byte param=0;
            if (-180<=pt1.getX() && pt1.getX()<=180 && -90<=pt1.getY() && pt1.getY()<=90)
                param=1;
            double dis=0;
            for (int i=1;i<size;++i)
            {
                dis+=cn.creable.ucmap.openGIS.Arithmetic.Distance(points.get(i-1),points.get(i),param);
            }
            return dis;
        }
        else if (type==1)
        {
            Point pt1=points.get(0);
            byte param=0;
            if (-180<=pt1.getX() && pt1.getX()<=180 && -90<=pt1.getY() && pt1.getY()<=90)
                param=1;
            return cn.creable.ucmap.openGIS.Arithmetic.Area(points,param);
        }
        return 0;
	}

	public void start()
	{
		if (vlayer==null) vlayer=mMapView.addVectorLayer();
		if (mlayer==null) mlayer=mMapView.addMarkerLayer(null);
		points.clear();
		mMapView.setListener(this, null);
		this.lastText=null;
		//mMapView.moveLayer(mlayer, 0);
	}
	
	public void stop()
	{
		mMapView.deleteLayer(mlayer);
		mMapView.deleteLayer(vlayer);
		mMapView.setListener(null, null);
	}
}
