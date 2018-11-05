package cn.creable.surveyOnUCMap;

import android.graphics.Bitmap;
import android.view.GestureDetector.OnGestureListener;

import java.io.InputStream;
import java.net.URL;
import java.net.URLConnection;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;

import android.view.MotionEvent;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCVectorLayer;

public class PathAnalysisTool implements OnGestureListener,IMapTool {
	
	private UCMapView mMapView;
	private UCVectorLayer vlayer;
	private UCMarkerLayer mlayer;
	private double lon1,lat1,lon2,lat2;
	private int state;
	private Bitmap startBitmap,endBitmap;
	private LineString line;
	
	public PathAnalysisTool(UCMapView mapView,Bitmap startBitmap,Bitmap endBitmap)
	{
		mMapView=mapView;
		this.startBitmap=startBitmap;
		this.endBitmap=endBitmap;
	}
	
	public void start()
	{
		vlayer=mMapView.addVectorLayer();
		mlayer=mMapView.addMarkerLayer(null);
		mMapView.setListener(this, null);
		state=0;
	}
	
	public void end()
	{
		mMapView.deleteLayer(vlayer);
		mMapView.deleteLayer(mlayer);
		mMapView.refresh();
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
		Point pt=mMapView.toMapPoint(e.getX(), e.getY());
		if (state==0)
		{
			if (line!=null) {
				vlayer.remove(line);
				line=null;
			}
			mlayer.removeAllItems();
			lon1=pt.getX();
			lat1=pt.getY();
			mlayer.addBitmapItem(startBitmap, lon1,lat1,"","");
			state=1;
		}
		else if (state==1)
		{
			lon2=pt.getX();
			lat2=pt.getY();
			mlayer.addBitmapItem(endBitmap, lon2,lat2,"","");
			state=0;
			new Thread(new Runnable() {

				@Override
				public void run() {
					String page=String.format("http://www.tianditu.com/query.shtml?postStr={\"orig\":\"%f,%f\",\"dest\":\"%f,%f\",\"style\":\"0\"}&type=search",lon1,lat1,lon2,lat2);
					URL urlString;
					try {
						urlString = new URL(page);
						URLConnection conn = urlString.openConnection();
					    InputStream is = conn.getInputStream();
					    SAXParserFactory spf = SAXParserFactory.newInstance();
						SAXParser parser = spf.newSAXParser();
						PathAnalysisHandler pah=new PathAnalysisHandler();
						parser.parse(is,pah);
					    is.close();
					    GeometryFactory gf=new GeometryFactory();
					    line=gf.createLineString(pah.points);
					    vlayer.addLine(line, 5, 0x8000FF00);
					    mMapView.refresh();
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				    
				}
				
			}).start();
		}
		mMapView.refresh();
		return true;
	}

	@Override
	public void stop() {
		end();
	}

}
