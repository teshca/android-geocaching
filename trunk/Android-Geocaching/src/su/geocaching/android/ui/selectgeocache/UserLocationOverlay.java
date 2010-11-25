package su.geocaching.android.ui.selectgeocache;

import android.graphics.Canvas;
import android.location.Location;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.MyLocationOverlay;
import su.geocaching.android.utils.Helper;

/**
 * @Author: Yuri Denison
 * @Date: 20.11.10
 */
public class UserLocationOverlay extends MyLocationOverlay {
    private SelectGeoCacheMap context;
    private MapView map;

    @Override
    public void onLocationChanged(Location location) {
        if (location == null) {
            return;
        }

        context.updateCacheOverlay(map.getProjection().fromPixels(0, 0),
                map.getProjection().fromPixels(map.getWidth(), map.getHeight()));
    }

    public UserLocationOverlay(SelectGeoCacheMap arg0, MapView arg1) {
        super(arg0, arg1);
        context = arg0;
        map = arg1;
    }
}
