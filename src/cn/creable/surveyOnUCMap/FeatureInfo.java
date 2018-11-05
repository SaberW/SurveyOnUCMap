package cn.creable.surveyOnUCMap;

public class FeatureInfo
{
	public double xmin,ymin,xmax,ymax;
	public String text;
	
	public FeatureInfo(String text,double xmin,double ymin,double xmax,double ymax)
	{
		this.text=text;
		this.xmin=xmin;
		this.ymin=ymin;
		this.xmax=xmax;
		this.ymax=ymax;
	}
}