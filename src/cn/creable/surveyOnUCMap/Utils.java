package cn.creable.surveyOnUCMap;

import com.vividsolutions.jts.geom.Coordinate;
import com.vividsolutions.jts.geom.Envelope;
import com.vividsolutions.jts.geom.Geometry;
import com.vividsolutions.jts.geom.GeometryFactory;
import com.vividsolutions.jts.geom.LineString;
import com.vividsolutions.jts.geom.MultiLineString;
import com.vividsolutions.jts.geom.MultiPoint;
import com.vividsolutions.jts.geom.Point;
import com.vividsolutions.jts.geom.Polygon;

import cn.creable.ucmap.openGIS.GeometryType;

public class Utils {
	
	private static Polygon getCutPolygon(Polygon pg,LineString line1,LineString line2,LineString line)
	{
		if (!line1.intersects(line2) && !line1.intersects(pg) && !line2.intersects(pg))
		{
			Coordinate[] pts=line.getCoordinates();
			Coordinate[] pts1=line1.getCoordinates();
			Coordinate[] pts2=line2.getCoordinates();
			int length=pts.length;
			Coordinate[] points=new Coordinate[length+3];
			System.arraycopy(pts, 0, points, 0, length);
			points[length++]=pts2[1];
			points[length++]=pts1[1];
			points[length++]=points[0];
			GeometryFactory gf=new GeometryFactory();
			return gf.createPolygon(points);
		}
		return null;
	}
	
