package su.geocaching.android.ui.searchmap;

import android.graphics.Point;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.OverlayUtils;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 GeoCache Itemized Overlay for one or more caches
 */
class SearchGeoCacheOverlay extends ItemizedOverlay<GeoCacheOverlayItem> {

    private GeoCacheOverlayItem item;
    private final GestureDetector gestureDetector;

    public SearchGeoCacheOverlay(Drawable defaultMarker, final SearchMapActivity searchMapActivity, final MapView mapView) {
        super(defaultMarker);
        populate();

        gestureDetector = new GestureDetector(searchMapActivity, new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                mapView.getController().zoomInFixing((int) e.getX(), (int) e.getY());
                return true;
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {
                GeoCache gc = item.getGeoCache();
                NavigationManager.startInfoActivity(searchMapActivity, gc);
                return true;
            }

            public void onLongPress(MotionEvent e) {
                searchMapActivity.setActiveItem(item.getGeoCache());
            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        if (OverlayUtils.isMultiTouch(event))
            return false;

        if (hitTest(event, mapView)) {
            gestureDetector.onTouchEvent(event);
            return true;
        }

        return false;
    }

    private boolean hitTest(MotionEvent event, MapView mapView) {
        Point itemPoint = mapView.getProjection().toPixels(item.getPoint(), null);
        int relativeX = (int)event.getX() - itemPoint.x;
        int relativeY = (int)event.getY() - itemPoint.y;
        return hitTest(item, item.getMarker(0), relativeX, relativeY);
    }

    public void addOverlayItem(GeoCacheOverlayItem overlayItem) {
        item = overlayItem;
        setLastFocusedIndex(-1);
        populate();
    }

    @Override
    protected GeoCacheOverlayItem createItem(int i) {
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
}
