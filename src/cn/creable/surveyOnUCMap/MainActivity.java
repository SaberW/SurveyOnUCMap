package cn.creable.surveyOnUCMap;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.Set;
import java.util.Vector;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;

import org.gdal.gdal.gdal;
import org.gdal.ogr.DataSource;
import org.gdal.ogr.FeatureDefn;
import org.gdal.ogr.FieldDefn;
import org.gdal.ogr.Layer;
import org.gdal.ogr.ogr;
import org.jeo.data.Cursor;
import org.jeo.vector.BasicFeature;
import org.jeo.vector.Feature;
import org.jeo.vector.Field;
import org.jeo.vector.Schema;
import org.osgeo.proj4j.CRSFactory;
import org.osgeo.proj4j.CoordinateTransform;
import org.osgeo.proj4j.CoordinateTransformFactory;
import org.osgeo.proj4j.ProjCoordinate;

import com.github.johnkil.print.PrintConfig;
import com.github.johnkil.print.PrintView;
import com.urizev.gpx.GPXParser;
import com.urizev.gpx.beans.GPX;
import com.urizev.gpx.beans.Track;
import com.urizev.gpx.beans.Waypoint;
import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.LayoutParams;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.OvershootInterpolator;
import android.view.animation.TranslateAnimation;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import cn.creable.ucmap.openGIS.UCCoordinateFilter;
import cn.creable.ucmap.openGIS.UCFeatureLayer;
import cn.creable.ucmap.openGIS.UCFeatureLayerListener;
import cn.creable.ucmap.openGIS.UCLayer;
import cn.creable.ucmap.openGIS.UCMapView;
import cn.creable.ucmap.openGIS.UCMapViewListener;
import cn.creable.ucmap.openGIS.UCMarker;
import cn.creable.ucmap.openGIS.UCMarkerLayer;
import cn.creable.ucmap.openGIS.UCMarkerLayerListener;
import cn.creable.ucmap.openGIS.UCRasterLayer;
import cn.creable.ucmap.openGIS.UCStyle;
import cn.creable.ucmap.openGIS.UCVectorLayer;

public class MainActivity extends Activity implements UCFeatureLayerListener,LocationListener,android.widget.AdapterView.OnItemClickListener{
	private MenuAdapter ma = new MenuAdapter(this, new String[]{"保存数据",/*"图层控制", */"地图搜索",/* "文件管理", */"选择shp","选择影像","查看属性", "测距", "测面积","定位","记录轨迹","加载轨迹","路径分析"/*, "分屏", "卷帘", "涂鸦", "统计", "天地图"*/},
            new int[]{R.drawable.toolbar,/*R.drawable.tckz, */R.drawable.dtss, /*R.drawable.wjgl, */R.drawable.ty,R.drawable.yx,R.drawable.cksx, R.drawable.cj, R.drawable.cmj,R.drawable.dw,R.drawable.gpx,R.drawable.layer_output,R.drawable.add_polyline/*, R.drawable.fp, R.drawable.jl, R.drawable.ty, R.drawable.tj, R.drawable.tdt*/});//左侧菜单适配器
	
	UCMapView mView;
	
	//private MeasureTool mTool=null;
	private UCCoordinateFilter filter;
	
	private LocationManager locationManager;
	private boolean locationFlag=false;
	private boolean inAnim=false;
	
	private UCMarkerLayer locationLayer;
	private UCMarker location;
	
	private double currentLon,currentLat;
	
	TextView tv_maptype1,tv_maptype2,tv_maptype3;
	
	UCVectorLayer vlayer;
	GeometryFactory gf=new GeometryFactory();
	//PathAnalysisTool paTool;
	IMapTool curTool;
	
	class Layer
	{
		String pathname;
		UCFeatureLayer layer;
		UCRasterLayer rlayer;
		boolean flag;
	}
	
	UCRasterLayer gLayer;
	Vector<Layer> layers=new Vector<Layer>();
	Layer pointLayer=new Layer();
	Layer lineLayer=new Layer();
	Layer polygonLayer=new Layer();
	int type;
	Vector<String> shps=new Vector<String>();
	Vector<String> shpNames=new Vector<String>();
	Vector<String> mbs=new Vector<String>();
	Vector<String> mbNames=new Vector<String>();
	Vector<Layer> mbLayers=new Vector<Layer>();
	
	private FrameLayout fragment_menu;//承载二级菜单界面
    private AnimationSet openMenuAnimation;//弹出二级菜单动画
    private AnimationSet closeMenuAnimation;//关闭二级菜单动画
    private Handler handler;
    private Runnable closeMenuRunnable;//定时隐藏二级菜单
    private RelativeLayout toolbar;//底部toolbar
    private TranslateAnimation openToolbarAnimation;//底部toolbar弹出动画
    private TranslateAnimation closeToolbarAnimation;//底部toolbar关闭动画
    
    private PopupWindow popupWindow;//右上角工具弹出菜单
    private String currentTool;
    
    private Thread routeThread;
    private boolean routeFlag;
    private ArrayList<Waypoint> routePoints;
    private LineString route;
    
    WakeLock mWakeLock;
    
    public  String[] fields;public String[] values;
    
    private String dir=Environment.getExternalStorageDirectory().getPath();
	
	public double transformlat(double lng, double lat) {
        double ret = -100.0 + 2.0 * lng + 3.0 * lat + 0.2 * lat * lat + 0.1 * lng * lat + 0.2 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lat * Math.PI) + 40.0 * Math.sin(lat / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (160.0 * Math.sin(lat / 12.0 * Math.PI) + 320 * Math.sin(lat * Math.PI / 30.0)) * 2.0 / 3.0;
        return ret;
    };

    public double transformlng(double lng, double lat) {
        double ret = 300.0 + lng + 2.0 * lat + 0.1 * lng * lng + 0.1 * lng * lat + 0.1 * Math.sqrt(Math.abs(lng));
        ret += (20.0 * Math.sin(6.0 * lng * Math.PI) + 20.0 * Math.sin(2.0 * lng * Math.PI)) * 2.0 / 3.0;
        ret += (20.0 * Math.sin(lng * Math.PI) + 40.0 * Math.sin(lng / 3.0 * Math.PI)) * 2.0 / 3.0;
        ret += (150.0 * Math.sin(lng / 12.0 * Math.PI) + 300.0 * Math.sin(lng / 30.0 * Math.PI)) * 2.0 / 3.0;
        return ret;
    };
    
    public double ee = 0.00669342162296594323;
    public double a = 6378245.0;
    public double x_PI = 3.14159265358979324 * 3000.0 / 180.0;
    
