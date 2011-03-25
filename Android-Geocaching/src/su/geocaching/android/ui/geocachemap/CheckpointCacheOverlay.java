package su.geocaching.android.ui.geocachemap;

import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * Overlay with checkpoints
 * 
 * @author Nikita Bumakov
 */
public class CheckpointCacheOverlay extends ItemizedOverlay<OverlayItem> {

    private final GestureDetector gestureDetector;

    private final List<GeoCacheOverlayItem> items;
    private final Activity activity;
    private int checkpointNumber;

    private final DbManager dbm;

    public CheckpointCacheOverlay(Drawable defaultMarker, Activity context, final MapView map) {
        super(defaultMarker);

        dbm = Controller.getInstance().getDbManager();

        items = Collections.synchronizedList(new LinkedList<GeoCacheOverlayItem>());
        GestureDetector.SimpleOnGestureListener sogl = new GestureDetector.SimpleOnGestureListener() {
            public void onLongPress(MotionEvent e) {
                GeoCache gc = new GeoCache();
                gc.setType(GeoCacheType.CHECKPOINT);
                gc.setLocationGeoPoint(map.getProjection().fromPixels((int) e.getX(), (int) e.getY()));
                UiHelper.startStepByStepForResult(activity, gc);
            }
        };
        gestureDetector = new GestureDetector(context, sogl);

        this.activity = context;

        populate();
    }

    public void addOverlayItem(GeoCacheOverlayItem overlayItem) {
        if (!contains(overlayItem.getGeoCache())) {
            checkpointNumber++;
            overlayItem.getGeoCache().setName(String.format("%s %d", activity.getString(R.string.checkpoint_dialog_title), checkpointNumber));
            dbm.addCheckpointGeoCache(overlayItem.getGeoCache());
            items.add(overlayItem);
            setLastFocusedIndex(-1);
            populate();
        }
    }

    public void removeOverlayItem(int index) {
        GeoCache gc = items.get(index).getGeoCache();
        dbm.deleteCheckpointCache(gc.getName(), gc.getId());
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
