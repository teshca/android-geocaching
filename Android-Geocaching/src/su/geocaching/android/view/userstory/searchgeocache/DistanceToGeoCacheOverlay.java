package su.geocaching.android.view.userstory.searchgeocache;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.location.Location;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

import java.text.DecimalFormat;

public class DistanceToGeoCacheOverlay extends com.google.android.maps.Overlay {

    private static final int BIG_DISTANCE_VALUE = 10000; //if distance(m) greater than this - 
    //show (x/1000) km else x m
    private static final DecimalFormat BIG_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0.0");
    private static final DecimalFormat SMALL_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0");
    private static final String BIG_DISTANCE_VALUE_NAME = "km";
    private static final String SMALL_DISTANCE_VALUE_NAME = "m";
    private static final float DEFAULT_TEXT_SIZE = 12;
    private static final int LINE_COLOR = Color.BLUE;
    private static final int TEXT_COLOR = Color.RED;
    private static final float DEFAULT_TEXT_X = 10;
    private static final float DEFAULT_TEXT_Y = 30;

    private GeoPoint userPoint;
    private GeoPoint cachePoint;

    public DistanceToGeoCacheOverlay(GeoPoint userPoint, GeoPoint cachePoint) {
        this.userPoint = userPoint;
        this.cachePoint = cachePoint;
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow,
                        long when) {
        super.draw(canvas, mapView, shadow);
        Projection proj = mapView.getProjection();
        Paint paintLine = new Paint();
        paintLine.setColor(LINE_COLOR);
        paintLine.setAntiAlias(true);
        Paint paintText = new Paint();
        paintText.setColor(TEXT_COLOR);
        paintText.setTextSize(DEFAULT_TEXT_SIZE);
        paintText.setAntiAlias(true);
        paintText.setFakeBoldText(true);
        Point from = new Point();
        Point to = new Point();
        proj.toPixels(userPoint, from);
        proj.toPixels(cachePoint, to);

        canvas.drawLine(from.x, from.y, to.x, to.y, paintLine);

        float dist = getDistanceBetween();
        String text = "";
        if (dist >= BIG_DISTANCE_VALUE) {
            text = BIG_DISTANCE_NUMBER_FORMAT.format(dist / 1000) + " " + BIG_DISTANCE_VALUE_NAME;
        } else {
            text = SMALL_DISTANCE_NUMBER_FORMAT.format(dist) + " " + SMALL_DISTANCE_VALUE_NAME;
        }
        canvas.drawText(text, DEFAULT_TEXT_X, DEFAULT_TEXT_Y, paintText);

        return true;
    }

    private float getDistanceBetween() {
        double begLong = userPoint.getLongitudeE6() / 1E6;
        double begLat = userPoint.getLatitudeE6() / 1E6;

        double endLong = cachePoint.getLongitudeE6() / 1E6;
        double endLat = cachePoint.getLatitudeE6() / 1E6;

        float[] res = new float[3];
        Location.distanceBetween(begLat, begLong, endLat, endLong, res);
        return res[0];
    }

    protected void setUserPoint(GeoPoint userPoint) {
        this.userPoint = userPoint;
    }

    protected void setCachePoint(GeoPoint cachePoint) {
        this.cachePoint = cachePoint;
    }
}