    private void copyAssertFile(String fileName,String dest)
	{
		try {
			InputStream is=this.getAssets().open(fileName);
			FileOutputStream fos=new FileOutputStream(dest);
			byte[] buffer=new byte[8912];
			int count=0;
			while ((count=is.read(buffer))>0)
			{
				fos.write(buffer, 0, count);
			}
			fos.close();
			is.close();
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
    
    private float px2dp(float px) {
        float scale = getResources().getDisplayMetrics().density / 1.5f;
        return px * scale + 0.5f;
        //Resources r = getResources();
        //return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, px, r.getDisplayMetrics());
    }

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		PrintConfig.initDefault(getAssets(), "material-icon-font.ttf");//初始化printview控件要显示的字体文件
		UCFeatureLayer.setSimplifyTolerance(0.1f);
		UCMapView.setTileScale(0.5f);
		setContentView(R.layout.activity_main);
		ListView menu = (ListView) findViewById(R.id.lv_main_menu);
        //设置左侧菜单
        menu.setAdapter(ma);
        //设置菜单点击事件
        menu.setOnItemClickListener(this);
        fragment_menu = (FrameLayout) findViewById(R.id.fragment_menu);
        //初始化二级菜单弹出动画
        openMenuAnimation = new AnimationSet(true);
        TranslateAnimation open = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, -1, Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        openMenuAnimation.addAnimation(open);
        openMenuAnimation.setFillAfter(true);
        openMenuAnimation.setDuration(500);
        //初始化二级菜单关闭动画
        closeMenuAnimation = new AnimationSet(true);
        TranslateAnimation close = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, -1,
                Animation.RELATIVE_TO_SELF, 0, Animation.RELATIVE_TO_SELF, 0);
        closeMenuAnimation.addAnimation(close);
        closeMenuAnimation.setFillAfter(true);
        closeMenuAnimation.setDuration(500);
        handler = new Handler();
        //二级菜单关闭动画结束后将承载二级菜单的fragment隐藏掉，否则它会继续拦截触屏事件，导致原来显示二级菜单区域的地图无法接收触屏事件
        closeMenuRunnable = new Runnable() {
            @Override
            public void run() {
                fragment_menu.setVisibility(View.GONE);
                fragment_menu.removeAllViews();
            }
        };
        //初始化底部toolbar弹出动画
        openToolbarAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF,
                1.5f, Animation.RELATIVE_TO_SELF, 0.0f);
        openToolbarAnimation.setDuration(500);
        //初始化底部toolbar关闭动画
        closeToolbarAnimation = new TranslateAnimation(Animation.RELATIVE_TO_SELF,
                0.0f, Animation.RELATIVE_TO_SELF, 0.0f,
                Animation.RELATIVE_TO_SELF, 0.0f, Animation.RELATIVE_TO_SELF, 1.5f);
        closeToolbarAnimation.setDuration(500);
        //初始化底部toolbar
        toolbar = (RelativeLayout) findViewById(R.id.rl_main_toolbar);
        HorizontalListView hlv = (HorizontalListView) findViewById(R.id.hlv_main_toolbar);
        hlv.setOnItemClickListener(this);
        //关闭toolbar
        PrintView pv = (PrintView) findViewById(R.id.pv_main);
        pv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toolbar.startAnimation(closeToolbarAnimation);
                toolbar.setVisibility(View.GONE);
            }
        });
        TextView tv_qt = (TextView) findViewById(R.id.tv_main_qt);
        //全图点击事件
        tv_qt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	inAnim=true;
            	Envelope env=null;
    			UCFeatureLayer flayer=null;
    			int size=layers.size();
				for (int i1=0;i1<size;++i1)
				//for (Layer layer:layers)
    			{
					flayer=layers.get(i1).layer;
					if (flayer!=null) {
						if (env==null) env=flayer.getFullExtent();
						else env.expandToInclude(flayer.getFullExtent());
					}
    			}
    			UCRasterLayer rlayer;
    			size=mbLayers.size();
				for (int i1=0;i1<size;++i1)
				//for (Layer layer:mbLayers)
    			{
					rlayer=mbLayers.get(i1).rlayer;
					if (rlayer!=null && rlayer.getFullExtent()!=null)
					{
						if (env==null) env=rlayer.getFullExtent();
						else env.expandToInclude(rlayer.getFullExtent());
					}
    			}
    			if (env!=null) mView.refresh(1000, env);
    			else mView.refresh();
            }
        });
        final TextView tv_gj = (TextView) findViewById(R.id.tv_main_gj);
        View pop = View.inflate(MainActivity.this, R.layout.menu, null);
        ListView lv = (ListView) pop.findViewById(R.id.lv_menu);
        //初始化工具菜单，工具菜单前面的小图标是用printview实现的，实际使用的时候建议替换成imageview，并同时修改对应的adapter中的getview方法
        lv.setAdapter(new ShowItemsAdapter(MainActivity.this, new String[]{ "数据采集" ,"调查取证"/*,"导出图层", "清空"  , "标绘工具条"*/}, new int[]{
                R.drawable.toolbar,R.drawable.toolbar/*,R.drawable.layer_output, R.drawable.clean, R.drawable.toolbar*/
        }));
        lv.setOnItemClickListener(MainActivity.this);

        popupWindow = new PopupWindow(pop, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);

        popupWindow.setWidth((int) px2dp(300));//设置工具菜单显示宽度
        popupWindow.setHeight((int) px2dp(400));//设置工具菜单显示高度
        popupWindow.setTouchable(true);
        popupWindow.setTouchInterceptor(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return false;
            }
        });
        popupWindow.setBackgroundDrawable(getResources().getDrawable(R.drawable.out_view_backgroud));
        //工具点击事件
        tv_gj.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                popupWindow.showAsDropDown(tv_gj, 130, 0);
            }
        });
        
        tv_maptype1 = (TextView) findViewById(R.id.tv_maptype1);
        tv_maptype1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (type!=0)
    			{
    				mView.deleteLayer(gLayer);
    				String dir=Environment.getExternalStorageDirectory().getPath();
    				gLayer=mView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=m&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 0, 20, dir+"/cacheGoogleMapM.db");
    				mView.moveLayer(gLayer, 0);
    				mView.refresh();
    				type=0;
    			}
            	
            	tv_maptype1.setBackgroundColor(Color.GRAY);
            	tv_maptype2.setBackgroundColor(Color.WHITE);
            	tv_maptype3.setBackgroundColor(Color.WHITE);
            }
        });
        tv_maptype2 = (TextView) findViewById(R.id.tv_maptype2);
        tv_maptype2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (type!=1)
    			{
    				mView.deleteLayer(gLayer);
    				String dir=Environment.getExternalStorageDirectory().getPath();
    				gLayer=mView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=p&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 0, 20, dir+"/cacheGoogleMapP.db");
    				mView.moveLayer(gLayer, 0);
    				mView.refresh();
    				type=1;
    			}
            	tv_maptype2.setBackgroundColor(Color.GRAY);
            	tv_maptype1.setBackgroundColor(Color.WHITE);
            	tv_maptype3.setBackgroundColor(Color.WHITE);
            }
        });
        tv_maptype3 = (TextView) findViewById(R.id.tv_maptype3);
        tv_maptype3.setBackgroundColor(Color.GRAY);
        tv_maptype3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
            	if (type!=2)
    			{
    				mView.deleteLayer(gLayer);
    				String dir=Environment.getExternalStorageDirectory().getPath();
    				gLayer=mView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=y&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 0, 20, dir+"/cacheGoogleMapY.db");
    				mView.moveLayer(gLayer, 0);
    				mView.refresh();
    				type=2;
    			}
            	tv_maptype3.setBackgroundColor(Color.GRAY);
            	tv_maptype2.setBackgroundColor(Color.WHITE);
            	tv_maptype1.setBackgroundColor(Color.WHITE);
            }
        });
        
//		Coordinate[] points1=new Coordinate[5];
//		points1[0]=new Coordinate(0,0);
//		points1[1]=new Coordinate(100,0);
//		points1[2]=new Coordinate(100,100);
//		points1[3]=new Coordinate(0,100);
//		points1[4]=points1[0];
//		GeometryFactory gf=new GeometryFactory();
//		Polygon target=gf.createPolygon(points1);
//		Coordinate[] points2=new Coordinate[2];
//		points2[0]=new Coordinate(50,120);
//		points2[1]=new Coordinate(70,-100);
//		LineString ref=gf.createLineString(points2);
//		Geometry[] results=Utils.cut(target, ref);
		
		mView=(UCMapView)this.findViewById(R.id.mv_main);
		mView.setBackgroundColor(0xFFFFFFFF);
		mView.rotation(false);
		
		filter=new UCCoordinateFilter() {

			@Override
			public double[] to(double x, double y) {
				double[] result=new double[2];
				double dlat = transformlat(x - 105.0, y - 35.0);
		        double dlng = transformlng(x - 105.0, y - 35.0);
		        double radlat = y / 180.0 * Math.PI;
		        double magic = Math.sin(radlat);
		        magic = 1 - ee * magic * magic;
		        double sqrtmagic = Math.sqrt(magic);
		        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * Math.PI);
		        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * Math.PI);
		        double mglat = y + dlat;
		        double mglng = x + dlng;
		        result[0]=mglng;
		        result[1]=mglat;
				return result;
			}

			@Override
			public double[] from(double x, double y) {
				double[] result=new double[2];
				double dlat = transformlat(x - 105.0, y - 35.0);
		    	double dlng = transformlng(x - 105.0, y - 35.0);
		    	double radlat = y / 180.0 * Math.PI;
		    	double magic = Math.sin(radlat);
		        magic = 1 - ee * magic * magic;
		        double sqrtmagic = Math.sqrt(magic);
		        dlat = (dlat * 180.0) / ((a * (1 - ee)) / (magic * sqrtmagic) * Math.PI);
		        dlng = (dlng * 180.0) / (a / sqrtmagic * Math.cos(radlat) * Math.PI);
		        double mglat = y + dlat;
		        double mglng = x + dlng;
		        result[0]=x * 2 - mglng;
		        result[1]=y * 2 - mglat;
				return result;
			}
			
		};
		mView.setCoordinateFilter(filter);
		
		
//		gLayer=mView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=m&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 0, 20, dir+"/cacheGoogleMapM.db");
//		gLayer=mView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=p&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 0, 20, dir+"/cacheGoogleMapP.db");
		gLayer=mView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=y&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 0, 20, dir+"/cacheGoogleMapY.db");
//		mView.setLayerVisible(1, false);
//		mView.setLayerVisible(2, false);
		type=2;
//		mView.addTDMapLayer("http://t0.tianditu.cn/img_c/wmts?SERVICE=WMTS&REQUEST=GetTile&VERSION=1.0.0&LAYER=img&STYLE=default&TILEMATRIXSET=c&FORMAT=tiles", 1, 18,dir+"/cacheImg.db");
//		mView.addTDMapLayer("http://t1.tianditu.cn/cia_c/wmts?service=wmts&request=GetTile&version=1.0.0&LAYER=cia&tileMatrixSet=c&format=tiles", 1, 18,dir+"/cacheCia.db");
		
		File f=new File(GVS.getInstance().mapPath);
		if (!f.exists())
			f.mkdirs();
		