	private static Polygon makeCutPolygon(Envelope env,Polygon pg,LineString line1,LineString line2,LineString line)
	{
		
		Coordinate[] pts1=line1.getCoordinates();
		Coordinate[] pts2=line2.getCoordinates();
		//左
		pts1[1]=new Coordinate(env.getMinX(),env.getMinY());
		pts2[1]=new Coordinate(env.getMinX(),env.getMaxY());
//		pts1[2]=env.getMinX();pts1[3]=env.getMinY();
//		pts2[2]=env.getMinX();pts2[3]=env.getMaxY();
		GeometryFactory gf=new GeometryFactory();
		line1=gf.createLineString(pts1);
		line2=gf.createLineString(pts2);
		Polygon ret=getCutPolygon(pg,line1,line2,line);
		if (ret!=null) return ret;
		pts1[1]=new Coordinate(env.getMinX(),env.getMaxY());
		pts2[1]=new Coordinate(env.getMinX(),env.getMinY());
//		pts1[2]=env.getXMin();pts1[3]=env.getYMax();
//		pts2[2]=env.getXMin();pts2[3]=env.getYMin();
		line1=gf.createLineString(pts1);
		line2=gf.createLineString(pts2);
		ret=getCutPolygon(pg,line1,line2,line);
		if (ret!=null) return ret;
		//上
		pts1[1]=new Coordinate(env.getMinX(),env.getMinY());
		pts2[1]=new Coordinate(env.getMaxX(),env.getMinY());
//		pts1[2]=env.getXMin();pts1[3]=env.getYMin();
//		pts2[2]=env.getXMax();pts2[3]=env.getYMin();
		line1=gf.createLineString(pts1);
		line2=gf.createLineString(pts2);
		ret=getCutPolygon(pg,line1,line2,line);
		if (ret!=null) return ret;
		pts1[1]=new Coordinate(env.getMaxX(),env.getMinY());
		pts2[1]=new Coordinate(env.getMinX(),env.getMinY());
//		pts1[2]=env.getXMax();pts1[3]=env.getYMin();
//		pts2[2]=env.getXMin();pts2[3]=env.getYMin();
		line1=gf.createLineString(pts1);
		line2=gf.createLineString(pts2);
		ret=getCutPolygon(pg,line1,line2,line);
		if (ret!=null) return ret;
		//右
		pts1[1]=new Coordinate(env.getMaxX(),env.getMinY());
		pts2[1]=new Coordinate(env.getMaxX(),env.getMaxY());
//		pts1[2]=env.getXMax();pts1[3]=env.getYMin();
//		pts2[2]=env.getXMax();pts2[3]=env.getYMax();
		line1=gf.createLineString(pts1);
		line2=gf.createLineString(pts2);
		ret=getCutPolygon(pg,line1,line2,line);
		if (ret!=null) return ret;
		pts1[1]=new Coordinate(env.getMaxX(),env.getMaxY());
		pts2[1]=new Coordinate(env.getMaxX(),env.getMinY());
//		pts1[2]=env.getXMax();pts1[3]=env.getYMax();
//		pts2[2]=env.getXMax();pts2[3]=env.getYMin();
		line1=gf.createLineString(pts1);
		line2=gf.createLineString(pts2);
		ret=getCutPolygon(pg,line1,line2,line);
		if (ret!=null) return ret;
		//下
		pts1[1]=new Coordinate(env.getMinX(),env.getMaxY());
		pts2[1]=new Coordinate(env.getMaxX(),env.getMaxY());
//		pts1[2]=env.getXMin();pts1[3]=env.getYMax();
//		pts2[2]=env.getXMax();pts2[3]=env.getYMax();
		line1=gf.createLineString(pts1);
		line2=gf.createLineString(pts2);
		ret=getCutPolygon(pg,line1,line2,line);
		if (ret!=null) return ret;
		pts1[1]=new Coordinate(env.getMaxX(),env.getMaxY());
		pts2[1]=new Coordinate(env.getMinX(),env.getMaxY());
//		pts1[2]=env.getXMax();pts1[3]=env.getYMax();
//		pts2[2]=env.getXMin();pts2[3]=env.getYMax();
		line1=gf.createLineString(pts1);
		line2=gf.createLineString(pts2);
		ret=getCutPolygon(pg,line1,line2,line);
		if (ret!=null) return ret;
		return null;
	}
	
//	private static Polygon makeCutPolygon(Envelope env,Polygon pg,LineString line1,LineString line2,LineString line)
//	{
//		
//		Coordinate[] pts1=line1.getCoordinates();
//		Coordinate[] pts2=line2.getCoordinates();
//		//左
//		pts1[1]=new Coordinate(env.getMinX(),env.getMinY());
//		pts2[1]=new Coordinate(env.getMinX(),env.getMaxY());
////		pts1[2]=env.getMinX();pts1[3]=env.getMinY();
////		pts2[2]=env.getMinX();pts2[3]=env.getMaxY();
//		GeometryFactory gf=new GeometryFactory();
//		line1=gf.createLineString(pts1);
//		line2=gf.createLineString(pts2);
//		boolean flag;
//		flag=false;
//		if (!line1.intersects(line2) && !line1.intersects(pg) && !line2.intersects(pg))
//		{
//			if (line1.intersects(pg))
//			{
//				Geometry geo=line1.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			if (flag==true && line2.intersects(pg))
//			{
//				Geometry geo=line2.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			if (flag)
//			{
//				Coordinate[] pts=line.getCoordinates();
//				int length=pts.length;
//				Coordinate[] points=new Coordinate[length+3];
//				System.arraycopy(pts, 0, points, 0, length);
//				points[length++]=pts2[1];
//				points[length++]=pts1[1];
//				points[length++]=points[0];
//				return gf.createPolygon(points);
//			}
//		}
//		pts1[1]=new Coordinate(env.getMinX(),env.getMaxY());
//		pts2[1]=new Coordinate(env.getMinX(),env.getMinY());
////		pts1[2]=env.getXMin();pts1[3]=env.getYMax();
////		pts2[2]=env.getXMin();pts2[3]=env.getYMin();
//		line1=gf.createLineString(pts1);
//		line2=gf.createLineString(pts2);
//		if (!line1.intersects(line2))
//		{
//			if (line1.intersects(pg))
//			{
//				Geometry geo=line1.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			if (flag==true && line2.intersects(pg))
//			{
//				Geometry geo=line2.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			
//			if (flag)
//			{
//				Coordinate[] pts=line.getCoordinates();
//				int length=pts.length;
//				Coordinate[] points=new Coordinate[length+3];
//				System.arraycopy(pts, 0, points, 0, length);
//				points[length++]=pts2[1];
//				points[length++]=pts1[1];
//				points[length++]=points[0];
//				return gf.createPolygon(points);
//			}
//		}
//		//上
//		pts1[1]=new Coordinate(env.getMinX(),env.getMinY());
//		pts2[1]=new Coordinate(env.getMaxX(),env.getMinY());
////		pts1[2]=env.getXMin();pts1[3]=env.getYMin();
////		pts2[2]=env.getXMax();pts2[3]=env.getYMin();
//		line1=gf.createLineString(pts1);
//		line2=gf.createLineString(pts2);
//		if (!line1.intersects(line2) && !line1.intersects(pg) && !line2.intersects(pg))
//		{
//			if (line1.intersects(pg))
//			{
//				Geometry geo=line1.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			if (flag==true && line2.intersects(pg))
//			{
//				Geometry geo=line2.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			
//			if (flag)
//			{
//				Coordinate[] pts=line.getCoordinates();
//				int length=pts.length;
//				Coordinate[] points=new Coordinate[length+3];
//				System.arraycopy(pts, 0, points, 0, length);
//				points[length++]=pts2[1];
//				points[length++]=pts1[1];
//				points[length++]=points[0];
//				return gf.createPolygon(points);
//			}
//		}
//		pts1[1]=new Coordinate(env.getMaxX(),env.getMinY());
//		pts2[1]=new Coordinate(env.getMinX(),env.getMinY());
////		pts1[2]=env.getXMax();pts1[3]=env.getYMin();
////		pts2[2]=env.getXMin();pts2[3]=env.getYMin();
//		line1=gf.createLineString(pts1);
//		line2=gf.createLineString(pts2);
//		if (!line1.intersects(line2) && !line1.intersects(pg) && !line2.intersects(pg))
//		{
//			if (line1.intersects(pg))
//			{
//				Geometry geo=line1.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			if (flag==true && line2.intersects(pg))
//			{
//				Geometry geo=line2.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			
//			if (flag)
//			{
//				Coordinate[] pts=line.getCoordinates();
//				int length=pts.length;
//				Coordinate[] points=new Coordinate[length+3];
//				System.arraycopy(pts, 0, points, 0, length);
//				points[length++]=pts2[1];
//				points[length++]=pts1[1];
//				points[length++]=points[0];
//				return gf.createPolygon(points);
//			}
//		}
//		//右
//		pts1[1]=new Coordinate(env.getMaxX(),env.getMinY());
//		pts2[1]=new Coordinate(env.getMaxX(),env.getMaxY());
////		pts1[2]=env.getXMax();pts1[3]=env.getYMin();
////		pts2[2]=env.getXMax();pts2[3]=env.getYMax();
//		line1=gf.createLineString(pts1);
//		line2=gf.createLineString(pts2);
//		if (!line1.intersects(line2) && !line1.intersects(pg) && !line2.intersects(pg))
//		{
//			if (line1.intersects(pg))
//			{
//				Geometry geo=line1.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			if (flag==true && line2.intersects(pg))
//			{
//				Geometry geo=line2.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			
//			if (flag)
//			{
//				Coordinate[] pts=line.getCoordinates();
//				int length=pts.length;
//				Coordinate[] points=new Coordinate[length+3];
//				System.arraycopy(pts, 0, points, 0, length);
//				points[length++]=pts2[1];
//				points[length++]=pts1[1];
//				points[length++]=points[0];
//				return gf.createPolygon(points);
//			}
//		}
//		pts1[1]=new Coordinate(env.getMaxX(),env.getMaxY());
//		pts2[1]=new Coordinate(env.getMaxX(),env.getMinY());
////		pts1[2]=env.getXMax();pts1[3]=env.getYMax();
////		pts2[2]=env.getXMax();pts2[3]=env.getYMin();
//		line1=gf.createLineString(pts1);
//		line2=gf.createLineString(pts2);
//		if (!line1.intersects(line2) && !line1.intersects(pg) && !line2.intersects(pg))
//		{
//			if (line1.intersects(pg))
//			{
//				Geometry geo=line1.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			if (flag==true && line2.intersects(pg))
//			{
//				Geometry geo=line2.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			
//			if (flag)
//			{
//				Coordinate[] pts=line.getCoordinates();
//				int length=pts.length;
//				Coordinate[] points=new Coordinate[length+3];
//				System.arraycopy(pts, 0, points, 0, length);
//				points[length++]=pts2[1];
//				points[length++]=pts1[1];
//				points[length++]=points[0];
//				return gf.createPolygon(points);
//			}
//		}
//		//下
//		pts1[1]=new Coordinate(env.getMinX(),env.getMaxY());
//		pts2[1]=new Coordinate(env.getMaxX(),env.getMaxY());
////		pts1[2]=env.getXMin();pts1[3]=env.getYMax();
////		pts2[2]=env.getXMax();pts2[3]=env.getYMax();
//		line1=gf.createLineString(pts1);
//		line2=gf.createLineString(pts2);
//		if (!line1.intersects(line2) && !line1.intersects(pg) && !line2.intersects(pg))
//		{
//			if (line1.intersects(pg))
//			{
//				Geometry geo=line1.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			if (flag==true && line2.intersects(pg))
//			{
//				Geometry geo=line2.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			
//			if (flag)
//			{
//				Coordinate[] pts=line.getCoordinates();
//				int length=pts.length;
//				Coordinate[] points=new Coordinate[length+3];
//				System.arraycopy(pts, 0, points, 0, length);
//				points[length++]=pts2[1];
//				points[length++]=pts1[1];
//				points[length++]=points[0];
//				return gf.createPolygon(points);
//			}
//		}
//		pts1[1]=new Coordinate(env.getMaxX(),env.getMaxY());
//		pts2[1]=new Coordinate(env.getMinX(),env.getMaxY());
////		pts1[2]=env.getXMax();pts1[3]=env.getYMax();
////		pts2[2]=env.getXMin();pts2[3]=env.getYMax();
//		line1=gf.createLineString(pts1);
//		line2=gf.createLineString(pts2);
//		if (!line1.intersects(line2) && !line1.intersects(pg) && !line2.intersects(pg))
//		{
//			if (line1.intersects(pg))
//			{
//				Geometry geo=line1.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts1[0].x)<0.000001 && Math.abs(pt.getY()-pts1[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			if (flag==true && line2.intersects(pg))
//			{
//				Geometry geo=line2.intersection(pg.getExteriorRing());
//				if (geo instanceof MultiPoint)
//				{
//					MultiPoint mp=(MultiPoint)geo;
//					if (mp.getNumGeometries()==1)
//					{
//						Point pt=(Point) mp.getGeometryN(0);
//						if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//							flag=true;
//						else flag=false;
//					}
//				}
//				else if (geo instanceof Point)
//				{
//					Point pt=(Point)geo;
//					if (Math.abs(pt.getX()-pts2[0].x)<0.000001 && Math.abs(pt.getY()-pts2[0].y)<0.000001)
//						flag=true;
//					else flag=false;
//				}
//				else flag=false;
//			}
//			else
//				flag=true;
//			
//			if (flag)
//			{
//				Coordinate[] pts=line.getCoordinates();
//				int length=pts.length;
//				Coordinate[] points=new Coordinate[length+3];
//				System.arraycopy(pts, 0, points, 0, length);
//				points[length++]=pts2[1];
//				points[length++]=pts1[1];
//				points[length++]=points[0];
//				return gf.createPolygon(points);
//			}
//		}
//		return null;
//	}
	
