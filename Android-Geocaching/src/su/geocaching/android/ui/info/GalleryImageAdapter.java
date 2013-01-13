package su.geocaching.android.ui.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.adapters.BaseArrayAdapter;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.ui.R;

/**
 * @author Nikita Bumakov
 */
class GalleryImageAdapter extends BaseArrayAdapter<Uri> {

    private static final String TAG = GalleryImageAdapter.class.getCanonicalName();

    private final int cacheId;
    private final int thumbnailsPhotoSize;

    public GalleryImageAdapter(final Context context, int cacheId) {
        super(context);
        this.thumbnailsPhotoSize = context.getResources().getDimensionPixelSize(R.dimen.adapter_photo_size);
        this.cacheId = cacheId;
        LogManager.d(TAG, "created");
    }

    public void updateImageList() {
        clear();
        addAll(Controller.getInstance().getExternalStorageManager().getPhotos(cacheId));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ImageView image;
        if (convertView == null) { // if it's not recycled, initialize some attributes
            image = new ImageView(getContext());
            image.setLayoutParams(new GridView.LayoutParams(thumbnailsPhotoSize, thumbnailsPhotoSize));
            image.setScaleType(ImageView.ScaleType.CENTER_CROP); // ImageView.ScaleType.CENTER_INSIDE         
        } else {
            image = (ImageView) convertView;
        }

        Bitmap scaleBm = scaleBitmap(position);
        if (scaleBm == null) {
            image.setImageResource(R.drawable.photo_bg);
        } else {
            image.setImageBitmap(scaleBm);
        }

        return image;
    }

    private Bitmap scaleBitmap(int position) {
        BitmapFactory.Options justDecodeBoundsOptions = new BitmapFactory.Options();
        justDecodeBoundsOptions.inJustDecodeBounds = true;
        String path = getItem(position).getPath();
        BitmapFactory.decodeFile(path, justDecodeBoundsOptions);
        if (justDecodeBoundsOptions.outHeight == -1 || justDecodeBoundsOptions.outWidth == -1) {
            return null;
        }
        int scale = Math.max(justDecodeBoundsOptions.outHeight, justDecodeBoundsOptions.outWidth) / thumbnailsPhotoSize;
        BitmapFactory.Options scaleOptions = new BitmapFactory.Options();
        scaleOptions.inSampleSize = scale;
        return BitmapFactory.decodeFile(path, scaleOptions);
    }
}