//		layer=mView.addFeatureLayer(this);
//		layerShp=dir + "/changdeng2.shp";
//		File file=new File(layerShp);
//		if (!file.exists()) 
//		{
//			this.copyAssertFile("changdeng2.shp", dir + "/changdeng2.shp");
//			this.copyAssertFile("changdeng2.cpg", dir + "/changdeng2.cpg");
//			this.copyAssertFile("changdeng2.dbf", dir + "/changdeng2.dbf");
//			this.copyAssertFile("changdeng2.prj", dir + "/changdeng2.prj");
//			this.copyAssertFile("changdeng2.shx", dir + "/changdeng2.shx");
//		}
//		layer.loadShapefile(file.getAbsolutePath(), 30, 2, "#FFFF0000", "#00000000",true);
		
		if (pointLayer.layer==null)
		{
			pointLayer.pathname=GVS.getInstance().mapPath+"临时点.shp";
			File file1=new File(pointLayer.pathname);
			if (file1.exists())
			{
				pointLayer.layer=mView.addFeatureLayer(this);
				pointLayer.layer.loadShapefile(pointLayer.pathname, 30, 2, "#FFFF0000", "#FFFF0000",true);
			}
		}
		if (lineLayer.layer==null)
		{
			lineLayer.pathname=GVS.getInstance().mapPath+"临时线.shp";
			File file1=new File(lineLayer.pathname);
			if (file1.exists())
			{
				lineLayer.layer=mView.addFeatureLayer(this);
				lineLayer.layer.loadShapefile(lineLayer.pathname, 30, 2, "#FFFF0000", "#00000000",true);
			}
		}
		if (polygonLayer.layer==null)
		{
			polygonLayer.pathname=GVS.getInstance().mapPath+"临时面.shp";
			File file1=new File(polygonLayer.pathname);
			if (file1.exists())
			{
				polygonLayer.layer=mView.addFeatureLayer(this);
				polygonLayer.layer.loadShapefile(polygonLayer.pathname, 30, 2, "#FFFF0000", "#00000000",true);
			}
		}
		
		if (vlayer==null) vlayer=mView.addVectorLayer();
		
		//mView.addLocationLayer();
		//locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
		
		//mView.moveTo(114.25,23.05, 2<<14);
		//mView.moveTo(121.05,29.86, 512);
		mView.moveTo(116.41, 25.65, 2<<14);
		mView.postDelayed(new Runnable() {
			@Override
			public void run() {
				mView.refresh();
			}
		}, 0);
		

		PowerManager pm = (PowerManager)getSystemService(Context.POWER_SERVICE);

		mWakeLock = pm.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK | PowerManager.ACQUIRE_CAUSES_WAKEUP, this.getClass().getCanonicalName());

		
//		Button btn1=(Button)findViewById(R.id.button1);
//        if (btn1!=null)
//        btn1.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (curTool!=null)
//				{
//					curTool.stop();
//					curTool=null;
//				}
//				BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
//				MeasureTool mTool=new MeasureTool(mView,bd.getBitmap(),0);
//				mTool.start();
//				curTool=mTool;
//			}
//        });
//        
//        Button btn2=(Button)findViewById(R.id.button2);
//        if (btn2!=null)
//        btn2.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (curTool!=null)
//				{
//					curTool.stop();
//					curTool=null;
//				}
//				BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
//				MeasureTool mTool=new MeasureTool(mView,bd.getBitmap(),1);
//				mTool.start();
//				curTool=mTool;
//			}
//        });
//        
//        Button btn3=(Button)findViewById(R.id.button3);
//        if (btn3!=null)
//        btn3.setOnClickListener(new OnClickListener() {
//
//			@Override
//			public void onClick(View v) {
//				if (curTool!=null)
//				{
//					curTool.stop();
//					curTool=null;
//					mView.getMaskLayer().clear();
//					mView.refresh();
//				}
//			}
//        });
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.menu, menu);
		return true;
	}

