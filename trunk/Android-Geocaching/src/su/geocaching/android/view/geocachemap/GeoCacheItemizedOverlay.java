package su.geocaching.android.view.geocachemap;

import java.util.ArrayList;

import android.graphics.drawable.Drawable;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 GeoCache Itemized Overlay for one or more caches
 */
public class GeoCacheItemizedOverlay extends com.google.android.maps.ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> items;

    public GeoCacheItemizedOverlay(Drawable defaultMarker) {
	super(defaultMarker);
	items = new ArrayList<OverlayItem>();
    }

    public void addOverlayItem(OverlayItem overlay) {
	items.add(overlay);
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

    protected OverlayItem update(OverlayItem overlay, GeoPoint point) {
	if ((overlay == null) || (point == null)) {
	    return null;
	}
	OverlayItem overlay_new = new OverlayItem(point, overlay.getTitle(), overlay.getSnippet());
	items.remove(overlay);
	addOverlayItem(overlay_new);
	populate();
	return overlay_new;
    }

    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView, boolean shadow) {
	super.draw(canvas, mapView, false);
    }

}
