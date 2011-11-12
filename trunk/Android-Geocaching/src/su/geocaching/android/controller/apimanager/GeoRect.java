package su.geocaching.android.controller.apimanager;

import com.google.android.maps.GeoPoint;

public class GeoRect {

    public GeoPoint tl;
    public GeoPoint br;

    public GeoRect(GeoPoint tl, GeoPoint br) {
        assert(tl.getLatitudeE6() < br.getLatitudeE6() || tl.getLongitudeE6() == br.getLongitudeE6() );
        this.tl = tl;
        this.br = br;
    }

    public boolean contains(GeoRect rect) {
        if (rect.tl.getLatitudeE6() > tl.getLatitudeE6()) return false;
        if (rect.br.getLatitudeE6() < br.getLatitudeE6()) return false;

        if (br.getLongitudeE6() > tl.getLongitudeE6()) {
            if (rect.br.getLongitudeE6() > rect.tl.getLongitudeE6()) {
                if (rect.tl.getLongitudeE6() < tl.getLongitudeE6() || rect.br.getLongitudeE6() > br.getLongitudeE6()) return false;
            } else {
                return false;
            }
        } else {
            if (rect.br.getLongitudeE6() > rect.tl.getLongitudeE6()) {
                if (rect.tl.getLongitudeE6() > tl.getLatitudeE6()) return true;
                if (rect.br.getLongitudeE6() < br.getLongitudeE6()) return true;
            } else {
                if ((rect.tl.getLongitudeE6() < tl.getLongitudeE6() && rect.tl.getLongitudeE6() > br.getLongitudeE6())
                    || ((rect.br.getLongitudeE6() < tl.getLongitudeE6() && rect.br.getLongitudeE6() > br.getLongitudeE6())))
                        return false;
            }
        }
        return true;
    }

    public boolean contains(GeoPoint point) {
        if (point.getLatitudeE6() > tl.getLatitudeE6()) return false;
        if (point.getLatitudeE6() < br.getLatitudeE6()) return false;

        if (br.getLongitudeE6() > tl.getLongitudeE6()){
            if (point.getLongitudeE6() < tl.getLongitudeE6() || point.getLongitudeE6() > br.getLongitudeE6()) return false;
        } else {
            // rightLong maybe smaller than leftLong. 4ex 160:-160
            if (point.getLongitudeE6() < tl.getLongitudeE6() && point.getLongitudeE6() > br.getLongitudeE6()) return false;
        }

        return true;
    }

    public boolean TryMergeWith(GeoRect rect)
    {
        // if lat is not the same we can't make merge
        if ((tl.getLatitudeE6() != rect.tl.getLatitudeE6())
                || (br.getLatitudeE6() != rect.tl.getLatitudeE6()))
            return false;

        //TODO: implement
        return true;
    }

    @Override
    public String toString()
    {
        return String.format("%s : %s", tl, br);
    }

}
