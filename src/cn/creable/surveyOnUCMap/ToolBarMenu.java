package cn.creable.surveyOnUCMap;

/**
 * Created by BluceLee on 2016-12-02.
 * �ײ�toolbar�����Ϣ
 */

public class ToolBarMenu {
    private String name;//�˵�����
    private int icon;//�˵�ͼ��
    private boolean showDevider;//�Ƿ���ʾ�ָ���

    public ToolBarMenu(String name, int icon, boolean showDevider) {
        this.name = name;
        this.icon = icon;
        this.showDevider = showDevider;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getIcon() {
        return icon;
    }

    public void setIcon(int icon) {
        this.icon = icon;
    }

    public boolean isShowDevider() {
        return showDevider;
    }

    public void setShowDevider(boolean showDevider) {
        this.showDevider = showDevider;
    }
}