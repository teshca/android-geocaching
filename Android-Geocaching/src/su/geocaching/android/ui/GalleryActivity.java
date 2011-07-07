package su.geocaching.android.ui;

import java.io.File;
import java.io.FilenameFilter;

import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * @author Anatoliy
 */
public class GalleryActivity extends Activity {

    private static final String TAG = GalleryActivity.class.getCanonicalName();
    private static final int DEFAULT_ID = -1;

    private int cacheId;
    private Uri[] photoUrls;
    private String[] photoNames;
    private File images;
    private Context context;

    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.gallery_activity);

        LogManager.e(TAG, "onCreate");
        context = this;
        cacheId = getIntent().getIntExtra(NavigationManager.CACHE_ID, DEFAULT_ID);
        if (cacheId != DEFAULT_ID) {
            images = new File(Environment.getExternalStorageDirectory(), String.format(context.getString(R.string.cache_directory), cacheId));
            photoNames = images.list();
            final File[] imagelist = images.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".jpg") || name.endsWith(".png"));
                }
            });

            photoUrls = new Uri[imagelist.length];

            for (int i = 0; i < imagelist.length; i++) {
                photoUrls[i] = Uri.parse(imagelist[i].getAbsolutePath());
            }

            GridView photosView = (GridView) findViewById(R.id.gridview);
            photosView.setAdapter(new ImageAdapter(this));
            photosView.setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    NavigationManager.startPictureViewer(context, Uri.fromFile(imagelist[position]));
                }
            });
        }
    }

    // TODO this method doesn't work
    public void deleteCachePhotosFromSDCard() {

        for (String i : photoNames) {
            int d = i.indexOf(".");
            String s = i.substring(0, d);
            String id = String.format("%d%s", cacheId, s);
            getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.format("%s=%s", MediaStore.Images.Media._ID, id), null);
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

        private Context context;

        public ImageAdapter(Context c) {
            context = c;
        }

        public int getCount() {
            return photoUrls.length;
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        final int photoWidth = 125;
        final int photoHeight = 100;

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image;
            if (convertView == null) { // if it's not recycled, initialize some attributes
                image = new ImageView(context);
                image.setLayoutParams(new GridView.LayoutParams(photoWidth, photoHeight));
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            } else {
                image = (ImageView) convertView;
            }

            image.setImageBitmap(scaleBitmap(position));
            return image;
        }

        private Bitmap scaleBitmap(int position) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(images.listFiles()[position].getAbsolutePath(), options);
            int width_tmp = options.outWidth, height_tmp = options.outHeight;
            int scale = 1;

            while (true) {
                if (width_tmp / 2 < photoWidth || height_tmp / 2 < photoHeight)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;
            return BitmapFactory.decodeFile(images.listFiles()[position].getAbsolutePath(), options2);
        }
    }

    public void onHomeClick(View v) {
        NavigationManager.startDashboardActvity(this);
    }
}