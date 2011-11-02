package su.geocaching.android.controller.apimanager;

import android.os.Debug;
import com.google.android.maps.GeoPoint;

public class GeoRect {

    public GeoPoint tl;
    public GeoPoint br;

    public GeoRect(GeoPoint tl, GeoPoint br) {
        //TODO: remove assert
        assert(tl.getLatitudeE6() == br.getLatitudeE6() && tl.getLongitudeE6() == br.getLongitudeE6() );
        this.tl = tl;
        this.br = br;
    }

    public boolean contains(GeoRect rect) {
        if (rect.tl.getLatitudeE6() > tl.getLatitudeE6()) return false;
        if (rect.tl.getLongitudeE6() < tl.getLongitudeE6()) return false;
        if (rect.br.getLatitudeE6() < br.getLatitudeE6()) return false;
        if (rect.br.getLongitudeE6() > br.getLongitudeE6()) return false;
        return true;
    }

    public boolean contains(GeoPoint point) {
        if (point.getLatitudeE6() > tl.getLatitudeE6()) return false;
        if (point.getLongitudeE6() < tl.getLongitudeE6()) return false;
        if (point.getLatitudeE6() < br.getLatitudeE6()) return false;
        if (point.getLongitudeE6() > br.getLongitudeE6()) return false;
        return true;
    }

}
