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
 * 用于存放一些全局变量
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

    public Map<String, UCLayer> layers = new HashMap<String, UCLayer>();//存储所有图层
    public String mapPath = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "ucdata" + File.separator;//地图路径
    public String photoPath = mapPath + "photos" + File.separator;//照片路径
    public String videoPath = mapPath + "videos" + File.separator;//录像路径
    public String voicesPath = mapPath + "voices" + File.separator;//录音路径
    public String exportPath = mapPath + "export" + File.separator;//导出图层路径
    public String screenshort = mapPath + "screenshort" + File.separator;//截图路径
    public PhotoAdapter pa;//添加照片的适配器
    public List<String> voices = new ArrayList<String>();//管理所有添加的录音文件
    public TextView tv_voice;//显示最新录音
    public List<String> videos = new ArrayList<String>();//管理所有添加的录像文件
    public String vp;//临时存储录音文件路径
    public TextView tv_video;//显示最新录像
    public int dwState = 0;//百度定位状态切换

    //将任意view对象保存为图片
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
                throw new Exception("创建文件失败!");
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
