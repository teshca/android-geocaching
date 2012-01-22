package su.geocaching.android.ui.searchmap;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;

import android.view.GestureDetector;
import android.view.MotionEvent;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class DistanceToGeoCacheOverlay extends com.google.android.maps.Overlay {

    private GeoPoint userPoint;
    private GeoPoint cachePoint;
    private Paint paintLine;
    private Point to;
    private Point from;
    private static final int DISTANCE_STROKE_COLOR = 0x64641464;
    private static final int DISTANCE_STROKE_WIDTH = 4;

    private final GestureDetector gestureDetector;

    public DistanceToGeoCacheOverlay(GeoPoint userPoint, GeoPoint cachePoint, final MapView mapView) {
        this.userPoint = userPoint;
        this.cachePoint = cachePoint;
        from = new Point();
        to = new Point();
        paintLine = new Paint();
        paintLine.setStyle(Paint.Style.STROKE);
        paintLine.setColor(DISTANCE_STROKE_COLOR);
        paintLine.setAntiAlias(true);
        paintLine.setStrokeWidth(DISTANCE_STROKE_WIDTH);

        gestureDetector = new GestureDetector(new GestureDetector.SimpleOnGestureListener() {
            public boolean onDoubleTap(MotionEvent e) {
                mapView.getController().zoomInFixing((int) e.getX(), (int) e.getY());
                return true;
            }
        });
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        Projection proj = mapView.getProjection();
        proj.toPixels(userPoint, from);
        proj.toPixels(cachePoint, to);
        canvas.drawLine(from.x, from.y, to.x, to.y, paintLine);
    }

    protected void setUserPoint(GeoPoint userPoint) {
        this.userPoint = userPoint;
    }

    public void setCachePoint(GeoPoint cachePoint) {
        this.cachePoint = cachePoint;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        return gestureDetector.onTouchEvent(event);
    }
}
