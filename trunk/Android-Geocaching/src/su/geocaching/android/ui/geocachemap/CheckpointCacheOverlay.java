package su.geocaching.android.ui.geocachemap;

import java.util.LinkedList;
import java.util.List;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.model.datastorage.DbManager;
import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.model.datatype.GeoCacheStatus;
import su.geocaching.android.model.datatype.GeoCacheType;
import su.geocaching.android.ui.R;
import android.app.Activity;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

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

        items = new LinkedList<GeoCacheOverlayItem>();
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
            items.add(overlayItem);
            int number = overlayItem.getGeoCache().getId();
            if (checkpointNumber < number) {
                checkpointNumber = number;
            }
            setLastFocusedIndex(-1);
            populate();
        }
    }

    public void addCheckpoint(int latitudeE6, int longitudeE6) {
        checkpointNumber++;  
        Controller controller = Controller.getInstance();
        deactivateCheckpoints();
        GeoCache gc = new GeoCache();    
        gc.setName(String.format("%s %d", activity.getString(R.string.checkpoint_dialog_title), checkpointNumber));
        gc.setLocationGeoPoint(new GeoPoint(latitudeE6, longitudeE6));
        gc.setType(GeoCacheType.CHECKPOINT);
        gc.setStatus(GeoCacheStatus.ACTIVE_CHECKPOINT);
        
        gc.setId(checkpointNumber);
        GeoCacheOverlayItem checkpoint = new GeoCacheOverlayItem(gc, "", "");
        addOverlayItem(checkpoint);
        dbm.addCheckpointGeoCache(gc, Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId());
        controller.setSearchingGeoCache(gc);
    }
    
    private void deactivateCheckpoints(){
        for (GeoCacheOverlayItem item : items) {
            item.getGeoCache().setStatus(GeoCacheStatus.NOT_ACTIVE_CHECKPOINT);
        }
    }

    public void removeOverlayItem(int index) {
        GeoCache gc = items.get(index).getGeoCache();
        dbm.deleteCheckpointCache(Controller.getInstance().getPreferencesManager().getLastSearchedGeoCache().getId(), gc.getId());
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
//
//    public void clear() {
//        items.clear();
//        setLastFocusedIndex(-1);
//        populate();
//    }

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
        return i >= items.size() ? -1 : i;
    }

    /**
     * @param activeItem
     *            the activeItem to set
     */
    public void setActiveItem(int activeItem) {
        deactivateCheckpoints();
        items.get(activeItem).getGeoCache().setStatus(GeoCacheStatus.ACTIVE_CHECKPOINT);
    }
}
