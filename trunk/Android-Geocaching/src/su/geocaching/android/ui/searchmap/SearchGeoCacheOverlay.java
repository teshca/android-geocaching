package su.geocaching.android.ui.searchmap;

import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 GeoCache Itemized Overlay for one or more caches
 */
public class SearchGeoCacheOverlay extends ItemizedOverlay<OverlayItem> {

    private GeoCacheOverlayItem item;
    private Activity activity;
    private final GestureDetector gestureDetector;

    public SearchGeoCacheOverlay(Drawable defaultMarker, Activity context, final MapView map) {
        super(defaultMarker);
        this.activity = context;
        populate();

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                map.getController().zoomInFixing((int) e.getX(), (int) e.getY());
                return true;
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView map) {
        return gestureDetector.onTouchEvent(event);
    }

    public void addOverlayItem(GeoCacheOverlayItem overlayItem) {
        item = overlayItem;
        setLastFocusedIndex(-1);
        populate();
    }

    public GeoCache getGeoCache(int index) {
        return item.getGeoCache();
    }

    @Override
    protected OverlayItem createItem(int i) {
        return item;
    }

    @Override
    public int size() {
        return item == null ? 0 : 1;
    }

    public void clear() {
        item = null;
        setLastFocusedIndex(-1);
        populate();
    }

    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, false);
    }

    @Override
    public boolean onTap(int index) {
        GeoCache gc = item.getGeoCache();
        UiHelper.startGeoCacheInfo(activity, gc);
        return true;
    }
}
