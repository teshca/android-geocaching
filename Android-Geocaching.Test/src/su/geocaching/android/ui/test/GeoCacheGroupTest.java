package su.geocaching.android.ui.test;

import android.test.AndroidTestCase;
import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.selectgeocache.SelectGeoCacheMap;

/**
 * @author: Yuri Denison
 * @since: 25.02.11
 */
public class GeoCacheGroupTest extends AndroidTestCase {

    public void testGroup() {
        Controller.getInstance().updateSelectedGeoCaches(
            new SelectGeoCacheMap(),
            new GeoPoint(71873449, 13037433),
            new GeoPoint(40622590, 55224933));
    }
}
