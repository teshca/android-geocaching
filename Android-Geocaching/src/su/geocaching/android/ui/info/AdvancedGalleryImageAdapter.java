package su.geocaching.android.ui.info;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.View;
import android.view.ViewGroup;
import android.widget.GridView;
import android.widget.ImageView;
import su.geocaching.android.controller.adapters.BaseArrayAdapter;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.model.GeoCachePhoto;
import su.geocaching.android.ui.R;

/**
 * @author Nikita Bumakov
 */
class AdvancedGalleryImageAdapter extends BaseArrayAdapter<GeoCachePhoto> {

    private static final String TAG = AdvancedGalleryImageAdapter.class.getCanonicalName();

    private final int thumbnailsPhotoSize;
    private final InfoViewModel infoViewModel;

    public AdvancedGalleryImageAdapter(final Context context, InfoViewModel infoViewModel) {
        super(context);
        this.thumbnailsPhotoSize = context.getResources().getDimensionPixelSize(R.dimen.adapter_photo_size);
        this.infoViewModel = infoViewModel;
        setNotifyOnChange(false);
        LogManager.d(TAG, "created");
    }

    public void updateImageList() {        
        clear();
        add(infoViewModel.getPhotosState().getPhotos());
        notifyDataSetChanged();
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
            image.setImageResource(R.drawable.no_photo_square);
            image.setEnabled(false);
        } else {
            image.setImageBitmap(scaleBm);
            image.setEnabled(true);
        }

        return image;
    }

    private Bitmap scaleBitmap(int position) {
        BitmapFactory.Options justDecodeBoundsOptions = new BitmapFactory.Options();
        justDecodeBoundsOptions.inJustDecodeBounds = true;
        Uri localUri = getItem(position).localUri;
        if (localUri == null) {
            return null;
        }        
        String path = localUri.getPath();
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