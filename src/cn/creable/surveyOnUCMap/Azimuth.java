package cn.creable.surveyOnUCMap;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * �����豸��������ȡ��λ��
 * ���豸ƽ��ʱ�����豸��ͷ�������㷽λ��
 * ���豸��ֱʱ�����豸�ı��������㷽λ��
 *
 */
public class Azimuth {
	private boolean sensorReady=false;
	private float[] magnitude_values = new float[3];
	private float[] accelerometer_values = new float[3];
	
	float[] outR = new float[16];
    float[] actual_orientation = new float[3];
    float[] R = new float[16];
    float[] I = new float[16];
    
    private double angle;
    private long time;
    private int interval;
	
	private SensorEventListener listener;
	
	private SensorEventListener myListener=new SensorEventListener() {

		@Override
		public void onAccuracyChanged(Sensor arg0, int arg1) {
			
		}

		@Override
		public void onSensorChanged(SensorEvent event) {
			switch (event.sensor.getType()) {
		    case Sensor.TYPE_MAGNETIC_FIELD:
		    	magnitude_values=null;
		        magnitude_values = event.values.clone();
		        sensorReady = true;
		        break;
		    case Sensor.TYPE_ACCELEROMETER:
		    	accelerometer_values=null;
		        accelerometer_values = event.values.clone();
		    }   
			//���㷽λ����Ҫ2�����������ص�ֵ����
			long now=System.currentTimeMillis();
		    if ((now-time)>interval && magnitude_values != null && accelerometer_values != null && sensorReady) {
		    	//System.out.println("time="+(now-time));
		    	time=System.currentTimeMillis();
		        sensorReady = false;
		        SensorManager.getRotationMatrix(R, I, accelerometer_values, magnitude_values);
		        
		        float v0=Math.abs(accelerometer_values[0]);
		        float v1=Math.abs(accelerometer_values[1]);
		        float v2=Math.abs(accelerometer_values[2]);
		        if (v2>v0 && v2>v1)
		        {//���z�ᴦ�ڴ�ֱ��Ҳ�����豸��ƽ���ŵ����
		        	SensorManager.getOrientation(R, actual_orientation); 
		        	actual_orientation[0]+=Math.PI/2;//���ڽ���֧�ֺ���������������Ҫ����90��
		        }
		        else if (v1>v0 && v1>v2)
		        {//���y�ᴦ�ڴ�ֱ��Ҳ�����豸�������������
		        	SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Z, outR);
		        	SensorManager.getOrientation(outR, actual_orientation);
		        }
		        else
		        {
		        	SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Y, outR);
		        	SensorManager.getOrientation(outR, actual_orientation);
		        	if (accelerometer_values[0]>0) actual_orientation[0]+=Math.PI/2;
		        	else actual_orientation[0]-=Math.PI/2;
		        }
		        angle=Math.toDegrees(actual_orientation[0]);
		        if (angle<0) angle=360+angle;
		        if (listener!=null) listener.onSensorChanged(event);
		    }
		}
		
	};
	
	/**
	 * ��ȡ��λ��
	 * @return ��λ�ǣ���λ�Ĵ�С�仯��ΧΪ0�㡫360�㣬����Ϊ0�㣬����Ϊ90�㣬�ϵ�Ϊ180�㣬����Ϊ270��
	 */
	public double get()
	{
		return angle;
	}
	
	private SensorManager sm;
	
	/**
	 * ��ʼ��ȡ��λ��
	 * @param listener ������������λ�ǻ�ȡ��֮������listener��onSensorChanged����
	 * @param interval ʱ��������λ�Ǻ��룬����ʱ����֮�󣬻ᰴ��ָ���ļ��������������
	 */
	public void start(Context c,SensorEventListener listener,int interval)
	{
		time=System.currentTimeMillis();
		this.listener=listener;
		this.interval=interval;
		//����2��������
		sm = (SensorManager) c.getSystemService(Context.SENSOR_SERVICE);
		sm.registerListener(myListener, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
		sm.registerListener(myListener, sm.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD),SensorManager.SENSOR_DELAY_NORMAL);
	}
	
	/**
	 * ֹͣ��ȡ��λ��
	 */
	public void stop()
	{
		sm.unregisterListener(myListener);
	}
	
	/**
	 * ����2���������㷽λ��
	 * @param x
	 * @param y
	 * @param x2
	 * @param y2
	 * @return
	 */
	public static double calc(double x,double y,double x2,double y2)
	{
		double dx=y2-y;
		double dy=x2-x;
		if (dx==0)
		{
			if (dy>0) return 90;
			else return 270;
		}
		double a=Math.atan(Math.abs(dy/dx))/Math.PI*180;
		if (dx>0 && dy>=0)
		{
			
		}
		else if (dx<0 && dy>=0)
		{
			a=180-a;
		}
		else if (dx<0 && dy<0)
		{
			a=180+a;
		}
		else if (dx>0 && dy<0)
		{
			a=360-a;
		}
		return a;
	}
}
