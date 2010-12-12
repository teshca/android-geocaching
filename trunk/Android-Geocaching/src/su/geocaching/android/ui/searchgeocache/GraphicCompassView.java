package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.ui.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
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

    private int bearingToNorth; // in degrees
    private int bearingToCache; // in degrees
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
	bearingToCache = 0;

	compassBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.compass256);
	cacheBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.cache);
	blueArrowBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.blue_arrow);
	greenArrowBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.green_arrow);
	arrowBitmap = blueArrowBitmap;
	windroseRotateMatrix = new Matrix();
	paint = new Paint();
	paint.setAntiAlias(true);
    }

    @Override
    public void onDraw(Canvas canvas) {
	super.onDraw(canvas);
	// Convert angles relative mobile view direction
	int bearingNorthRel = bearingToNorth;
	int bearingGCRel = bearingToCache - bearingToNorth;

	int center = Math.min(getHeight(), getWidth()) / 2;
	int compassRadius = center / 2;

	float scaleWRPicX = (float) (2 * compassRadius) / compassBitmap.getWidth();
	float scaleWRPicY = (float) (2 * compassRadius) / compassBitmap.getHeight();

	windroseRotateMatrix.setScale(scaleWRPicX, scaleWRPicY);
	windroseRotateMatrix.setRotate(-bearingNorthRel);

	Bitmap windrose = Bitmap.createBitmap(compassBitmap, 0, 0, compassBitmap.getWidth(), compassBitmap.getHeight(), windroseRotateMatrix, true);
	canvas.drawBitmap(windrose, center - windrose.getWidth() / 2, center - windrose.getHeight() / 2, paint);
	drawArrow(canvas, (int) (compassRadius / scaleWRPicY));
	if (isLocationFixed)
	    drawGeoCache(canvas, (int) (compassRadius / scaleWRPicY), bearingGCRel);
    }

    private void drawArrow(Canvas canvas, int radius) {
	int x = (getWidth() - blueArrowBitmap.getWidth()) / 2;
	int y = getHeight() / 2 - radius-blueArrowBitmap.getHeight();
	canvas.drawBitmap(blueArrowBitmap, x, y, paint);
    }

    private void drawGeoCache(Canvas canvas, int radius, int bearingGC) {
	double  bearingRad = (double) bearingGC * Math.PI / 180;
	int cx = (int) (getWidth() / 2 + Math.sin(bearingRad) * radius) - cacheBitmap.getWidth() / 2;
	int cy = (int) (getHeight() / 2 - Math.cos(bearingRad) * radius) - cacheBitmap.getHeight() / 2;
	canvas.drawBitmap(cacheBitmap, cx, cy, paint);
    }

    /**
     * @param angle
     *            - user bearing in degrees
     */
    public void setBearingToNorth(float angle) {
	this.bearingToNorth = (int) angle;
	invalidate();
    }

    /**
     * @param bearingToGeoCache
     *            - bearing to geocache in degrees
     */
    public void setBearingToGeoCache(float bearingToGeoCache) {
	this.bearingToCache = (int) bearingToGeoCache;
	if (bearingToGeoCache < DEFAULT_ARROW_ACCURENCY){
	    arrowBitmap = greenArrowBitmap;
	}
	else arrowBitmap = blueArrowBitmap;
	invalidate();
    }

    public void setLocationFix(boolean isLocationFix) {
	this.isLocationFixed = isLocationFix;
    }
}
