package cn.creable.surveyOnUCMap;

/**
 * Created by blucelee on 2016/11/29.
 *
 * �����ļ�������Ҫ��һ��ʼ��ʱ��Ҫ��ʾ����Ƭ��ͼ���������֣��������������ķ�װ����
 */

public class BluFile {
    private String name;
    private String path;
    private boolean isDir;

    public BluFile(String name, String path, boolean isDir) {
        this.name = name;
        this.path = path;
        this.isDir = isDir;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }
}