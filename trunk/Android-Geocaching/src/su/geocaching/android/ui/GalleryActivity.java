package su.geocaching.android.ui;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.*;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Gallery;
import android.widget.ImageView;
import su.geocaching.android.controller.apimanager.DownloadPhotoTask;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.model.GeoCache;

import java.io.File;
import java.io.FilenameFilter;


/**
 * Created by IntelliJ IDEA.
 * User: Anatoliy
 * Date: 24.04.11
 * Time: 0:15
 * To change this template use File | Settings | File Templates.
 */
public class GalleryActivity extends Activity {

    private static final String TAG = GalleryActivity.class.getCanonicalName();
    private static final int DEFAULT_ID = -1;

    private Uri[] mUrls;
    private String[] mFiles = null;
    private int cacheId;
    private String[] cachePhotoNames;
    private File images;
    private Context context;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);

        LogManager.e(TAG, "onCreate");
        context = this;
        cacheId = getIntent().getIntExtra(GeoCache.class.getCanonicalName(), DEFAULT_ID);
        if (cacheId != DEFAULT_ID) {
            images = new File(Environment.getExternalStorageDirectory(), String.format(context.getString(R.string.cache_directory), cacheId));
            cachePhotoNames = images.list();
            File[] imagelist = images.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return ((name.endsWith(".jpg")) || (name.endsWith(".png")));
                }
            });
            mFiles = new String[imagelist.length];
            mUrls = new Uri[mFiles.length];


            for (int i = 0; i < imagelist.length; i++) {
                mFiles[i] = imagelist[i].getAbsolutePath();
                mUrls[i] = Uri.parse(mFiles[i]);
            }

            Gallery g = (Gallery) findViewById(R.id.gallery);
            g.setAdapter(new ImageAdapter(this));
            g.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView parent, View v, int position, long id) {


                    Intent intent = new Intent();
                    intent.setAction(android.content.Intent.ACTION_VIEW);
                    File file = new File(mFiles[position]);
                    intent.setDataAndType(Uri.fromFile(file), "image/*");
                    startActivity(intent);

                }

            });
            g.setFadingEdgeLength(40);
        }
    }

    public void deleteCachePhotosFromSDCard() {

        for (String i : cachePhotoNames) {
            int d = i.indexOf(".");
            String s = i.substring(0, d);
            String id = String.format("%d%s", cacheId, s);
            this.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    String.format("%s=%s", MediaStore.Images.Media._ID, id),
                    null);
        }
        images.delete();
    }
     @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.gallery_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menu_delete_photos_cache:
                deleteCachePhotosFromSDCard();
                finish();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public class ImageAdapter extends BaseAdapter {

        int mGalleryItemBackground;

        public ImageAdapter(Context c) {
            mContext = c;
        }

        public int getCount() {
            return mUrls.length;
        }

        public Object getItem(int position) {
            return position;
        }

        public long getItemId(int position) {
            return position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView i = new ImageView(mContext);
            i.setImageURI(mUrls[position]);
            i.setScaleType(ImageView.ScaleType.FIT_XY);
            i.setLayoutParams(new Gallery.LayoutParams(260, 210));
            return i;
        }

        private Context mContext;
    }
}