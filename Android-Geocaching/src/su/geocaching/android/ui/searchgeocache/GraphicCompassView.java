package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.ui.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

/**
 * View which displays compass contains of bitmaps for searching geocache.
 * 
 * @author Android-Geocaching.su student project team
 * @since October 2010
 */
public class GraphicCompassView extends View {

    private static final int DEFAULT_ARROW_ACCURENCY = 10;
    // private static String TAG = GraphicCompassView.class.getCanonicalName();

    private int bearingToNorth; // in degrees
    private int absoluteBearingToCache, relativeBearingToCache; // in degrees
    private int compassRadius;
    private boolean isLocationFixed = false;

    private Matrix windroseRotateMatrix;
    private Bitmap compassBitmap, cacheBitmap, blueArrowBitmap, greenArrowBitmap, arrowBitmap;;
    private Paint paint;

    public GraphicCompassView(Context context) {
	this(context, null, 0);
    }

    public GraphicCompassView(Context context, AttributeSet attrs) {
	this(context, attrs, 0);
    }

    public GraphicCompassView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
	initParameters();
    }

    private void initParameters() {
	bearingToNorth = 0;
	relativeBearingToCache = absoluteBearingToCache = 0;

	compassBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.compass256);
	cacheBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cache);
	blueArrowBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.blue_arrow);
	greenArrowBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.green_arrow);
	arrowBitmap = blueArrowBitmap;
	windroseRotateMatrix = new Matrix();
	paint = new Paint();
	paint.setAntiAlias(true);
	compassRadius = compassBitmap.getHeight() / 2;
    }

    @Override
    public void onDraw(Canvas canvas) {
	super.onDraw(canvas);
	windroseRotateMatrix.setRotate(bearingToNorth);

	Bitmap windrose = Bitmap.createBitmap(compassBitmap, 0, 0, compassBitmap.getWidth(), compassBitmap.getHeight(), windroseRotateMatrix, true);
	canvas.drawBitmap(windrose, (getWidth() - windrose.getWidth()) / 2, (getHeight() - windrose.getHeight()) / 2, paint);
	drawArrow(canvas);
	if (isLocationFixed) {
	    drawGeoCache(canvas);
	}
    }

    private void drawArrow(Canvas canvas) {
	if (isLocationFixed && Math.abs(relativeBearingToCache) < DEFAULT_ARROW_ACCURENCY) {
	    arrowBitmap = greenArrowBitmap;
	} else {
	    arrowBitmap = blueArrowBitmap;
	}
	int x = (getWidth() - blueArrowBitmap.getWidth()) / 2;
	int y = getHeight() / 2 - compassRadius - blueArrowBitmap.getHeight();
	canvas.drawBitmap(arrowBitmap, x, y, paint);
    }

    private void drawGeoCache(Canvas canvas) {
	double bearingRad = (double) (relativeBearingToCache * Math.PI) / 180;
	int cx = (int) ((getWidth() - cacheBitmap.getWidth()) / 2 + Math.sin(bearingRad) * compassRadius * 0.9);
	int cy = (int) ((getHeight() - cacheBitmap.getHeight()) / 2 - Math.cos(bearingRad) * compassRadius * 0.9);
	canvas.drawBitmap(cacheBitmap, cx, cy, paint);
    }

    /**
     * @param angle
     *            - user bearing in degrees
     */
    public void setBearingToNorth(float angle) {
	bearingToNorth = (int) -angle;
	relativeBearingToCache = absoluteBearingToCache + bearingToNorth;
	invalidate();
    }

    /**
     * @param bearingToGeoCache
     *            - bearing to geocache in degrees
     */
    public void setBearingToGeoCache(float bearingToGeoCache) {
	this.absoluteBearingToCache = (int) bearingToGeoCache;
	relativeBearingToCache = absoluteBearingToCache + bearingToNorth;
	invalidate();
    }

    public void setLocationFix(boolean isLocationFix) {
	this.isLocationFixed = isLocationFix;
    }
}
