package su.geocaching.android.ui.searchmap;

import java.util.LinkedList;
import java.util.List;

import android.graphics.Point;
import android.graphics.Rect;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.OverlayUtils;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * Overlay with checkpoints
 *
 * @author Nikita Bumakov
 */
public class CheckpointOverlay extends ItemizedOverlay<GeoCacheOverlayItem> {

    private final GestureDetector gestureDetector;
    private final List<GeoCacheOverlayItem> items;
    private boolean isMultiTouch;

    public CheckpointOverlay(Drawable defaultMarker, final SearchMapActivity searchMapActivity, final MapView mapView) {
        super(defaultMarker);

        items = new LinkedList<GeoCacheOverlayItem>();

        gestureDetector = new GestureDetector(searchMapActivity, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                if (isMultiTouch) return;
                if (searchMapActivity.getGeoCacheOverlay().hitTest(e, mapView)) return;
                if (searchMapActivity.getLocationOverlay().hitTest(e)) return;

                for (GeoCacheOverlayItem item : items) {
                    if (hitTest(e, item))
                    {
                        searchMapActivity.setActiveItem(item.getGeoCache());
                        return;
                    }
                }
                GeoCache gc = new GeoCache();
                gc.setType(GeoCacheType.CHECKPOINT);
                gc.setId(Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId());
                gc.setLocationGeoPoint(mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY()));
                NavigationManager.startCreateCheckpointActivity(searchMapActivity, gc);
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (OverlayUtils.isMultiTouch(e)) return false;
                for (GeoCacheOverlayItem item : items) {
                    if (hitTest(e, item))
                    {
                        Controller.getInstance().Vibrate();
                        NavigationManager.startCheckpointDialog(searchMapActivity, item.getGeoCache().getId());
                        return true;
                    }
                }
                return false;
            }

            private boolean hitTest(MotionEvent event, OverlayItem item) {
                Point itemPoint = mapView.getProjection().toPixels(item.getPoint(), null);
                int relativeX = (int)event.getX() - itemPoint.x;
                int relativeY = (int)event.getY() - itemPoint.y;
                int touchMargin = 10;
                Rect bounds = item.getMarker(0).getBounds();
                bounds.left -=  touchMargin;
                bounds.top -= touchMargin;
                bounds.bottom += touchMargin;
                bounds.right += touchMargin;
                return bounds.contains(relativeX, relativeY);
            }
        });

        populate();
    }

    public void addOverlayItem(GeoCacheOverlayItem overlayItem) {
        if (!contains(overlayItem.getGeoCache())) {
            items.add(overlayItem);
            setLastFocusedIndex(-1);
            populate();
        }
    }

    public void removeOverlayItem(int index) {
        items.remove(index);
        setLastFocusedIndex(-1);
        populate();
    }

    private boolean contains(GeoCache geoCache) {
        for (GeoCacheOverlayItem item : items) {
            if (item.getGeoCache().equals(geoCache)) {
                return true;
            }
        }
        return false;
    }

    @Override
    protected GeoCacheOverlayItem createItem(int i) {
        return items.get(i);
    }

    @Override
    public int size() {
        return items.size();
    }

    public void clear() {
        items.clear();
        setLastFocusedIndex(-1);
        populate();
    }

    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        isMultiTouch = OverlayUtils.isMultiTouch(event);
        return gestureDetector.onTouchEvent(event);
    }
}
