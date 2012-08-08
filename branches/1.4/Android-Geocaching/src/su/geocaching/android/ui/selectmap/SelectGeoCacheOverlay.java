package su.geocaching.android.ui.selectmap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import android.content.Context;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.view.GestureDetector;
import android.view.MotionEvent;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.ui.OverlayUtils;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 GeoCache Itemized Overlay for one or more caches
 */

class SelectGeoCacheOverlay extends ItemizedOverlay<GeoCacheOverlayItem> {
    private static final String TAG = SelectGeoCacheOverlay.class.getCanonicalName();
    private final List<GeoCacheOverlayItem> items;
    private final MapView map;
    private final GestureDetector gestureDetector;

    public SelectGeoCacheOverlay(Drawable defaultMarker, final Context context, final MapView mapView) {
        super(defaultMarker);
        items = Collections.synchronizedList(new LinkedList<GeoCacheOverlayItem>());
        this.map = mapView;
        populate();

        gestureDetector = new GestureDetector(context, new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                map.getController().zoomInFixing((int) e.getX(), (int) e.getY());
                return true;
            }

            public boolean onSingleTapConfirmed(MotionEvent e) {
                for (GeoCacheOverlayItem gcItem : items) {
                    if (hitTest(e, gcItem))
                    {
                        if (!gcItem.getTitle().equals("Group")) {
                            NavigationManager.startInfoActivity(context, gcItem.getGeoCache());
                        } else {
                            Point p = map.getProjection().toPixels(gcItem.getGeoCache().getLocationGeoPoint(), null);
                            map.getController().zoomInFixing(p.x, p.y);
                            map.invalidate();
                        }
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
    }

    public void AddOverlayItems(List<GeoCacheOverlayItem> overlayItemList) {
        for (GeoCacheOverlayItem overlayItem : overlayItemList) {
            items.add(overlayItem);
        }
        setLastFocusedIndex(-1);
        populate();
    }

    @Override
    protected GeoCacheOverlayItem createItem(int i) {
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
        if (OverlayUtils.isMultiTouch(event)) return false;

        return gestureDetector.onTouchEvent(event);
    }
}