	/**
	 * 根据几何对象裁切几何对象，支持用面裁线、用面裁面、用线裁面
	 * @param target 目标几何对象，这个几何对象本身不会改变，而通过返回值来告知裁切结果
	 * @param ref 参考几何对象
	 * @return 裁切的结果，是一个几何对象数组
	 */
	public static Geometry[] cut(Geometry target,Geometry ref)
	{
		if (target==null || ref==null) return null;
		if ((target.getGeometryType()==GeometryType.Polygon/* || target.getGeometryType()==GeometryType.MultiPolygon*/) &&
				ref.getGeometryType()==GeometryType.LineString)
		{//目标是多面的情况没有处理
			//Polygon pg=(Polygon)target;
			Geometry pg=target;
			LineString line=(LineString)ref;
			GeometryFactory gf=new GeometryFactory();
			if (line.isClosed())
			{
				Polygon cutPG=gf.createPolygon(line.getCoordinates());
				Geometry geo1=target.intersection(cutPG);
				if (geo1==null) return null;
				Geometry geo2=target.difference(cutPG);
				if (geo2==null) return null;
				Geometry[] result=new Geometry[2];
				result[0]=geo1;result[1]=geo2;
				return result;
			}
			else
			{
				Envelope env=new Envelope(pg.getEnvelopeInternal());
				Coordinate[] pts=line.getCoordinates();
				Coordinate[] pts1=new Coordinate[2];
				Coordinate[] pts2=new Coordinate[2];
				
				pts1[0]=pts[0];pts1[1]=new Coordinate(0,0);
				pts2[0]=pts[pts.length-1];pts2[1]=new Coordinate(0,0);
				LineString line1=gf.createLineString(pts1);
				LineString line2=gf.createLineString(pts2);
				
				for (int i=0;i<10;++i)//尝试放大访问搜索十次
				{
					env.expandBy(env.getWidth()*5, env.getHeight()*5);
					//env.expand(10, 10, false);//将env放大之后重新计算
					Polygon cutPG=makeCutPolygon(env,(Polygon)pg,line1,line2,line);
					//if (!cutPG.isSimple()) continue;
					if (cutPG!=null)
					{
						Geometry geo1=target.intersection(cutPG);
						if (geo1==null || geo1.isEmpty()) return null;
						Geometry geo2=target.difference(cutPG);
						if (geo2==null || geo2.isEmpty()) return null;
						Geometry[] result=new Geometry[2];
						result[0]=geo1;result[1]=geo2;
						return result;
					}
				}
			}
			return null;
		}
		else if ((target.getGeometryType()==GeometryType.LineString || target.getGeometryType()==GeometryType.MultiLineString) &&
				ref.getGeometryType()==GeometryType.LineString)
		{
			Geometry geo=target.difference(ref);
			if (geo.getGeometryType()==GeometryType.MultiLineString)
			{
				MultiLineString mls=(MultiLineString)geo;
				int count=mls.getNumGeometries();
				Geometry[] result=new Geometry[count];
				for (int i=0;i<count;++i)
				{
					result[i]=mls.getGeometryN(i);
				}
				return result;
			}
			return null;
		}
		else
		{
			Geometry geo1=target.intersection(ref);
			if (geo1==null) return null;
			Geometry geo2=target.difference(ref);
			if (geo2==null) return null;
			Geometry[] result=new Geometry[2];
			result[0]=geo1;result[1]=geo2;
			return result;
		}
	}
}
