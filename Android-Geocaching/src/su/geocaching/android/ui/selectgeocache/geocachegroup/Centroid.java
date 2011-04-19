package su.geocaching.android.ui.selectgeocache.geocachegroup;

import su.geocaching.android.model.GeoCache;

/**
 * @author: Yuri Denison
 * @since: 25.02.11
 */

public class Centroid extends GeoCacheView {
    private int numberOfView;
    private int newX;
    private int newY;

    public Centroid(int x, int y, GeoCache cache) {
        super(x, y, cache);
        numberOfView = 0;
    }

    public int getNumberOfView() {
        return numberOfView;
    }

    public void plusNew(int x, int y) {
        newX += x;
        newY += y;
        numberOfView++;
    }

    public void setNew() {
        if (numberOfView == 0) {
            return;
        }
        this.set(
                (newX / numberOfView),
                (newY / numberOfView)
        );
        newX = 0;
        newY = 0;
        numberOfView = 0;
    }
}
