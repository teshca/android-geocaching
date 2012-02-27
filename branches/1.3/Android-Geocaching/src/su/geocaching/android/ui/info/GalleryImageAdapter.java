package su.geocaching.android.ui.info;

import java.io.File;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.R;

/**
 * @author Nikita Bumakov
 */
class GalleryImageAdapter extends BaseAdapter {

    private static final String TAG = GalleryImageAdapter.class.getCanonicalName();

    private final int cacheId;
    private final Context context;
    private final int thumbnailsPhotoSize;
    private File[] imageList;

    public GalleryImageAdapter(final Context context, int cacheId) {
        this.context = context;
        LogManager.d(TAG, "created");
        thumbnailsPhotoSize = context.getResources().getDimensionPixelSize(R.dimen.adapter_photo_size);
        this.cacheId = cacheId;
        updateImageList();
    }

    public void updateImageList() {
        imageList = Controller.getInstance().getExternalStorageManager().getPhotos(cacheId);
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
        return i;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView image;
        if (convertView == null) { // if it's not recycled, initialize some attributes
            image = new ImageView(context);
            image.setLayoutParams(new GridView.LayoutParams(thumbnailsPhotoSize, thumbnailsPhotoSize));
            image.setScaleType(ImageView.ScaleType.CENTER_INSIDE);
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
        int scale = Math.max(options.outHeight, options.outWidth) / thumbnailsPhotoSize;
        BitmapFactory.Options options2 = new BitmapFactory.Options();
        options2.inSampleSize = scale;
        return BitmapFactory.decodeFile(path, options2);
    }
}