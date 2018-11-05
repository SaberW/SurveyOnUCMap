package cn.creable.surveyOnUCMap;

import java.util.List;

/**
 * Created by BluceLee on 2016-11-30.
 * ���ڼ�¼��ͼ�ĵ�ǰ��Ϣ
 */

public class MapInfo {
    private float scale;//��ǰ��ͼ��ʾ����
    private double xCenter;//��ǰ��ͼ���ĵ�x����
    private double yCenter;//��ǰ��ͼ���ĵ�y����
    private List<FeatureState> states;//��¼ͼ�����ʾ������״̬

    public MapInfo(float scale, double xCenter, double yCenter, List<FeatureState> states) {
        this.scale = scale;
        this.xCenter = xCenter;
        this.yCenter = yCenter;
        this.states = states;
    }

    public float getScale() {
        return scale;
    }

    public void setScale(float scale) {
        this.scale = scale;
    }

    public double getxCenter() {
        return xCenter;
    }

    public void setxCenter(double xCenter) {
        this.xCenter = xCenter;
    }

    public double getyCenter() {
        return yCenter;
    }

    public void setyCenter(double yCenter) {
        this.yCenter = yCenter;
    }

    public List<FeatureState> getStates() {
        return states;
    }

    public void setStates(List<FeatureState> states) {
        this.states = states;
    }
}
