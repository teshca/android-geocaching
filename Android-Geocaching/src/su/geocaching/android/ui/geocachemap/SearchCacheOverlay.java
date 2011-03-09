package su.geocaching.android.ui.geocachemap;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import su.geocaching.android.model.datatype.GeoCache;
import su.geocaching.android.utils.UiHelper;
import android.app.Activity;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 GeoCache Itemized Overlay for one or more caches
 */
public class SearchCacheOverlay extends ItemizedOverlay<OverlayItem> {

    private List<GeoCacheOverlayItem> items;
    private Activity activity;

    public SearchCacheOverlay(Drawable defaultMarker, Activity context) {
        super(defaultMarker);

        items = Collections.synchronizedList(new LinkedList<GeoCacheOverlayItem>());
        this.activity = context;
        populate();
    }

    public synchronized void addOverlayItem(GeoCacheOverlayItem overlay) {
        if (!contains(overlay.getGeoCache())) {
            items.add(overlay);
            setLastFocusedIndex(-1);
            populate();
        }
    }

    public void removeOverlayItem(int index) {
        items.remove(index);
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
        GeoCache gc = items.get(index).getGeoCache();
        UiHelper.showGeoCacheInfo(activity, gc);
        return true;
    }
}
