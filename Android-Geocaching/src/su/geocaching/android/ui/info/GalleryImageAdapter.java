package su.geocaching.android.ui.info;

import java.io.File;
import java.io.FilenameFilter;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.R;

/**
 * @author Nikita Bumakov
 */
public class GalleryImageAdapter extends BaseAdapter {

    private static final String TAG = GalleryImageAdapter.class.getCanonicalName();

    private File[] imageList;
    private Context context;

    private final int thumbnailsPhotoSize = 125;

    public GalleryImageAdapter(Context context) {
        this.context = context;
        LogManager.d(TAG, "new GalleryImageAdapter created");
    }

    public GalleryImageAdapter(final Context context, int cacheId) {
        this(context);

        File imagesDirectory = new File(Environment.getExternalStorageDirectory(), String.format(context.getString(R.string.cache_directory), cacheId));

        FilenameFilter imageFilter = new FilenameFilter() {

            @Override
            public boolean accept(File dir, String name) {
                return (name.endsWith(".jpg") || name.endsWith(".png"));
            }
        };
        imageList = imagesDirectory.listFiles(imageFilter);
    }

    @Override
    public int getCount() {
        return imageList == null ? 0 : imageList.length;
    }

    @Override
    public Object getItem(int i) {
        return Uri.fromFile(imageList[i]);
    }

    @Override
    public long getItemId(int i) {
        return 0;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView image;
        if (convertView == null) { // if it's not recycled, initialize some attributes
            image = new ImageView(context);
            image.setLayoutParams(new GridView.LayoutParams(GridView.LayoutParams.WRAP_CONTENT, GridView.LayoutParams.WRAP_CONTENT));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);
            image.setPadding(8, 8, 8, 8);

        } else {
            image = (ImageView) convertView;
        }

        Bitmap scaleBm = scaleBitmap(position);
        if (scaleBm == null) {
            image.setImageResource(R.drawable.no_photo);
        } else {
            image.setImageBitmap(scaleBm);
        }

        return image;
    }

    private Bitmap scaleBitmap(int position) {
        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        String path = imageList[position].getAbsolutePath();
        BitmapFactory.decodeFile(path, options);
        if (options.outHeight == -1 || options.outWidth == -1) {
            return null;
        }
        int scale = Math.max(options.outHeight, options.outWidth) / thumbnailsPhotoSize + 1;
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inSampleSize = scale;
        return BitmapFactory.decodeFile(path, options2);
    }
}