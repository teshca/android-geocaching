package su.geocaching.android.controller.apimanager;

import su.geocaching.android.model.GeoPoint;

public class GeoRect {

    public GeoPoint tl;
    public GeoPoint br;

    public GeoRect(GeoPoint tl, GeoPoint br) {
        assert (tl.getLatitude() < br.getLatitude() || tl.getLongitude() == br.getLongitude());
        this.tl = tl;
        this.br = br;
    }

    public boolean contains(GeoRect rect) {
        if (rect.tl.getLatitude() > tl.getLatitude()) return false;
        if (rect.br.getLatitude() < br.getLatitude()) return false;

        if (br.getLongitude() > tl.getLongitude()) {
            if (rect.br.getLongitude() > rect.tl.getLongitude()) {
                if (rect.tl.getLongitude() < tl.getLongitude() || rect.br.getLongitude() > br.getLongitude())
                    return false;
            } else {
                return false;
            }
        } else {
            if (rect.br.getLongitude() > rect.tl.getLongitude()) {
                if (rect.tl.getLongitude() > tl.getLatitude()) return true;
                if (rect.br.getLongitude() < br.getLongitude()) return true;
            } else {
                if ((rect.tl.getLongitude() < tl.getLongitude() && rect.tl.getLongitude() > br.getLongitude())
                        || ((rect.br.getLongitude() < tl.getLongitude() && rect.br.getLongitude() > br.getLongitude())))
                    return false;
            }
        }
        return true;
    }

    public boolean contains(GeoPoint point) {
        if (point.getLatitude() > tl.getLatitude()) return false;
        if (point.getLatitude() < br.getLatitude()) return false;

        if (br.getLongitude() > tl.getLongitude()) {
            if (point.getLongitude() < tl.getLongitude() || point.getLongitude() > br.getLongitude()) return false;
        } else {
            // rightLong maybe smaller than leftLong. 4ex 160:-160
            if (point.getLongitude() < tl.getLongitude() && point.getLongitude() > br.getLongitude()) return false;
        }

        return true;
    }

    @Override
    public String toString() {
        return String.format("%s : %s", tl, br);
    }

}
