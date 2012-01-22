package su.geocaching.android.ui.searchmap;

import java.util.LinkedList;
import java.util.List;

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
import com.google.android.maps.OverlayItem;

/**
 * Overlay with checkpoints
 *
 * @author Nikita Bumakov
 */
public class CheckpointOverlay extends ItemizedOverlay<GeoCacheOverlayItem> {

    private final GestureDetector gestureDetector;
    private final List<GeoCacheOverlayItem> items;
    private final Activity activity;

    public CheckpointOverlay(Drawable defaultMarker, final Activity context, final MapView mapView) {
        super(defaultMarker);

        this.activity = context;
        items = new LinkedList<GeoCacheOverlayItem>();

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                int cacheId = Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId();
                for (GeoCacheOverlayItem item : items) {
                    if (hitTest(e, item))
                    {
                        //Controller.getInstance().getCheckpointManager(cacheId).setActiveItem(item.getGeoCache().getId());
                        return;
                    }
                }
                GeoCache gc = new GeoCache();
                gc.setType(GeoCacheType.CHECKPOINT);
                gc.setId(cacheId);
                gc.setLocationGeoPoint(mapView.getProjection().fromPixels((int) e.getX(), (int) e.getY()));
                NavigationManager.startCreateCheckpointActivity(activity, gc);
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {
                for (GeoCacheOverlayItem item : items) {
                    if (hitTest(e, item))
                    {
                        NavigationManager.startCheckpointDialog(activity, item.getGeoCache().getId());
                        return true;
                    }
                }
                return false;
            }

            private boolean hitTest(MotionEvent event, OverlayItem item) {
                Point itemPoint = mapView.getProjection().toPixels(item.getPoint(), null);
                int relativeX = (int)event.getX() - itemPoint.x;
                int relativeY = (int)event.getY() - itemPoint.y;
                return item.getMarker(0).getBounds().contains(relativeX, relativeY);
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
        if (OverlayUtils.isMultiTouch(event))
            return false;
        return gestureDetector.onTouchEvent(event);
    }
}
