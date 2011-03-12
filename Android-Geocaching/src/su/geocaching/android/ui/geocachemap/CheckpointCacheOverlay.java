package su.geocaching.android.ui.geocachemap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.utils.UiHelper;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class CheckpointCacheOverlay extends ItemizedOverlay<OverlayItem> {

    private GestureDetector gestureDetector;

    private List<GeoCacheOverlayItem> items;
    private Activity activity;
    private MapView map;
    private int checkpointNumber;

    public CheckpointCacheOverlay(Drawable defaultMarker, Activity context, MapView map) {
        super(defaultMarker);

        items = Collections.synchronizedList(new LinkedList<GeoCacheOverlayItem>());
        gestureDetector = new GestureDetector(context, sogl);

        this.activity = context;
        this.map = map;

        populate();
    }

    GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener() {

        public void onLongPress(MotionEvent e) {
            GeoCache gc = new GeoCache();
            gc.setType(GeoCacheType.CHECKPOINT);
            gc.setLocationGeoPoint(map.getProjection().fromPixels((int) e.getX(), (int) e.getY()));
            UiHelper.startStepByStepForResult(activity, gc);
        }
    };

    public void addOverlayItem(GeoCacheOverlayItem overlayItem) {
        if (!contains(overlayItem.getGeoCache())) {
            checkpointNumber++;
            overlayItem.getGeoCache().setName(activity.getString(R.string.checkpoint_dialog_title)+" "+checkpointNumber);
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

    public GeoCache getGeoCache(int index) {
        return items.get(index).getGeoCache();
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
        activity.showDialog(index);
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        return gestureDetector.onTouchEvent(event);
    }

    /**
     * @return the activeItem
     */
    public int getActiveItem() {
        int i = 0;
        for (GeoCacheOverlayItem item : items) {
            if (item.getGeoCache().getStatus() == GeoCacheStatus.ACTIVE_CHECKPOINT) {
                break;
            }
            i++;
        }
        return i;
    }

    /**
     * @param activeItem
     *            the activeItem to set
     */
    public void setActiveItem(int activeItem) {
        for (GeoCacheOverlayItem item : items) {
            item.getGeoCache().setStatus(GeoCacheStatus.NOT_ACTIVE_CHECKPOINT);
        }

        int i = getActiveItem();
        items.get(i).getGeoCache().setStatus(GeoCacheStatus.ACTIVE_CHECKPOINT);
    }
}
