package su.geocaching.android.ui.selectmap;

import android.graphics.Canvas;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

/**
 * @author: Yuri Denison
 * @since: 14.07.11
 */
public class SimpleUserLocationOverlay extends com.google.android.maps.Overlay {
    private GeoPoint location;
    private Point userPoint;
    private Drawable userLocationImage;

    SimpleUserLocationOverlay(Drawable userLocationImage)
    {
        this.userLocationImage = userLocationImage;
        this.userPoint = new Point();
    }
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
        if (location != null)
        {
            mapView.getProjection().toPixels(location, userPoint);
            drawAt(canvas, userLocationImage, userPoint.x, userPoint.y, false);
        }
    }

    public void updateLocation(GeoPoint location) {
        this.location = location;
    }
}