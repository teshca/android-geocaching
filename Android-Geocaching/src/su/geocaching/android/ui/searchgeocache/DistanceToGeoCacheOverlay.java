package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.utils.GpsHelper;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Projection;

public class DistanceToGeoCacheOverlay extends com.google.android.maps.Overlay {

    private static final float DEFAULT_TEXT_SIZE = 12;
    private static final int LINE_COLOR = Color.BLUE;
    private static final int TEXT_COLOR = Color.RED;
    private static final float DEFAULT_TEXT_X = 10;
    private static final float DEFAULT_TEXT_Y = 30;

    private GeoPoint userPoint;
    private GeoPoint cachePoint;
    private boolean withShortestWay;

    public DistanceToGeoCacheOverlay(GeoPoint userPoint, GeoPoint cachePoint) {
	this.userPoint = userPoint;
	this.cachePoint = cachePoint;
	withShortestWay = true;
    }

    @Override
    public boolean draw(Canvas canvas, MapView mapView, boolean shadow, long when) {
	super.draw(canvas, mapView, shadow);

	Paint paintText = new Paint();
	paintText.setColor(TEXT_COLOR);
	paintText.setTextSize(DEFAULT_TEXT_SIZE);
	paintText.setAntiAlias(true);
	paintText.setFakeBoldText(true);
	if (withShortestWay) {
	    Point from = new Point();
	    Point to = new Point();
	    Projection proj = mapView.getProjection();
	    Paint paintLine = new Paint();
	    paintLine.setColor(LINE_COLOR);
	    paintLine.setAntiAlias(true);
	    proj.toPixels(userPoint, from);
	    proj.toPixels(cachePoint, to);
	    canvas.drawLine(from.x, from.y, to.x, to.y, paintLine);
	}

	float dist = GpsHelper.getDistanceBetween(userPoint, cachePoint);
	canvas.drawText(GpsHelper.distanceToString(dist), DEFAULT_TEXT_X, DEFAULT_TEXT_Y, paintText);

	return true;
    }

    protected void setUserPoint(GeoPoint userPoint) {
	this.userPoint = userPoint;
    }

    protected void setCachePoint(GeoPoint cachePoint) {
	this.cachePoint = cachePoint;
    }
    
    protected void setShorteshtWayVisible(boolean with) {
	withShortestWay=with;
    }
}
