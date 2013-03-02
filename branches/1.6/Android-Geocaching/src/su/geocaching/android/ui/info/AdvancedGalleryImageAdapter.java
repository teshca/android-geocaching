package su.geocaching.android.ui.info;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import su.geocaching.android.controller.adapters.BaseArrayAdapter;
import su.geocaching.android.controller.managers.LogManager;

/**
 * @author Nikita Bumakov
 */
class AdvancedGalleryImageAdapter extends BaseArrayAdapter<GeoCachePhotoViewModel> {

    private static final String TAG = AdvancedGalleryImageAdapter.class.getCanonicalName();

    private final InfoViewModel infoViewModel;

    public AdvancedGalleryImageAdapter(final Context context, InfoViewModel infoViewModel) {
        super(context);
        this.infoViewModel = infoViewModel;
        setNotifyOnChange(false);
        LogManager.d(TAG, "created");
    }

    public void updateImageList() {
        clear();
        addAll(infoViewModel.getPhotosState().getPhotos());
        notifyDataSetChanged();
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        GeoCachePhotoViewModel photo = getItem(position);
        if (convertView == null) {
            convertView = new GalleryItemView(getContext(), photo);
        } else {
            GalleryItemView galleryItemView = (GalleryItemView) convertView;
            galleryItemView.updateGeoCachePhoto(photo);
        }
        return convertView;
    }

    @Override
    public boolean isEnabled(int position) {
        return !getItem(position).IsDownloading();
    }
}