package cn.creable.surveyOnUCMap;

import java.util.List;

/**
 * Created by BluceLee on 2016-11-30.
 * 用于记录地图的当前信息
 */

public class MapInfo {
    private float scale;//当前地图显示比例
    private double xCenter;//当前地图中心点x坐标
    private double yCenter;//当前地图中心点y坐标
    private List<FeatureState> states;//记录图层的显示或隐藏状态

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
