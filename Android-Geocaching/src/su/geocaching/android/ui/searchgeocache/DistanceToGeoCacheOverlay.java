package su.geocaching.android.ui.searchgeocache;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class DistanceToGeoCacheOverlay extends com.google.android.maps.Overlay {

    private static final int LINE_COLOR = Color.BLUE;

    private GeoPoint userPoint;
    private GeoPoint cachePoint;
    private Paint paintLine;
    private boolean withShortestWay;

    public DistanceToGeoCacheOverlay(GeoPoint userPoint, GeoPoint cachePoint) {
        this.userPoint = userPoint;
        this.cachePoint = cachePoint;
        withShortestWay = true;

        paintLine = new Paint();
        paintLine.setColor(LINE_COLOR);
        paintLine.setAntiAlias(true);
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
        super.draw(canvas, mapView, shadow, when);

        if (withShortestWay) {
            Point from = new Point();
            Point to = new Point();
            Projection proj = mapView.getProjection();
            proj.toPixels(userPoint, from);
            proj.toPixels(cachePoint, to);
            canvas.drawLine(from.x, from.y, to.x, to.y, paintLine);
        }

        return true;
    }

    protected void setUserPoint(GeoPoint userPoint) {
        this.userPoint = userPoint;
    }

    protected void setCachePoint(GeoPoint cachePoint) {
        this.cachePoint = cachePoint;
    }

    protected void setShorteshtWayVisible(boolean with) {
        withShortestWay = with;
    }
}
