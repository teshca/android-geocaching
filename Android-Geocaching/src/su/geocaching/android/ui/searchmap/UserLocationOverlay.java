package su.geocaching.android.ui.searchmap;

import android.graphics.*;
import android.graphics.Paint.Style;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.LogManager;
import su.geocaching.android.controller.UiHelper;
import su.geocaching.android.controller.compass.ICompassAnimation;
import su.geocaching.android.ui.R;

/**
 * @author Grigory Kalabin. grigory.kalabin@gmail.com
 * @since November 2010
 */
public class UserLocationOverlay extends com.google.android.maps.Overlay implements ICompassAnimation {
    private static final int ACCURACY_CIRCLE_ALPHA = 25;
    private static final int ACCURACY_CIRCLE_COLOR = 0xff00aa00;
    private static final int ACCURACY_CIRCLE_STROKE_COLOR = 0xff000a00;

    private GeoPoint userGeoPoint;
    private float bearing;
    private float accuracyRadius; // accuracy radius in meters
    private Paint paintCircle;
    private Paint paintStroke;
    private Bitmap userArrowBmp;
    private Bitmap userPointBmp;
    private Matrix matrix;
    private SearchGeoCacheMap context;
    private Point tapPoint;
    private Point userPoint;
    private int userBitmapWidth;
    private int userBitmapHeight;
    private MapView map;
    private long lastTimeDrawing;

    /**
     * @param context
     *            activity which use this overlay
     */
    public UserLocationOverlay(SearchGeoCacheMap context, MapView map) {
        userGeoPoint = null;
        bearing = Float.NaN;
        accuracyRadius = Float.NaN;

        this.map = map;
        lastTimeDrawing = -1;

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

        userPointBmp = BitmapFactory.decodeResource(Controller.getInstance().getResourceManager().getResources(), R.drawable.userpoint);
        userArrowBmp = BitmapFactory.decodeResource(Controller.getInstance().getResourceManager().getResources(), R.drawable.userarrow);
        matrix = new Matrix();
    }

    /*
     * (non-Javadoc)
     * 
     * @see com.google.android.maps.Overlay#draw(android.graphics.Canvas, com.google.android.maps.MapView, boolean, long)
     */
    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        if (userGeoPoint == null) {
            return;
        }
        lastTimeDrawing = System.currentTimeMillis();
        // Translate the GeoPoint to screen pixels
        mapView.getProjection().toPixels(userGeoPoint, userPoint);

        // Prepare to draw default marker
        // Bitmap userBitmapPoint;
        // if (Float.isNaN(bearing)) {
        // userBitmapPoint = userPointBmp;
        // } else {
        // userBitmapPoint = userArrowBmp;
        // Rotate default marker
        // matrix.setRotate(bearing);
        // userBitmapPoint = Bitmap.createBitmap(userBitmapPoint, 0, 0, userBitmapPoint.getWidth(), userBitmapPoint.getHeight(), matrix, true);
        // }
        // userBitmapWidth = userBitmapPoint.getWidth();
        // userBitmapHeight = userBitmapPoint.getHeight();

        // Draw accuracy circle
        if (!Float.isNaN(accuracyRadius)) {
            float radiusInPixels = mapView.getProjection().metersToEquatorPixels(accuracyRadius);
            // if ((radiusInPixels > userBitmapPoint.getWidth()) && (radiusInPixels > userBitmapPoint.getHeight())) {
            canvas.drawCircle(userPoint.x, userPoint.y, radiusInPixels, paintCircle);
            canvas.drawCircle(userPoint.x, userPoint.y, radiusInPixels, paintStroke);
            // }
        }

        int h = 17; // h/2
        int w = 14; // w/2
        double s = Math.sin(-bearing * Math.PI / 180);
        double c = Math.cos(-bearing * Math.PI / 180);

        Path arrowPath = new Path();
        float topX = (float) (-h * s + userPoint.x);
        float topY = (float) (-h * c + userPoint.y);
        float bottomLeftX = (float) (-w * c + h * s + userPoint.x);
        float bottomLeftY = (float) (w * s + h * c + userPoint.y);
        float bottomRightX = (float) (w * c + h * s + userPoint.x);
        float bottomRightY = (float) (-w * s + h * c + userPoint.y);

        arrowPath.moveTo(userPoint.x, userPoint.y);
        arrowPath.lineTo(bottomLeftX, bottomLeftY);
        arrowPath.lineTo(topX, topY);
        arrowPath.lineTo(bottomRightX, bottomRightY);
        arrowPath.lineTo(userPoint.x, userPoint.y);
        arrowPath.close();

        Paint paint = new Paint();
        paint.setAntiAlias(true);
        paint.setStyle(Style.FILL_AND_STROKE);
        paint.setStrokeWidth(1);

        paint.setARGB(200, 60, 200, 90);
        canvas.drawPath(arrowPath, paint);

        // Draw default marker
        // canvas.drawBitmap(userBitmapPoint, userPoint.x - userBitmapPoint.getWidth() / 2, userPoint.y - userBitmapPoint.getHeight() / 2, null);
        return;
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
        bearing = direction;
        postInvalidate();
        return true;
    }

    private void postInvalidate() {
        if (userGeoPoint == null || System.currentTimeMillis() - lastTimeDrawing < 100) {
            return;
        }
        map.postInvalidate();
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
        if ((Math.abs(tapPoint.x - userPoint.x) < userBitmapWidth / 2) && (Math.abs((tapPoint.y - userPoint.y)) < userBitmapHeight / 2)) {
            UiHelper.startCompassActivity(context);
            return true;
        }
        return false;
    }
}
