package su.geocaching.android.controller.apimanager;

import su.geocaching.android.controller.managers.LogManager;
import su.geocaching.android.model.GeoCache;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class GeoCacheMemoryStorage {
    private HashSet<GeoCache> geoCaches = new HashSet<GeoCache>();
    private List<GeoRect> rectangles = new LinkedList<GeoRect>();

    private static final String TAG = GeoCacheMemoryStorage.class.getCanonicalName();

    public boolean isRectangleStored(GeoRect newRect)
    {
        for(GeoRect rect : rectangles)
        {
            if (rect.contains(newRect)) return true;
        }
        return false;
    }

    private void addRectangle(GeoRect newRect)
    {
        // remove old rectangles if new rectangle covers them
        Iterator<GeoRect> it = rectangles.iterator();
        while(it.hasNext()) {
          GeoRect rect = it.next();
          if(newRect.contains(rect)) it.remove();
        }
        // add new rectangle
        rectangles.add(newRect);
    }

    public List<GeoCache> getCaches(GeoRect rect)
    {
        List<GeoCache> filteredGeoCaches = new LinkedList<GeoCache>();
        for (GeoCache gc : geoCaches) {
            if (rect.contains(gc.getLocationGeoPoint())) {
                filteredGeoCaches.add(gc);
            }
        }
        LogManager.d(TAG, "Number of geocaches on the screen: %d", filteredGeoCaches.size());
        return filteredGeoCaches;
    }

    public void addCaches(List<GeoCache> newCaches, GeoRect rect)
    {
        geoCaches.addAll(newCaches);
        addRectangle(rect);
        LogManager.d(TAG, "%d caches added. Total size of memory cached geocaches: %d", newCaches.size(), geoCaches.size());
    }
}

