package cn.creable.surveyOnUCMap;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;

import com.bumptech.glide.Glide;

public class PhotoAdapter extends BaseAdapter {
    private Context context;
    private List<String> photos;

    public PhotoAdapter(Context context) {
        this.context = context;
        this.photos = new ArrayList<String>();
    }

    public void setPhotos(String[] items) {
        this.photos.clear();
        for (String s : items) {
            this.photos.add(s);
        }
        notifyDataSetChanged();
    }

    public List<String> getPhotos() {
        return photos;
    }

    public void addPhoto(String path) {
        this.photos.add(path);
        notifyDataSetChanged();
    }

    public void removePhoto(String path) {
        this.photos.remove(path);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return photos.size();
    }

    @Override
    public Object getItem(int position) {
        return photos.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = View.inflate(context, R.layout.photo, null);
        }
        ImageView iv = (ImageView) convertView.findViewById(R.id.iv_photo);
        Glide.with(context).load(GVS.getInstance().photoPath + photos.get(position)).error(R.drawable.error).into(iv);
        return convertView;
    }
}
