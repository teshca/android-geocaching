package su.geocaching.android.ui;

import java.io.File;
import java.io.FilenameFilter;

import su.geocaching.android.controller.apimanager.DownloadPhotoTask;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

/**
 * @author Nikita Bumakov
 */
public class GalleryView extends GridView {

    private static final String TAG = GalleryView.class.getCanonicalName();
    private static final int DEFAULT_ID = -1;

    private int cacheId;
    private Uri[] photoUrls;
    private Context context;
    private File[] imagelist;
    private File images;

    public GalleryView(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LogManager.e(TAG, "onCreate");

        this.context = context;
    }

    public void init(int cahaeId) {
        this.cacheId = cahaeId;

        if (cacheId != DEFAULT_ID) {
            images = new File(Environment.getExternalStorageDirectory(), String.format(context.getString(R.string.cache_directory), cacheId));

            imagelist = images.listFiles(new FilenameFilter() {

                @Override
                public boolean accept(File dir, String name) {
                    return (name.endsWith(".jpg") || name.endsWith(".png"));
                }
            });
            if (imagelist == null) {
                // TODO need some message
                return;
            }

            photoUrls = new Uri[imagelist.length];

            for (int i = 0; i < imagelist.length; i++) {
                photoUrls[i] = Uri.parse(imagelist[i].getAbsolutePath());
            }

            setAdapter(new ImageAdapter(context));
            setOnItemClickListener(new AdapterView.OnItemClickListener() {

                public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                    NavigationManager.startPictureViewer(context, Uri.fromFile(imagelist[position]));
                }
            });
        }
    }

    public void deleteCachePhotosFromSDCard() {
        if (images == null) {
            // TODO need some message
            return;
        }

        for (String f : images.list()) {
            String nameWithoutExtent = f.substring(0, f.indexOf("."));
            String id = String.format(DownloadPhotoTask.PHOTO_ID_TEMPLATE, cacheId, nameWithoutExtent);
            context.getContentResolver().delete(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, String.format("%s=%s", MediaStore.Images.Media._ID, id), null);
        }
        images.delete();
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

        private final int thumbnailsPhotoSize = 125;

        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView image;
            if (convertView == null) { // if it's not recycled, initialize some attributes
                image = new ImageView(context);
                image.setLayoutParams(new GridView.LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT));
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);
                image.setPadding(8, 8, 8, 8);

            } else {
                image = (ImageView) convertView;
            }

            image.setImageBitmap(scaleBitmap(position));
            return image;
        }

        private Bitmap scaleBitmap(int position) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            String path = imagelist[position].getAbsolutePath();
            BitmapFactory.decodeFile(path, options);
            int scale = Math.max(options.outHeight, options.outWidth) / thumbnailsPhotoSize + 1;
            BitmapFactory.Options options2 = new BitmapFactory.Options();
            options2.inSampleSize = scale;
            return BitmapFactory.decodeFile(path, options2);
        }
    }

}