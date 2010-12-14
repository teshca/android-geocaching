package su.geocaching.android.ui.geocachemap;

import android.graphics.drawable.Drawable;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

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

    public void addOverlayItem(GeoCacheOverlayItem overlay) {
	if(!items.contains(overlay)) {
            items.add(overlay);
            populate();
        }
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

}
