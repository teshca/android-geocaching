package su.geocaching.android.ui.searchmap;

import android.graphics.*;
import android.graphics.Paint.Style;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.controller.compass.ICompassAnimation;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since November 2010
 */
public class UserLocationOverlay extends com.google.android.maps.Overlay implements ICompassAnimation {
    private static final int ACCURACY_CIRCLE_ALPHA = 25;
    private static final int COMPASS_ARROW_WIDTH = 28;
    private static final int COMPASS_ARROW_HEIGHT = 34;
    private static final int ACCURACY_CIRCLE_COLOR = 0xff00aa00;
    private static final int ACCURACY_CIRCLE_STROKE_COLOR = 0xff000a00;

    private GeoPoint userGeoPoint;
    private float bearing;
    private float accuracyRadius; // accuracy radius in meters
    private Paint paintCircle;
    private Paint paintStroke;
    private Paint paintCompassArrow;
    private Path pathCompassArrow;
    private SearchGeoCacheMap context;
    private Point tapPoint;
    private Point userPoint;
    private MapView map;
    private long lastTimeInvalidate;

    /**
     * @param context
     *            activity which use this overlay
     */
    public UserLocationOverlay(SearchGeoCacheMap context, MapView map) {
        userGeoPoint = null;
        bearing = Float.NaN;
        accuracyRadius = Float.NaN;

        this.map = map;
        lastTimeInvalidate = -1;

        this.context = context;
        tapPoint = new Point();
        userPoint = new Point();

        paintCircle = new Paint();
        paintCircle.setColor(ACCURACY_CIRCLE_COLOR);
        paintCircle.setAntiAlias(true);
        paintCircle.setAlpha(ACCURACY_CIRCLE_ALPHA);

        paintStroke = new Paint();
        paintStroke.setColor(ACCURACY_CIRCLE_STROKE_COLOR);
        paintStroke.setAntiAlias(true);
        paintStroke.setStyle(Style.STROKE);
        paintStroke.setAlpha(ACCURACY_CIRCLE_ALPHA);

        paintCompassArrow = new Paint();
        paintCompassArrow.setAntiAlias(true);
        paintCompassArrow.setStyle(Style.FILL_AND_STROKE);
        paintCompassArrow.setStrokeWidth(1);
        paintCompassArrow.setARGB(200, 60, 200, 90);

        pathCompassArrow = new Path();
        
        int h = COMPASS_ARROW_HEIGHT / 2; // h/2
        int w = COMPASS_ARROW_WIDTH / 2; // w/2

        pathCompassArrow.lineTo(-w, h);
        pathCompassArrow.lineTo(0, -h);
        pathCompassArrow.lineTo(w, h);
        pathCompassArrow.lineTo(0, 0);
        pathCompassArrow.close();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas, com.google.android.maps.MapView, boolean)
     */
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        if (userGeoPoint == null) {
            return;
        }
        // Translate the GeoPoint to screen pixels
        mapView.getProjection().toPixels(userGeoPoint, userPoint);

        // Draw accuracy circle
        if (!Float.isNaN(accuracyRadius)) {
            float radiusInPixels = mapView.getProjection().metersToEquatorPixels(accuracyRadius);
            if ((radiusInPixels > COMPASS_ARROW_HEIGHT) && (radiusInPixels > COMPASS_ARROW_WIDTH)) {
                canvas.drawCircle(userPoint.x, userPoint.y, radiusInPixels, paintCircle);
                canvas.drawCircle(userPoint.x, userPoint.y, radiusInPixels, paintStroke);
            }
        }

        canvas.save();
        canvas.translate(userPoint.x, userPoint.y);
        canvas.rotate(bearing);
        canvas.drawPath(pathCompassArrow, paintCompassArrow);
        canvas.restore();      
    }

    /**
     * @param point
     *            set user location
     */
    public void setPoint(GeoPoint point) {
        this.userGeoPoint = point;
        postInvalidate();
    }

    /**
     * @param radius
     *            set accuracy radius of location point
     */
    public void setAccuracy(float radius) {
        this.accuracyRadius = radius;
        postInvalidate();
    }

    /*
     * (non-Javadoc)
     * 
     * @see su.geocaching.android.controller.compass.ICompassAnimation#setDirection(float)
     */
    @Override
    public boolean setDirection(float direction) {
        bearing = -direction;
        postInvalidate();
        return true;
    }

    private void postInvalidate() {
        if (userGeoPoint == null || System.currentTimeMillis() - lastTimeInvalidate < 50) {
            return;
        }
        map.getProjection().toPixels(userGeoPoint, userPoint);
        int max = Math.max(COMPASS_ARROW_HEIGHT, COMPASS_ARROW_WIDTH);
        map.postInvalidate(userPoint.x - max, userPoint.y - max, userPoint.x + max, userPoint.y + max);
        lastTimeInvalidate = System.currentTimeMillis();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.Overlay#onTap(com.google.android.maps.GeoPoint, com.google.android.maps.MapView)
     */
    @Override
    public boolean onTap(GeoPoint p, MapView map) {
        if (!Controller.getInstance().getLocationManager().hasLocation()) {
            return super.onTap(p, map);
        }
        map.getProjection().toPixels(p, tapPoint);
        if ((Math.abs(tapPoint.x - userPoint.x) < COMPASS_ARROW_WIDTH / 2) && (Math.abs((tapPoint.y - userPoint.y)) < COMPASS_ARROW_HEIGHT / 2)) {
            UiHelper.startCompassActivity(context);
            return true;
        }
        return false;
    }
}