//	@Override
//	public boolean onOptionsItemSelected(MenuItem item) {
//		// Handle action bar item clicks here. The action bar will
//		// automatically handle clicks on the Home/Up button, so long
//		// as you specify a parent activity in AndroidManifest.xml.
//		int id = item.getItemId();
//		if (id == R.id.menu_1) {
//			if (type!=0)
//			{
//				mView.deleteLayer(gLayer);
//				String dir=Environment.getExternalStorageDirectory().getPath();
//				gLayer=mView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=m&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 0, 20, dir+"/cacheGoogleMapM.db");
//				mView.moveLayer(gLayer, 0);
//				mView.refresh();
//				type=0;
//			}
////			mView.setLayerVisible(0, true);
////			mView.setLayerVisible(1, false);
////			mView.setLayerVisible(2, false);
////			mView.refresh();
//		}
//		else if (id==R.id.menu_2){
//			if (type!=1)
//			{
//				mView.deleteLayer(gLayer);
//				String dir=Environment.getExternalStorageDirectory().getPath();
//				gLayer=mView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=p&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 0, 20, dir+"/cacheGoogleMapP.db");
//				mView.moveLayer(gLayer, 0);
//				mView.refresh();
//				type=1;
//			}
////			mView.setLayerVisible(0, false);
////			mView.setLayerVisible(1, true);
////			mView.setLayerVisible(2, false);
////			mView.refresh();
//		}
//		else if (id==R.id.menu_3){
//			if (type!=2)
//			{
//				mView.deleteLayer(gLayer);
//				String dir=Environment.getExternalStorageDirectory().getPath();
//				gLayer=mView.addGoogleMapLayer("http://mt0.google.cn/vt/lyrs=y&hl=zh-CN&gl=cn&scale=2&x={X}&y={Y}&z={Z}", 0, 20, dir+"/cacheGoogleMapY.db");
//				mView.moveLayer(gLayer, 0);
//				mView.refresh();
//				type=2;
//			}
////			mView.setLayerVisible(0, false);
////			mView.setLayerVisible(1, false);
////			mView.setLayerVisible(2, true);
////			mView.refresh();
//		}
//		else if (id == R.id.menu_4) {
//			if (curTool!=null)
//			{
//				curTool.stop();
//				curTool=null;
//			}
//			BitmapDrawable start=(BitmapDrawable) getResources().getDrawable(R.drawable.start);
//			BitmapDrawable end=(BitmapDrawable) getResources().getDrawable(R.drawable.end);
//			PathAnalysisTool paTool=new PathAnalysisTool(mView,start.getBitmap(),end.getBitmap());
//			paTool.start();
//			curTool=paTool;
//			return true;
//		}
//		else if (id==R.id.menu_5) {
//			if (curTool!=null)
//			{
//				curTool.stop();
//				curTool=null;
//				mView.refresh();
//			}
//			return true;
//		}
//		else if (id==R.id.menu_6) {
//			if (curTool!=null)
//			{
//				curTool.stop();
//				curTool=null;
//			}
//			BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
//			AddFeatureTool2 addTool2=new AddFeatureTool2(mView,layer,bd.getBitmap());
//			UCFeatureLayer[] snapLayers=new UCFeatureLayer[1];
//			snapLayers[0]=layer;
//			addTool2.openSnap(snapLayers, 40,true);
//			addTool2.start();
//			curTool=addTool2;
//		}
//		else if (id==R.id.menu_7) {
//			if (curTool!=null)
//			{
//				curTool.stop();
//				curTool=null;
//			}
//			CutTool tool=new CutTool(mView,layer);
//			curTool=tool;
//		}
//		else if (id==R.id.menu_8) {
//			UndoRedo.getInstance().undo();
//			mView.getMaskLayer().clear();
//			mView.refresh();
//		}
//		else if (id==R.id.menu_9) {
//			UndoRedo.getInstance().redo();
//			mView.getMaskLayer().clear();
//			mView.refresh();
//		}
//		else if (id==R.id.menu_10) {
////			if (curTool!=null)
////			{
////				curTool.stop();
////				curTool=null;
////			}
////			DeleteFeatureTool tool=new DeleteFeatureTool(mView,layer);
////			curTool=tool;
//		}
//		else if (id==R.id.menu_11) {
//			String dir=Environment.getExternalStorageDirectory().getPath();
//			layer.saveShapefile(dir + "/changdeng2.shp");
//		}
//
//		return super.onOptionsItemSelected(item);
//	}
	
	static Feature feature(String id, Object... values) {
        Feature current=new BasicFeature(id,Arrays.asList(values));
        return current;
    }
	
	@Override
    protected void onResume() {
        super.onResume();
        enableAvailableProviders();
    }

	@Override
	public boolean onItemLongPress(UCFeatureLayer layer, Feature feature, double distance) {
		if (curTool!=null) return true;
		if (distance>30) return false;
		Toast.makeText(getBaseContext(), "长按了\n" + feature.id() + " distance="+distance, Toast.LENGTH_SHORT).show();
		Vector<Feature> features=new Vector<Feature>();
		features.add(feature);
		mView.getMaskLayer().setData(features, 30, 2, "#88FF0000", "#88FF0000");
		mView.refresh();
		return true;
	}

	@Override
	public boolean onItemSingleTapUp(UCFeatureLayer layer, Feature feature, double distance) {
		if (curTool!=null) return true;
		if (distance>30) return false;
		//Toast.makeText(getBaseContext(), "点击了\n" + feature.id() + " distance="+distance, Toast.LENGTH_SHORT).show();
		Vector<Feature> features=new Vector<Feature>();
		features.add(feature);
		mView.getMaskLayer().setData(features, 30, 2, "#88FF0000", "#88FF0000");
		mView.refresh();
		
		int fieldCount=layer.getFieldCount()-1;
		fields=new String[fieldCount];
		values=new String[fieldCount];
		for (int i=0;i<fieldCount;++i)
		{
			Field f=layer.getField(i+1);
			fields[i]=f.name();
			Object value=feature.get(f.name());
			if (value!=null)
			{
				if (f.type()==Byte.class)
					values[i]=Byte.toString((Byte)value);
				else if (f.type()==Short.class)
					values[i]=Short.toString((Short)value);
				else if (f.type()==Integer.class)
					values[i]=Integer.toString((Integer)value);
				else if (f.type()==Long.class)
					values[i]=Long.toString((Long)value);
				else if (f.type()==Float.class)
					values[i]=Float.toString((Float)value);
				else if (f.type()==Double.class)
					values[i]=Double.toString((Double)value);
				else if (f.type()==java.sql.Date.class)
				{
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					values[i]=format.format((java.sql.Date)value);
				}
				else if (f.type()==java.sql.Time.class)
				{
					SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
					values[i]=format.format((java.sql.Time)value);
				}
				else if (f.type()==String.class)
					values[i]=(String)value;
			}
			else
			{
				if (f.type()==String.class)
					values[i]="";
				else if (f.type()==java.sql.Date.class)
				{
					values[i]="";
				}
				else if (f.type()==java.sql.Time.class)
				{
					values[i]="";
				}
				else
					values[i]="0";
			}
		}
		
		showMenu(new ShowAttributeFragment(), curMenuIndex);
		return true;
	}

	@Override
	public void onLocationChanged(Location location) {
		mView.setLocationPosition(location.getLongitude(),location.getLatitude(),location.getAccuracy());
		
//		if (inAnim)
//		{
//			try {
//				Thread.sleep(1100);
//			} catch (InterruptedException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//			inAnim=false;
//		}
		
		if (locationFlag)
		{
//			mView.refresh();
			currentLon=location.getLongitude();
			currentLat=location.getLatitude();
			this.location.setXY(currentLon, currentLat);
			this.locationLayer.refresh();
		}
		else
		{
			Bitmap start=BitmapFactory.decodeResource(getResources(),R.drawable.arrow);
			
			currentLon=location.getLongitude();
			currentLat=location.getLatitude();
			this.location=locationLayer.addBitmapItem(start, currentLon,currentLat,"","");
			mView.animateTo(500,currentLon,currentLat, mView.getScale());
			locationFlag=true;
		}
	}

	@Override
	public void onProviderDisabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onProviderEnabled(String arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void onStatusChanged(String arg0, int arg1, Bundle arg2) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
    protected void onPause() {
        super.onPause();

        if (locationManager!=null) locationManager.removeUpdates(this);
    }
    
    private void enableAvailableProviders() {
    	if (locationManager==null) return;
        locationManager.removeUpdates(this);

        for (String provider : locationManager.getProviders(true)) {
            if (LocationManager.GPS_PROVIDER.equals(provider)
                    || LocationManager.NETWORK_PROVIDER.equals(provider)) {
                locationManager.requestLocationUpdates(provider, 0, 0, this);
            }
        }
    }
    
  //显示二级菜单
    private void showMenu(Fragment fragment, int index) {

        if (toolbar.getVisibility() == View.VISIBLE) {
            toolbar.startAnimation(closeToolbarAnimation);
            toolbar.setVisibility(View.GONE);
        }
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_menu, fragment);
        transaction.commit();
        fragment_menu.setVisibility(View.VISIBLE);
        fragment_menu.startAnimation(openMenuAnimation);
        ma.setCurrentIndex(index);
    }

    //关闭二级菜单
    public void closeMenu() {
        fragment_menu.startAnimation(closeMenuAnimation);
        ma.setCurrentIndex(-1);
        handler.postDelayed(closeMenuRunnable, 500);
    }
    
    private int curMenuIndex;
    
    private void hideToolBars() {
        if (toolbar.getVisibility() == View.VISIBLE) {
            toolbar.startAnimation(closeToolbarAnimation);
            toolbar.setVisibility(View.GONE);
        }
    }
    
    private void showToolBars() {
        if (toolbar.getVisibility() != View.VISIBLE) {
            if (ma.getCurrentIndex() != -1) {
                closeMenu();
            }
            toolbar.setVisibility(View.VISIBLE);
            toolbar.startAnimation(openToolbarAnimation);
        }
    }

	@Override
	public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
		String menu = (String) adapterView.getItemAtPosition(i);
		curMenuIndex = i;
		if ("数据采集".equals(menu)) {
			currentTool = menu;
			popupWindow.dismiss();
			hideToolBars();
			toolbar = (RelativeLayout) findViewById(R.id.rl_main_toolbar);
			HorizontalListView hlv = (HorizontalListView) findViewById(R.id.hlv_main_toolbar);
			hlv.setAdapter(new ToolBarAdapter(this,
					new ToolBarMenu[] { 
							new ToolBarMenu("添加点", R.drawable.add_point, false),
							new ToolBarMenu("添加线", R.drawable.add_polyline, false),
							new ToolBarMenu("添加面", R.drawable.add_polygon, false),
							new ToolBarMenu("添加点(打点)", R.drawable.add_point, false),
							new ToolBarMenu("添加线(打点)", R.drawable.add_polyline, false),
							new ToolBarMenu("添加面(打点)", R.drawable.add_polygon, false),
							new ToolBarMenu("删除", R.drawable.delete, false),
							new ToolBarMenu("编辑属性", R.drawable.edit_att, false),
							new ToolBarMenu("编辑图形", R.drawable.node_move, false),
							// new ToolBarMenu("添加节点", R.drawable.node_add,
							// false),
							// new ToolBarMenu("删除节点", R.drawable.node_delete,
							// false),
							new ToolBarMenu("撤销", R.drawable.undo, false),
							new ToolBarMenu("重做", R.drawable.redo, false),
							new ToolBarMenu("裁切", R.drawable.split, false),
							// new ToolBarMenu("合并", R.drawable.merge, false),
							new ToolBarMenu("开启捕捉", R.drawable.snap_open, false),
							new ToolBarMenu("关闭捕捉", R.drawable.snap_close, false) }));
			showToolBars();
		} else if ("调查取证".equals(menu)) {
			currentTool = menu;
			popupWindow.dismiss();
			hideToolBars();
			toolbar = (RelativeLayout) findViewById(R.id.rl_main_toolbar);
			HorizontalListView hlv = (HorizontalListView) findViewById(R.id.hlv_main_toolbar);
			hlv.setAdapter(new ToolBarAdapter(this,
					new ToolBarMenu[] { new ToolBarMenu("添加点", R.drawable.add_point, false),
							new ToolBarMenu("添加线", R.drawable.add_polyline, false),
							new ToolBarMenu("添加面", R.drawable.add_polygon, false),
							new ToolBarMenu("删除", R.drawable.delete, false),
							new ToolBarMenu("编辑属性", R.drawable.edit_att, false),
							new ToolBarMenu("编辑图形", R.drawable.node_move, false),
							// new ToolBarMenu("添加节点", R.drawable.node_add,
							// false),
							// new ToolBarMenu("删除节点", R.drawable.node_delete,
							// false),
							new ToolBarMenu("撤销", R.drawable.undo, false),
							new ToolBarMenu("重做", R.drawable.redo, false),
							new ToolBarMenu("裁切", R.drawable.split, false),
							// new ToolBarMenu("合并", R.drawable.merge, false),
							new ToolBarMenu("开启捕捉", R.drawable.snap_open, false),
							new ToolBarMenu("关闭捕捉", R.drawable.snap_close, false) }));
			showToolBars();
		} else if ("地图搜索".equals(menu)) {
			showMenu(new Dtss(),i);
		} else if ("查看属性".equals(menu)) {
			if (curTool!=null)
			{
				curTool.stop();
				curTool=null;
				mView.getMaskLayer().clear();
				mView.refresh();
			}
			int size=layers.size();
			for (int i1=0;i1<size;++i1)
			//for (Layer layer:layers) 
			{
				if (layers.get(i1).layer!=null) layers.get(i1).layer.setListener(this);
			}
			if (pointLayer.layer!=null) pointLayer.layer.setListener(this);
			if (lineLayer.layer!=null) lineLayer.layer.setListener(this);
			if (polygonLayer.layer!=null) polygonLayer.layer.setListener(this);
		} else if ("测距".equals(menu)) {
			if (curTool!=null)
			{
				curTool.stop();
				curTool=null;
				mView.getMaskLayer().clear();
				mView.refresh();
			}
			BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
			MeasureTool mTool=new MeasureTool(mView,bd.getBitmap(),0);
			mTool.start();
			curTool=mTool;
		} else if ("测面积".equals(menu)) {
			if (curTool!=null)
			{
				curTool.stop();
				curTool=null;
				mView.getMaskLayer().clear();
				mView.refresh();
			}
			BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
			MeasureTool mTool=new MeasureTool(mView,bd.getBitmap(),1);
			mTool.start();
			curTool=mTool;
		} else if ("保存数据".equals(menu)) {
			int size=layers.size();
			for (int i1=0;i1<size;++i1)
			//for (Layer layer:layers) 
			{
				Layer layer=layers.get(i1);
				if (layer.layer!=null && layer.flag) {
					layer.layer.saveShapefile(layer.pathname);
					layer.flag=false;
				}
			}
			if (pointLayer.flag && pointLayer.layer!=null)
			{
				pointLayer.layer.saveShapefile(pointLayer.pathname);
				pointLayer.flag=false;
			}
			if (lineLayer.flag && lineLayer.layer!=null)
			{
				lineLayer.layer.saveShapefile(lineLayer.pathname);
				lineLayer.flag=false;
			}
			if (polygonLayer.flag && polygonLayer.layer!=null)
			{
				polygonLayer.layer.saveShapefile(polygonLayer.pathname);
				polygonLayer.flag=false;
			}
			BluToast.makeText(MainActivity.this, "保存完毕", BluToast.LENGTH_SHORT).show();
		} else if ("定位".equals(menu)) {
			if (locationManager==null)
			{
				mView.addLocationLayer(Color.RED);
				locationLayer=mView.addMarkerLayer(null);
				locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
				enableAvailableProviders();
				
		        final Azimuth a=new Azimuth();
		        a.start(MainActivity.this,new SensorEventListener() {
					@Override
					public void onAccuracyChanged(Sensor arg0, int arg1) {
						// TODO Auto-generated method stub
						
					}
		
					@Override
					public void onSensorChanged(SensorEvent event) {
						if (location!=null) {
							if (Math.abs(location.getAngle()-a.get())>3) {//变化超过一定的值才会重设角度
								location.setAngle((float)a.get());
							}
							mView.refresh();
						}
						//Point pt=mView.getPosition();
						//mView.moveTo(pt.getX(),pt.getY(),mView.getScale(),0,(float)a.get());
						//System.out.println(a.get());
					}
		        	
		        },100);
		        
//		        UCMapViewListener listener=new UCMapViewListener() {
//		        
//		        			@Override
//		        			public void onMapViewEvent() {
//		        				mView.refresh();
//		        			}
//		        			
//		        		};
//		        		mView.bind(listener);
				
			}else if (currentLon!=0 && currentLat!=0){
				mView.animateTo(500,currentLon,currentLat, mView.getScale());
			}
		} else if ("添加点".equals(menu)) {
			try {
			if ("数据采集".equals(currentTool))
			{
				this.selectLayer(ogr.wkbPoint, new OnClickListener() {

					@Override
					public void onClick(Layer layer) {
						if (curTool!=null)
						{
							curTool.stop();
							curTool=null;
						}
						BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
						AddFeatureTool2 addTool2=new AddFeatureTool2(mView,layer.layer,bd.getBitmap());
						UCFeatureLayer[] snapLayers=new UCFeatureLayer[1];
						snapLayers[0]=layer.layer;
						addTool2.openSnap(snapLayers, 40,true);
						addTool2.start();
						curTool=addTool2;
						layer.flag=true;
					}
					
				});
				
			}
			else
			{
				if (pointLayer.layer==null)
				{
					pointLayer.pathname=GVS.getInstance().mapPath+"临时点.shp";
					File file=new File(pointLayer.pathname);
					if (!file.exists())
					{
						createShp(pointLayer.pathname,"point",ogr.wkbPoint);
					}
					pointLayer.layer=mView.addFeatureLayer(this);
					pointLayer.layer.loadShapefile(pointLayer.pathname, 30, 2, "#FFFF0000", "#FFFF0000",true);
				}
				if (curTool!=null)
				{
					curTool.stop();
					curTool=null;
				}
				BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
				AddFeatureTool2 addTool2=new AddFeatureTool2(mView,pointLayer.layer,bd.getBitmap());
				UCFeatureLayer[] snapLayers=new UCFeatureLayer[1];
				snapLayers[0]=pointLayer.layer;
				addTool2.openSnap(snapLayers, 40,true);
				addTool2.start();
				curTool=addTool2;
				pointLayer.flag=true;
			}
			}catch(Exception e) {
				try {
					PrintWriter p = new PrintWriter(new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/errors.txt"));
					p.println("=== toString() ===");
					p.println(e.toString() + "\n");
					p.println("=== getLocalizedMessage() ===");
					p.println(e.getLocalizedMessage());
					p.println("=== getMessage() ===\n");
					p.println(e.getMessage());
					p.println("=== printStackTrace() ===");
					e.printStackTrace(p);
					p.flush();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		} else if ("添加线".equals(menu)) {
			if ("数据采集".equals(currentTool))
			{
				this.selectLayer(ogr.wkbLineString, new OnClickListener() {

					@Override
					public void onClick(Layer layer) {
						if (curTool!=null)
						{
							curTool.stop();
							curTool=null;
						}
						BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
						AddFeatureTool2 addTool2=new AddFeatureTool2(mView,layer.layer,bd.getBitmap());
						UCFeatureLayer[] snapLayers=new UCFeatureLayer[1];
						snapLayers[0]=layer.layer;
						addTool2.openSnap(snapLayers, 40,true);
						addTool2.start();
						curTool=addTool2;
						layer.flag=true;
					}
					
				});
				
			}
			else
			{
				if (lineLayer.layer==null)
				{
					lineLayer.pathname=GVS.getInstance().mapPath+"临时线.shp";
					File file=new File(lineLayer.pathname);
					if (!file.exists())
					{
						createShp(lineLayer.pathname,"line",ogr.wkbLineString);
					}
					lineLayer.layer=mView.addFeatureLayer(this);
					lineLayer.layer.loadShapefile(lineLayer.pathname, 30, 2, "#FFFF0000", "#00000000",true);
				}
				if (curTool!=null)
				{
					curTool.stop();
					curTool=null;
				}
				BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
				AddFeatureTool2 addTool2=new AddFeatureTool2(mView,lineLayer.layer,bd.getBitmap());
				UCFeatureLayer[] snapLayers=new UCFeatureLayer[1];
				snapLayers[0]=lineLayer.layer;
				addTool2.openSnap(snapLayers, 40,true);
				addTool2.start();
				curTool=addTool2;
				lineLayer.flag=true;
			}
		} else if ("添加面".equals(menu)) {
			if ("数据采集".equals(currentTool))
			{
				this.selectLayer(ogr.wkbPolygon, new OnClickListener() {

					@Override
					public void onClick(Layer layer) {
						if (curTool!=null)
						{
							curTool.stop();
							curTool=null;
						}
						BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
						AddFeatureTool2 addTool2=new AddFeatureTool2(mView,layer.layer,bd.getBitmap());
						UCFeatureLayer[] snapLayers=new UCFeatureLayer[1];
						snapLayers[0]=layer.layer;
						addTool2.openSnap(snapLayers, 40,true);
						addTool2.start();
						curTool=addTool2;
						layer.flag=true;
					}
					
				});
				
			}
			else
			{
				if (polygonLayer.layer==null)
				{
					polygonLayer.pathname=GVS.getInstance().mapPath+"临时面.shp";
					File file=new File(polygonLayer.pathname);
					if (!file.exists())
					{
						createShp(polygonLayer.pathname,"polygon",ogr.wkbPolygon);
					}
					polygonLayer.layer=mView.addFeatureLayer(this);
					polygonLayer.layer.loadShapefile(polygonLayer.pathname, 30, 2, "#FFFF0000", "#00000000",true);
				}
				if (curTool!=null)
				{
					curTool.stop();
					curTool=null;
				}
				BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
				AddFeatureTool2 addTool2=new AddFeatureTool2(mView,polygonLayer.layer,bd.getBitmap());
				UCFeatureLayer[] snapLayers=new UCFeatureLayer[1];
				snapLayers[0]=polygonLayer.layer;
				addTool2.openSnap(snapLayers, 40,true);
				addTool2.start();
				curTool=addTool2;
				
				polygonLayer.flag=true;
			}
		} else if ("添加点(打点)".equals(menu)) {
			this.selectLayer(ogr.wkbPoint, new OnClickListener() {

				@Override
				public void onClick(Layer layer) {
					if (curTool!=null)
					{
						curTool.stop();
						curTool=null;
					}
					BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
					BitmapDrawable bd2=(BitmapDrawable) getResources().getDrawable(R.drawable.cross);
					AddFeatureTool addTool2=new AddFeatureTool(mView,layer.layer,bd.getBitmap(),bd2.getBitmap());
					UCFeatureLayer[] snapLayers=new UCFeatureLayer[1];
					snapLayers[0]=layer.layer;
					addTool2.openSnap(snapLayers, 40,true);
					addTool2.start();
					curTool=addTool2;
					layer.flag=true;
				}
				
			});
		} else if ("添加线(打点)".equals(menu)) {
			this.selectLayer(ogr.wkbLineString, new OnClickListener() {

				@Override
				public void onClick(Layer layer) {
					if (curTool!=null)
					{
						curTool.stop();
						curTool=null;
					}
					BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
					BitmapDrawable bd2=(BitmapDrawable) getResources().getDrawable(R.drawable.cross);
					AddFeatureTool addTool2=new AddFeatureTool(mView,layer.layer,bd.getBitmap(),bd2.getBitmap());
					UCFeatureLayer[] snapLayers=new UCFeatureLayer[1];
					snapLayers[0]=layer.layer;
					addTool2.openSnap(snapLayers, 40,true);
					addTool2.start();
					curTool=addTool2;
					layer.flag=true;
				}
				
			});
		} else if ("添加面(打点)".equals(menu)) {
			this.selectLayer(ogr.wkbPolygon, new OnClickListener() {

				@Override
				public void onClick(Layer layer) {
					if (curTool!=null)
					{
						curTool.stop();
						curTool=null;
					}
					BitmapDrawable bd=(BitmapDrawable) getResources().getDrawable(R.drawable.marker_poi);
					BitmapDrawable bd2=(BitmapDrawable) getResources().getDrawable(R.drawable.cross);
					AddFeatureTool addTool2=new AddFeatureTool(mView,layer.layer,bd.getBitmap(),bd2.getBitmap());
					UCFeatureLayer[] snapLayers=new UCFeatureLayer[1];
					snapLayers[0]=layer.layer;
					addTool2.openSnap(snapLayers, 40,true);
					addTool2.start();
					curTool=addTool2;
					layer.flag=true;
				}
				
			});
		} else if ("删除".equals(menu)) {
			if (curTool!=null)
			{
				curTool.stop();
				curTool=null;
			}
			Vector<UCFeatureLayer> v=new Vector<UCFeatureLayer>();
			if ("数据采集".equals(currentTool)) {//只有数据采集模式下，才能删除图斑
				int size=layers.size();
				for (int i1=0;i1<size;++i1)
				//for (Layer layer:layers)
				{
					v.add(layers.get(i1).layer);
				}
			}
			if (pointLayer!=null) v.add(pointLayer.layer);
			if (lineLayer!=null) v.add(lineLayer.layer);
			if (polygonLayer!=null) v.add(polygonLayer.layer);
			DeleteFeatureTool tool=new DeleteFeatureTool(mView,v);
			curTool=tool;
		} else if ("编辑属性".equals(menu)) {
			if (curTool!=null)
			{
				curTool.stop();
				curTool=null;
			}
			Vector<UCFeatureLayer> v=new Vector<UCFeatureLayer>();
			if ("数据采集".equals(currentTool))  {//数据采集模式
				int size=layers.size();
				for (int i1=0;i1<size;++i1)
				//for (Layer layer:layers)
				{
					v.add(layers.get(i1).layer);
				}
			}
			if (pointLayer!=null) v.add(pointLayer.layer);
			if (lineLayer!=null) v.add(lineLayer.layer);
			if (polygonLayer!=null) v.add(polygonLayer.layer);
			EditFeatureAttTool tool=new EditFeatureAttTool(mView,new Handler() {
				public void handleMessage(Message msg) 
				{
					if (msg.what==1)
					{
						EditFeatureAttTool efaTool=(EditFeatureAttTool)msg.obj;
						int fieldCount=efaTool.layer.getFieldCount()-1;
						fields=new String[fieldCount];
						values=new String[fieldCount];
						for (int i=0;i<fieldCount;++i)
						{
							Field f=efaTool.layer.getField(i+1);
							fields[i]=f.name();
							Object value=efaTool.feature.get(f.name());
							if (value!=null)
							{
								if (f.type()==Byte.class)
									values[i]=Byte.toString((Byte)value);
								else if (f.type()==Short.class)
									values[i]=Short.toString((Short)value);
								else if (f.type()==Integer.class)
									values[i]=Integer.toString((Integer)value);
								else if (f.type()==Long.class)
									values[i]=Long.toString((Long)value);
								else if (f.type()==Float.class)
									values[i]=Float.toString((Float)value);
								else if (f.type()==Double.class)
									values[i]=Double.toString((Double)value);
								else if (f.type()==java.sql.Date.class)
								{
									SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									values[i]=format.format((java.sql.Date)value);
								}
								else if (f.type()==java.sql.Time.class)
								{
									SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
									values[i]=format.format((java.sql.Time)value);
								}
								else if (f.type()==String.class)
									values[i]=(String)value;
							}
							else
							{
								if (f.type()==String.class)
									values[i]="";
								else if (f.type()==java.sql.Date.class)
								{
									values[i]="";
								}
								else if (f.type()==java.sql.Time.class)
								{
									values[i]="";
								}
								else
									values[i]="0";
							}
						}
						ModifyFeatureInfo.show(MainActivity.this, efaTool.feature, fields, values, efaTool.layer);
					}
				}
			},v);
			curTool=tool;
		} else if ("编辑图形".equals(menu)) {
			if (curTool!=null)
			{
				curTool.stop();
				curTool=null;
			}
			Vector<UCFeatureLayer> v=new Vector<UCFeatureLayer>();
			if ("数据采集".equals(currentTool))  {//只有数据采集模式下，才能删除图斑
				int size=layers.size();
				for (int i1=0;i1<size;++i1)
				//for (Layer layer:layers)
				{
					v.add(layers.get(i1).layer);
				}
			}
			if (pointLayer!=null) v.add(pointLayer.layer);
			if (lineLayer!=null) v.add(lineLayer.layer);
			if (polygonLayer!=null) v.add(polygonLayer.layer);
			EditFeatureTool tool=new EditFeatureTool(mView,v);
			curTool=tool;
		} else if ("撤销".equals(menu)) {
			UndoRedo.getInstance().undo();
			mView.getMaskLayer().clear();
			mView.refresh();
		} else if ("重做".equals(menu)) {
			UndoRedo.getInstance().redo();
			mView.getMaskLayer().clear();
			mView.refresh();
		} else if ("裁切".equals(menu)) {
			if ("数据采集".equals(currentTool))
			{
				this.selectLayer(ogr.wkbPolygon, new OnClickListener() {

					@Override
					public void onClick(Layer layer) {
						if (curTool!=null)
						{
							curTool.stop();
							curTool=null;
						}
						CutTool tool=new CutTool(mView,layer.layer);
						curTool=tool;
					}
					
				});
				
			}
			else if (polygonLayer!=null)
			{
				if (curTool!=null)
				{
					curTool.stop();
					curTool=null;
				}
				CutTool tool=new CutTool(mView,polygonLayer.layer);
				curTool=tool;
			}
		} else if ("开启捕捉".equals(menu)) {
			
		} else if ("关闭捕捉".equals(menu)) {
			
		} else if ("选择shp".equals(menu)) {
			this.selectShp(Environment.getExternalStorageDirectory().getPath());
		} else if ("选择影像".equals(menu)) {
			this.selectMbtiles(Environment.getExternalStorageDirectory().getPath());
		} else if ("记录轨迹".equals(menu) || "停止记录".equals(menu)) {
			if (routeThread!=null && routePoints!=null) {
				ma.setDates(new String[]{"保存数据",/*"图层控制", */"地图搜索",/* "文件管理", */"选择shp","选择影像","查看属性", "测距", "测面积","定位","记录轨迹","加载轨迹","路径分析"/*, "分屏", "卷帘", "涂鸦", "统计", "天地图"*/},
			            new int[]{R.drawable.toolbar,/*R.drawable.tckz, */R.drawable.dtss, /*R.drawable.wjgl, */R.drawable.ty,R.drawable.yx,R.drawable.cksx, R.drawable.cj, R.drawable.cmj,R.drawable.dw,R.drawable.gpx,R.drawable.layer_output,R.drawable.add_polyline/*, R.drawable.fp, R.drawable.jl, R.drawable.ty, R.drawable.tj, R.drawable.tdt*/});
                ma.notifyDataSetChanged();
                if (routePoints.size()<2) {
                	BluToast.makeText(MainActivity.this, "路径点个数小于2", BluToast.LENGTH_SHORT).show();
                	if(mWakeLock != null&&mWakeLock.isHeld()) {
    			        mWakeLock.release();
    			    }
                	return;
                }

                if(mWakeLock != null&&mWakeLock.isHeld()) {
			        mWakeLock.release();
			    }

				routeFlag=true;
				GPXParser parser = new GPXParser();
				GPX gpx=new GPX();
				Track track=new Track();
				track.addTrackPoints(routePoints);
				gpx.addTrack(track);
				try {
					String date = new TimeString().getTimeString();
					parser.writeGPX(gpx, new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/"+date+".gpx"));
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransformerException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				
				if (locationFlag==false)
				{
					BluToast.makeText(MainActivity.this, "未定位，请先开启定位", BluToast.LENGTH_SHORT).show();
				} else {
					mWakeLock.acquire();
					ma.setDates(new String[]{"保存数据",/*"图层控制", */"地图搜索",/* "文件管理", */"选择shp","选择影像","查看属性", "测距", "测面积","定位","停止记录","加载轨迹","路径分析"/*, "分屏", "卷帘", "涂鸦", "统计", "天地图"*/},
				            new int[]{R.drawable.toolbar,/*R.drawable.tckz, */R.drawable.dtss, /*R.drawable.wjgl, */R.drawable.ty,R.drawable.yx,R.drawable.cksx, R.drawable.cj, R.drawable.cmj,R.drawable.dw,R.drawable.gpx,R.drawable.layer_output,R.drawable.add_polyline/*, R.drawable.fp, R.drawable.jl, R.drawable.ty, R.drawable.tj, R.drawable.tdt*/});
	                ma.notifyDataSetChanged();
					routeFlag=false;
					routePoints=new ArrayList<Waypoint>();
					routeThread=new Thread(new Runnable() {
		
						@Override
						public void run() {
							while (routeFlag!=true)
							{
								routePoints.add(new Waypoint("",(float)currentLat,(float)currentLon));
								int size=routePoints.size();
								if (size>1) {
									Coordinate[] coords=new Coordinate[size];
									for (int i1=0;i1<size;++i1)
									{
										coords[i1]=new Coordinate(routePoints.get(i1).getLongitude(),routePoints.get(i1).getLatitude());
									}
									if (route==null) {
										route=gf.createLineString(coords);
										vlayer.addLine(route, 3, 0xFF00FF00);
									} else {
										LineString newgeo=gf.createLineString(coords);
										vlayer.updateGeometry(route, newgeo);
										route=newgeo;
									}
								}
								try {
									for (int i=0;i<150;i++)//停15秒
									{
										if (routeFlag==true) return;
										Thread.sleep(100);
									}
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						
					});
					routeThread.start();
				}
			}
		} else if ("加载轨迹".equals(menu)) {
//			GPXParser parser = new GPXParser();
//			try {
//				GPX gpx = parser.parseGPX(new FileInputStream(Environment.getExternalStorageDirectory().getPath()+"/gpx_testxxx.gpx"));
//				Set<Track> tracks = gpx.getTracks();
//				int size=tracks.size();
//				ArrayList<Waypoint> points=tracks.iterator().next().getTrackPoints();
//				if (vlayer==null) vlayer=mView.addVectorLayer();
//				size=points.size();
//				Coordinate[] coords=new Coordinate[size];
//				for (int i1=0;i1<size;++i1)
//				{
//					coords[i1]=new Coordinate(points.get(i1).getLongitude(),points.get(i1).getLatitude());
//				}
//				vlayer.addLine(gf.createLineString(coords), 3, 0xFF00FF00);
//				mView.moveTo(points.get(0).getLongitude(),points.get(0).getLatitude(), mView.getScale());
//				mView.postDelayed(new Runnable() {
//					@Override
//					public void run() {
//						mView.refresh();
//					}
//				}, 0);
//			} catch (FileNotFoundException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (Exception e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			selectGPX(Environment.getExternalStorageDirectory().getPath());
		} else if ("路径分析".equals(menu)) {
			if (curTool!=null)
			{
				curTool.stop();
				curTool=null;
			}
			Bitmap start=BitmapFactory.decodeResource(getResources(),R.drawable.start);
			Bitmap end=BitmapFactory.decodeResource(getResources(),R.drawable.end);
			PathAnalysisTool paTool=new PathAnalysisTool(mView,start,end);
			paTool.start();
			curTool=paTool;
		}
	}
	
	private void createShp(String shpPath,String name,int geomType) {

        ogr.RegisterAll();
        gdal.SetConfigOption("GDAL_FILENAME_IS_UTF8", "NO");
        gdal.SetConfigOption("SHAPE_ENCODING", "UTF-8");

        String strDriverName = "ESRI Shapefile";
        org.gdal.ogr.Driver oDriver = ogr.GetDriverByName(strDriverName);
        if (oDriver == null) {
            System.out.println(strDriverName + " 驱动不可用！\n");
            return;
        }
        //String shpPath=Environment.getExternalStorageDirectory().getPath() + "/geodata/utf8.shp";
        DataSource oDS = oDriver.CreateDataSource(shpPath, null);
        if (oDS == null) {
            System.out.println("创建矢量文件【" + shpPath + "】失败！\n");
            return;
        }

        org.gdal.ogr.Layer oLayer = oDS.CreateLayer(name, null, geomType, null);
        if (oLayer == null) {
            System.out.println("图层创建失败！\n");
            return;
        }

        // 下面创建属性表
        // 先创建一个叫FieldID的整型属性
        FieldDefn oFieldID = new FieldDefn("FieldID", ogr.OFTInteger);
        int ret=oLayer.CreateField(oFieldID);

        // 再创建一个叫FeatureName的字符型属性，字符长度为50
        FieldDefn oFieldName = new FieldDefn("photo", ogr.OFTString);
        oFieldName.SetWidth(100);
        ret=oLayer.CreateField(oFieldName);
        oFieldName = new FieldDefn("video", ogr.OFTString);
        oFieldName.SetWidth(100);
        ret=oLayer.CreateField(oFieldName);
        oFieldName = new FieldDefn("voice", ogr.OFTString);
        oFieldName.SetWidth(100);
        ret=oLayer.CreateField(oFieldName);

        FeatureDefn oDefn = oLayer.GetLayerDefn();

        org.gdal.ogr.Feature oFeatureTriangle = new org.gdal.ogr.Feature(oDefn);
        oFeatureTriangle.SetField(0, 0);
//        oFeatureTriangle.SetField(1, Base64Utils.encodeStr("三角形11"));
        oFeatureTriangle.SetField(1, "polgyon");
        org.gdal.ogr.Geometry geo = null;
        switch (geomType)
        {
        case ogr.wkbPoint:
        	geo=org.gdal.ogr.Geometry.CreateFromWkt("POINT (0 0)");
        	break;
        case ogr.wkbLineString:
        	geo=org.gdal.ogr.Geometry.CreateFromWkt("LINESTRING (0 0,20 0,10 15,0 0)");
        	break;
        case ogr.wkbPolygon:
        	geo=org.gdal.ogr.Geometry.CreateFromWkt("POLYGON ((0 0,20 0,10 15,0 0))");
        	break;
        }
        
        oFeatureTriangle.SetGeometry(geo);
        ret=oLayer.CreateFeature(oFeatureTriangle);
        oLayer.DeleteFeature(ret);

//        // 创建矩形要素
//        org.gdal.ogr.Feature oFeatureRectangle = new org.gdal.ogr.Feature(oDefn);
//        oFeatureRectangle.SetField(0, 1);
//        oFeatureRectangle.SetField(1, "矩形222");
//        Geometry geomRectangle = Geometry.CreateFromWkt("POLYGON ((30 0,60 0,60 30,30 30,30 0))");
//        oFeatureRectangle.SetGeometry(geomRectangle);
//        oLayer.CreateFeature(oFeatureRectangle);
//
//        // 创建五角形要素
//        org.gdal.ogr.Feature oFeaturePentagon = new org.gdal.ogr.Feature(oDefn);
//        oFeaturePentagon.SetField(0, 2);
//        oFeaturePentagon.SetField(1, "五角形33");
//        Geometry geomPentagon = Geometry.CreateFromWkt("POLYGON ((70 0,85 0,90 15,80 30,65 15,70 0))");
//        oFeaturePentagon.SetGeometry(geomPentagon);
//        oLayer.CreateFeature(oFeaturePentagon);

        try {
            oLayer.SyncToDisk();
            oDS.SyncToDisk();
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("\n数据集创建完成！\n");
    }
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, final Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            File file = new File(GVS.getInstance().vp);
            if (resultCode == 0) {
                if (file.exists()) {
                    file.delete();
                }
            } else {
                if (file.exists()) {
                    GVS.getInstance().pa.addPhoto(file.getName());
                }
            }
        } else if (requestCode == 3) {
            File file = new File(GVS.getInstance().vp);
            if (resultCode == 0) {
                if (file.exists()) {
                    file.delete();
                }
            } else {
                if (file.exists()) {
                    GVS.getInstance().videos.add(file.getName());
                    GVS.getInstance().tv_video.setText(file.getName());
                }
            }
        } else {

        }
    }
	
	public abstract static interface OnClickListener 
	{
		public abstract void onClick(Layer layer);
	}
	
	private void selectLayer(int geomType,final OnClickListener listener)
	{
		Vector<String> layerNames=new Vector<String>();
		final Vector<Layer> curLayers=new Vector<Layer>();
		int size=layers.size();
		for (int i1=0;i1<size;++i1)
		//for (Layer layer:layers)
		{
			Layer layer=layers.get(i1);
			int type=layer.layer.getGeometryType();
			if (type==geomType || (type-3)==geomType) { //-3是用来兼容Mutli类型的geometry
				layerNames.add(layer.layer.getName());
				curLayers.add(layer);
			}
		}
		size=layerNames.size();
		if (size>0)
		{
			String[] list=new String[size];
			layerNames.copyInto(list);
			AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
	        builder.setIcon(R.mipmap.ic_launcher);
	        builder.setTitle("请选择图层:");
	        builder.setSingleChoiceItems(list, -1,new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					listener.onClick(curLayers.elementAt(which));
				}
			});
			builder.create().show();
		}
		else {
			String text="";
			switch (geomType) {
			case ogr.wkbPoint:
				text="没有点图层";break;
			case ogr.wkbLineString:
				text="没有线图层";break;
			case ogr.wkbPolygon:
				text="没有面图层";break;
			}
			BluToast.makeText(MainActivity.this, text, BluToast.LENGTH_SHORT).show();
		}
	}
	
	private void selectGPX(String path)
	{
		//遍历存储卡中的一级文件夹，找出有哪些mbtiles文件
		final Vector<String> mbs=new Vector<String>();
		Vector<String> mbNames=new Vector<String>();
		//String path=Environment.getExternalStorageDirectory().getPath();
		File dir = new File(path);
        File file[] = dir.listFiles();
        
        //将shp按照最后修改时间排序
        Arrays.sort(file,new Comparator<File>(){

			@Override
			public int compare(File f1, File f2) {
				return f1.lastModified()<f2.lastModified()?1:-1;
			}
        	
        });
        
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile())
            {
            	String pathname=file[i].getAbsolutePath();
            	if (pathname.endsWith("gpx") || pathname.endsWith("GPX"))
            	{
            		mbs.add(pathname);
            		mbNames.add(file[i].getName());
            	}
            }
        }
        int size=mbNames.size();
        if (size==0) return;
        //显示一个对话框，供用户选择地图
        String[] list=new String[size];
        mbNames.copyInto(list);
        
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("请选择GPX:");
        builder.setSingleChoiceItems(list, -1, new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
				GPXParser parser = new GPXParser();
				try {
					GPX gpx = parser.parseGPX(new FileInputStream(mbs.elementAt(which)));
					Set<Track> tracks = gpx.getTracks();
					int size=tracks.size();
					ArrayList<Waypoint> points=tracks.iterator().next().getTrackPoints();
					if (route!=null) vlayer.remove(route);
					route=null;
					size=points.size();
					if (size<2)
					{
						BluToast.makeText(MainActivity.this, "路径点个数小于2", BluToast.LENGTH_SHORT).show();
						return;
					}
					Coordinate[] coords=new Coordinate[size];
					for (int i1=0;i1<size;++i1)
					{
						coords[i1]=new Coordinate(points.get(i1).getLongitude(),points.get(i1).getLatitude());
					}
					route=gf.createLineString(coords);
					vlayer.addLine(route, 3, 0xFF00FF00);
					mView.moveTo(points.get(0).getLongitude(),points.get(0).getLatitude(), mView.getScale());
					mView.postDelayed(new Runnable() {
						@Override
						public void run() {
							mView.refresh();
						}
					}, 0);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		builder.create().show();
	}
	
	private void selectMbtiles(String path)
	{
		//遍历存储卡中的一级文件夹，找出有哪些mbtiles文件
		mbs.clear();
		mbNames.clear();
		//String path=Environment.getExternalStorageDirectory().getPath();
		File dir = new File(path);
        File file[] = dir.listFiles();
        
        //将shp按照名称排序
        Arrays.sort(file,new Comparator<File>(){

			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
        	
        });
        
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile())
            {
            	String pathname=file[i].getAbsolutePath();
            	if (pathname.endsWith("mbtiles") || pathname.endsWith("MBTILES"))
            	{
            		mbs.add(pathname);
            		mbNames.add(file[i].getName());
            	}
            }
        }
        int size=mbNames.size();
        if (size==0) return;
        //显示一个对话框，供用户选择地图
        String[] list=new String[size];
        mbNames.copyInto(list);
        final boolean[] checkedItems=new boolean[size];
        for (int i=0;i<size;++i)
        {
        	for (int j=0;j<mbLayers.size();++j)
        	{
        		if (mbLayers.get(j).pathname.equalsIgnoreCase(mbs.get(i)))
        		{
        			checkedItems[i]=true;
        			break;
        		}
        	}
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("请选择影像:");
        builder.setMultiChoiceItems(list, checkedItems,new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				checkedItems[which]=isChecked;//保存用户的选择
			}
		});
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				//关闭之前的图层
				int size=mbLayers.size();
				for (int i=0;i<size;++i)
				//for (Layer layer:mbLayers)
				{
					Layer layer=mbLayers.get(i);
					if (layer.rlayer!=null) mView.deleteLayer(layer.rlayer);
				}
				mbLayers.clear();
				size=checkedItems.length;
				for (int i=0;i<size;++i)
				{//重新设置每个图层的可见性
					if (checkedItems[i]) {
						Layer layer=new Layer();
						layer.pathname=mbs.elementAt(i);
						layer.rlayer=mView.addMbtiesLayer(layer.pathname, 0, 20);
						mbLayers.add(layer);
						mView.moveLayer(layer.rlayer, 1);//移动到第二层位置上
					}
				}
				TextView tv_qt = (TextView) findViewById(R.id.tv_main_qt);
				tv_qt.performClick();
//						mView.refresh();//刷新地图立即看到效果
			}
		});
        builder.setNegativeButton("取消", null);//取消按钮什么事都不做
		builder.create().show();
	}
	
	private void selectShp(String path)
	{
		//遍历存储卡中的一级文件夹，找出有哪些shp文件
		shps.clear();
		shpNames.clear();
		//String path=Environment.getExternalStorageDirectory().getPath();
		File dir = new File(path);
        File file[] = dir.listFiles();
        
        //将shp按照名称排序
        Arrays.sort(file,new Comparator<File>(){

			@Override
			public int compare(File f1, File f2) {
				return f1.getName().compareToIgnoreCase(f2.getName());
			}
        	
        });
        
        for (int i = 0; i < file.length; i++) {
            if (file[i].isFile())
            {
            	String pathname=file[i].getAbsolutePath();
            	if (pathname.endsWith("shp") || pathname.endsWith("SHP"))
            	{
            		shps.add(pathname);
            		shpNames.add(file[i].getName());
            	}
            }
        }
        int size=shpNames.size();
        if (size==0) return;
        //显示一个对话框，供用户选择地图
        String[] list=new String[size];
        shpNames.copyInto(list);
        final boolean[] checkedItems=new boolean[size];
        for (int i=0;i<size;++i)
        {
        	for (int j=0;j<layers.size();++j)
        	{
        		if (layers.get(j).pathname.equalsIgnoreCase(shps.get(i)))
        		{
        			checkedItems[i]=true;
        			break;
        		}
        	}
        }
        
        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setIcon(R.mipmap.ic_launcher);
        builder.setTitle("请选择SHP:");
        builder.setMultiChoiceItems(list, checkedItems,new DialogInterface.OnMultiChoiceClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int which, boolean isChecked) {
				checkedItems[which]=isChecked;//保存用户的选择
			}
		});
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
			
			@Override
			public void onClick(DialogInterface dialog, int arg1) {
				//关闭之前的图层
				int size=layers.size();
				for (int i1=0;i1<size;++i1)
				//for (Layer layer:layers)
				{
					Layer layer=layers.get(i1);
					if (layer.layer!=null) mView.deleteLayer(layer.layer);
				}
				layers.clear();
				size=checkedItems.length;
				for (int i=0;i<size;++i)
				{//重新设置每个图层的可见性
					if (checkedItems[i]) {
						Layer layer=new Layer();
						layer.layer=mView.addFeatureLayer(MainActivity.this);
						layer.pathname=shps.elementAt(i);
						layer.layer.loadShapefile(layer.pathname,true);
						if (layer.layer.getGeometryType()==ogr.wkbPoint || layer.layer.getGeometryType()==ogr.wkbMultiPoint)
							layer.layer.setStyle(30, 2, "#FFFF0000", "#FFFF0000");
						else
							layer.layer.setStyle(30, 2, "#FFFF0000", "#00000000");
						layers.add(layer);
						mView.moveLayer(layer.layer, 1);//移动到第二层位置上
					}
				}
				TextView tv_qt = (TextView) findViewById(R.id.tv_main_qt);
				tv_qt.performClick();
//				mView.refresh();//刷新地图立即看到效果
			}
		});
        builder.setNegativeButton("取消", null);//取消按钮什么事都不做
		builder.create().show();
	}
	

}
