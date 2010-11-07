package su.geocaching.android.view.geoCacheMap1;

import android.graphics.drawable.Drawable;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

import java.util.ArrayList;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 GeoCache Itemized Overlay for one or more caches
 */
public class GeoCacheItemizedOverlay extends
        com.google.android.maps.ItemizedOverlay<OverlayItem> {
    private ArrayList<OverlayItem> Overlays;

    public GeoCacheItemizedOverlay(Drawable defaultMarker) {
        super(defaultMarker);
        Overlays = new ArrayList<OverlayItem>();
    }

    public void addOverlay(OverlayItem overlay) {
        Overlays.add(overlay);
        populate();
    }

    protected OverlayItem createItem(int i) {
        return Overlays.get(i);
    }

    @Override
    public int size() {
        return Overlays.size();
    }

    protected OverlayItem update(OverlayItem overlay, GeoPoint point) {
        if ((overlay == null) || (point == null)) {
            return null;
        }
        OverlayItem overlay_new = new OverlayItem(point, overlay.getTitle(),
                overlay.getSnippet());
        Overlays.remove(overlay);
        addOverlay(overlay_new);
        populate();
        return overlay_new;
    }

    @Override
    public void draw(android.graphics.Canvas canvas, MapView mapView,
                     boolean shadow) {
        super.draw(canvas, mapView, false);
    }

}
