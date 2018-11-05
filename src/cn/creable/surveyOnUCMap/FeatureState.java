package cn.creable.surveyOnUCMap;

/**
 * Created by BluceLee on 2016-11-30.
 * 用于记录图层的显示和隐藏状态
 */

public class FeatureState {
    private String featureName;
    private boolean isVisible;

    public FeatureState(String featureName, boolean isVisible) {
        this.featureName = featureName;
        this.isVisible = isVisible;
    }

    public String getFeatureName() {
        return featureName;
    }

    public void setFeatureName(String featureName) {
        this.featureName = featureName;
    }

    public boolean isVisible() {
        return isVisible;
    }

    public void setVisible(boolean visible) {
        isVisible = visible;
    }
}
