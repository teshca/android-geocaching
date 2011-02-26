package su.geocaching.android.ui.geocachemap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.utils.UiHelper;
import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 GeoCache Itemized Overlay for one or more caches
 */
public class SelectCacheOverlay extends com.google.android.maps.ItemizedOverlay<OverlayItem> {
    private List<GeoCacheOverlayItem> items;
    private Context context;
    private MapView map;

    public SelectCacheOverlay(Drawable defaultMarker, Context context, MapView map) {
        super(defaultMarker);
        items = Collections.synchronizedList(new LinkedList<GeoCacheOverlayItem>());
        this.context = context;
        this.map = map;
        populate();
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
    public boolean onTap(int index) {
    	GeoCacheOverlayItem gcItem = items.get(index);
    	   if (!gcItem.getTitle().equals("Group")) {
               UiHelper.showGeoCacheInfo(context, gcItem.getGeoCache());
           } else {
        	   map.getController().animateTo(gcItem.getGeoCache().getLocationGeoPoint());
        	   map.getController().zoomIn();
               map.invalidate();
           }
        return true;    
    }    
}
