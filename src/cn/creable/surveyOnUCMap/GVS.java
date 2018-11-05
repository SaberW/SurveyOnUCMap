package cn.creable.surveyOnUCMap;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.os.Environment;
import android.view.View;
import android.widget.TextView;
import cn.creable.ucmap.openGIS.UCLayer;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by blucelee on 2016/11/26.
 * <p>
 * ���ڴ��һЩȫ�ֱ���
 */

public class GVS {
    private static volatile GVS instance;

    private GVS() {
    }

    public static GVS getInstance() {
        if (instance == null) {
            synchronized (GVS.class) {
                if (instance == null) {
                    instance = new GVS();
                }
            }
        }
        return instance;
    }

    public Map<String, UCLayer> layers = new HashMap<String, UCLayer>();//�洢����ͼ��
    public String mapPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ucdata" + File.separator;//��ͼ·��
    public String photoPath = mapPath + "photos" + File.separator;//��Ƭ·��
    public String videoPath = mapPath + "videos" + File.separator;//¼��·��
    public String voicesPath = mapPath + "voices" + File.separator;//¼��·��
    public String exportPath = mapPath + "export" + File.separator;//����ͼ��·��
    public String screenshort = mapPath + "screenshort" + File.separator;//��ͼ·��
    public PhotoAdapter pa;//�����Ƭ��������
    public List<String> voices = new ArrayList<String>();//����������ӵ�¼���ļ�
    public TextView tv_voice;//��ʾ����¼��
    public List<String> videos = new ArrayList<String>();//����������ӵ�¼���ļ�
    public String vp;//��ʱ�洢¼���ļ�·��
    public TextView tv_video;//��ʾ����¼��
    public int dwState = 0;//�ٶȶ�λ״̬�л�

    //������view���󱣴�ΪͼƬ
    public void viewSaveToImage(View view, String name) {
        view.setDrawingCacheEnabled(true);
        view.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        view.setDrawingCacheBackgroundColor(Color.WHITE);
        Bitmap bitmap = Bitmap.createBitmap(loadBitmapFromView(view));
        FileOutputStream fos;
        try {
            boolean isHasSDCard = Environment.getExternalStorageState().equals(
                    android.os.Environment.MEDIA_MOUNTED);
            if (isHasSDCard) {
                File file = new File(screenshort + name);
                if (!file.getParentFile().exists()) {
                    file.getParentFile().mkdirs();
                }
                fos = new FileOutputStream(file);
            } else
                throw new Exception("�����ļ�ʧ��!");
            bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
            fos.flush();
            fos.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        view.destroyDrawingCache();
    }

    private Bitmap loadBitmapFromView(View v) {
        int w = v.getWidth();
        int h = v.getHeight();
        Bitmap bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888);
        Canvas c = new Canvas(bmp);
        c.drawColor(Color.WHITE);
        v.layout(0, 0, w, h);
        v.draw(c);
        return bmp;
    }
}
