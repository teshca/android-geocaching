package su.geocaching.android.ui.selectmap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 GeoCache Itemized Overlay for one or more caches
 */
class SelectGeoCacheOverlay extends com.google.android.maps.ItemizedOverlay<OverlayItem> {
    private final List<GeoCacheOverlayItem> items;
    private final Context context;
    private final MapView map;
    private boolean multiTouchFlag = false;
    private final GestureDetector gestureDetector;

    public SelectGeoCacheOverlay(Drawable defaultMarker, Context context, final MapView map) {
        super(defaultMarker);
        items = Collections.synchronizedList(new LinkedList<GeoCacheOverlayItem>());
        this.context = context;
        this.map = map;
        populate();

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                map.getController().zoomInFixing((int) e.getX(), (int) e.getY());
                return true;
            }
        });
    }

    public synchronized void addOverlayItem(GeoCacheOverlayItem overlayItem) {
        if (!contains(overlayItem.getGeoCache()) || overlayItem.getTitle().equals("Group")) {
            items.add(overlayItem);
            setLastFocusedIndex(-1);
            populate();
        }
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

    public synchronized void clear() {
        items.clear();
        setLastFocusedIndex(-1);
        populate();
    }

    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, false);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView map) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            multiTouchFlag = false;
        }
        
        try {
            Method getPointer = MotionEvent.class.getMethod("getPointerCount");
            if (Integer.parseInt(getPointer.invoke(event).toString()) > 1) {
                multiTouchFlag = true;
            }
            /* success, this is a newer device */
        } catch (NoSuchMethodException e) {
            /* failure, must be older device */
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        return gestureDetector.onTouchEvent(event);
    }

    @Override
    public boolean onTap(int index) {
        if (!multiTouchFlag) {
            GeoCacheOverlayItem gcItem = items.get(index);
            if (!gcItem.getTitle().equals("Group")) {
                NavigationManager.startInfoActivity(context, gcItem.getGeoCache());
            } else {
                Point p = map.getProjection().toPixels(gcItem.getGeoCache().getLocationGeoPoint(), null);
                map.getController().zoomInFixing(p.x, p.y);
                map.invalidate();
            }
        }
        return true;
    }
}
