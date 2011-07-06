package su.geocaching.android.ui.searchmap;

import java.util.LinkedList;
import java.util.List;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheType;
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
public class CheckpointOverlay extends ItemizedOverlay<OverlayItem> {

    private GestureDetector gestureDetector;

    private final List<GeoCacheOverlayItem> items;
    private final Activity activity;

    public CheckpointOverlay(Drawable defaultMarker, Activity context, final MapView map) {
        super(defaultMarker);

        this.activity = context;
        items = new LinkedList<GeoCacheOverlayItem>();
     
        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                GeoCache gc = new GeoCache();
                gc.setType(GeoCacheType.CHECKPOINT);
                gc.setId(Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId());
                gc.setLocationGeoPoint(map.getProjection().fromPixels((int) e.getX(), (int) e.getY()));
                NavigationManager.startStepByStep(activity, gc);
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
    protected OverlayItem createItem(int i) {
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
    public boolean onTap(int index) {
        NavigationManager.startCheckpointDialog(activity, items.get(index).getGeoCache().getId());
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        return gestureDetector.onTouchEvent(event);
    }
}
