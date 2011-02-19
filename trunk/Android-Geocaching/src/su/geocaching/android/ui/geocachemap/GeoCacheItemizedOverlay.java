package su.geocaching.android.ui.geocachemap;

import android.graphics.drawable.Drawable;
import android.util.Log;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;
import su.geocaching.android.model.datatype.GeoCache;

import java.util.ArrayList;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 GeoCache Itemized Overlay for one or more caches
 */
public class GeoCacheItemizedOverlay extends com.google.android.maps.ItemizedOverlay<OverlayItem> {
    private ArrayList<GeoCacheOverlayItem> items;
    private IMapAware context;

    public GeoCacheItemizedOverlay(Drawable defaultMarker, IMapAware context) {
        super(defaultMarker);
        items = new ArrayList<GeoCacheOverlayItem>();
        this.context = context;
        populate();
    }

    public synchronized void addOverlayItem(GeoCacheOverlayItem overlay) {
        if (!contains(overlay.getGeoCache()) || overlay.getTitle().equals("Group")) {
            Log.d("SelectGeoCacheMap", "adding overlay, title = " + overlay.getTitle());
            items.add(overlay);
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

    public synchronized void remove(GeoCacheOverlayItem item) {
        items.remove(item);
        populate();
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
    }

    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, false);
    }

    @Override
    public boolean onTap(int index) {
        context.onGeoCacheItemTaped(items.get(index));
        return true;
    }

    public void removeGroupItems() {
        for (GeoCacheOverlayItem item : items) {
            if (item.getTitle().equals("Group")) {
                items.remove(item);
            }
        }
        populate();
    }
}
