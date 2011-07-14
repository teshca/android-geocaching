package su.geocaching.android.ui.selectmap;

import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author: Yuri Denison
 * @since: 14.07.11
 */
public class SimpleUserLocationOverlay extends ItemizedOverlay<OverlayItem> {
    private OverlayItem locationItem;

    public SimpleUserLocationOverlay(Drawable drawable) {
        super(drawable);
        populate();
    }

    public void addOverlayItem(OverlayItem locationItem) {
        this.locationItem = locationItem;
        setLastFocusedIndex(-1);
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return locationItem;
    }

    @Override
    public int size() {
        return locationItem == null ? 0 : 1;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView map) {
        return false;
    }

    public void clear() {
        locationItem = null;
        setLastFocusedIndex(-1);
        populate();
    }
}
