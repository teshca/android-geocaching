package su.geocaching.android.ui.info;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.AdapterView;
import android.widget.GridView;
import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCachePhoto;

/**
 * @author Nikita Bumakov
 */
public class GalleryView extends GridView {

    private static final String TAG = GalleryView.class.getCanonicalName();

    public GalleryView(final Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        LogManager.d(TAG, "onCreate");

        setOnItemClickListener(new AdapterView.OnItemClickListener() {

            public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
                GeoCachePhoto photo = (GeoCachePhoto) getAdapter().getItem(position);
                NavigationManager.startPictureViewer(context, photo.localUri);
            }
        });
    }
}