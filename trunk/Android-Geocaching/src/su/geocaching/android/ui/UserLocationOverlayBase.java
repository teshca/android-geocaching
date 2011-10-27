package su.geocaching.android.ui;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import su.geocaching.android.controller.utils.CoordinateHelper;

/**
 * @author: Nickolay Artamonov
 * @since: 14.07.11
 */
public abstract class UserLocationOverlayBase extends com.google.android.maps.Overlay {
    private GeoPoint position;
    private Float accuracyRadiusInMeters;
    protected Float accuracyRadiusInPixels;

    private Paint paintStroke;
    private Paint paintCircle;

    protected Point userPoint;
    private Point tapPoint;
    protected int tapDistance;
    protected boolean locationAvailable;

    private static final int ACCURACY_CIRCLE_ALPHA = 25;
    private static final int ACCURACY_CIRCLE_COLOR = 0xff00aa00;
    private static final int ACCURACY_CIRCLE_STROKE_COLOR = 0xff000a00;

    public UserLocationOverlayBase() {
        userPoint = new Point();

        paintCircle = new Paint();
        paintCircle.setColor(ACCURACY_CIRCLE_COLOR);
        paintCircle.setAntiAlias(true);
        paintCircle.setAlpha(ACCURACY_CIRCLE_ALPHA);

        paintStroke = new Paint();
        paintStroke.setColor(ACCURACY_CIRCLE_STROKE_COLOR);
        paintStroke.setAntiAlias(true);
        paintStroke.setStyle(Paint.Style.STROKE);
        paintStroke.setAlpha(ACCURACY_CIRCLE_ALPHA);

        tapDistance = 24;
        tapPoint = new Point();

        accuracyRadiusInMeters = Float.NaN;
    }

    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);
        if (locationAvailable) {
            // Translate the GeoPoint to screen pixels
            mapView.getProjection().toPixels(position, userPoint);
            if (!Float.isNaN(accuracyRadiusInMeters)) {
                accuracyRadiusInPixels = mapView.getProjection().metersToEquatorPixels(accuracyRadiusInMeters);
                drawAccuracyCircle(canvas);
            }
            drawUserLocation(canvas);
        }
    }

    protected abstract void drawUserLocation(Canvas canvas);

    protected void drawAccuracyCircle(Canvas canvas) {
        canvas.drawCircle(userPoint.x, userPoint.y, accuracyRadiusInPixels, paintCircle);
        canvas.drawCircle(userPoint.x, userPoint.y, accuracyRadiusInPixels, paintStroke);
    }

    public void updateLocation(Location location) {
        locationAvailable = location != null;
        if (locationAvailable)
        {
            this.position = CoordinateHelper.locationToGeoPoint(location);
            this.accuracyRadiusInMeters = location.getAccuracy();
        }
    }

    /*
     * (non-Javadoc)
     *
     * @see com.google.android.maps.Overlay#onTap(com.google.android.maps.GeoPoint, com.google.android.maps.MapView)
     */
    @Override
    public boolean onTap(GeoPoint p, MapView map) {
        if (!locationAvailable) {
            return super.onTap(p, map);
        }
        map.getProjection().toPixels(p, tapPoint);
        if ((Math.abs(tapPoint.x - userPoint.x) < tapDistance) && (Math.abs((tapPoint.y - userPoint.y)) < tapDistance)) {
            onTapAction();
            return true;
        }
        return false;
    }

    protected void onTapAction(){

    }
}