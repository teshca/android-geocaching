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
 * @author Android-Geocaching.su student project team
 * @since October 2010
 *        <p>
 *        View which displays compass contains of bitmaps for searching
 *        geocache.
 *        </p>
 */
public class GraphicCompassView extends View {

    private static final int DEFAULT_PADDING = 0;

    private int bearingToNorth; // in degrees
    private int bearingToCache; // in degrees

    private Matrix windroseRotateMatrix;
    private Bitmap compassBitmap, cacheBitmap;
    private Paint paint, arrowPaint;
    private Path arrowPath;

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
	windroseRotateMatrix = new Matrix();
	paint = new Paint();
	paint.setAntiAlias(true);
	arrowPaint = new Paint();
	arrowPaint.setAntiAlias(true);
	arrowPaint.setColor(Color.GREEN);
	arrowPaint.setStyle(Paint.Style.FILL_AND_STROKE);
	arrowPath = new Path();
    }

    @Override
    public void onDraw(Canvas canvas) {
	super.onDraw(canvas);
	// Convert angles relative mobile view direction
	int bearingNorthRel = bearingToNorth;
	int bearingGCRel = bearingToCache - bearingToNorth;

	int center = Math.min(getHeight(), getWidth()) / 2;
	int compassRadius = center / 2 - DEFAULT_PADDING;

	float scaleWRPicX = (float) (2 * compassRadius) / compassBitmap.getWidth();
	float scaleWRPicY = (float) (2 * compassRadius) / compassBitmap.getHeight();

	windroseRotateMatrix.setScale(scaleWRPicX, scaleWRPicY);
	windroseRotateMatrix.setRotate(-bearingNorthRel);

	Bitmap windrose = Bitmap.createBitmap(compassBitmap, 0, 0, compassBitmap.getWidth(), compassBitmap.getHeight(), windroseRotateMatrix, false);
	canvas.drawBitmap(windrose, center - windrose.getWidth() / 2, center - windrose.getHeight() / 2, paint);
	drawArrow(canvas, (int) (compassRadius / scaleWRPicY));
	drawGeoCache(canvas, (int) (compassRadius / scaleWRPicY), bearingGCRel);
    }

    // TODO correct arrow
    private void drawArrow(Canvas canvas, int radius) {
	arrowPath.reset();
	int x = getWidth() / 2;
	int y = getHeight() / 2 - radius;
	arrowPath.moveTo(x, y);
	x -= 10;
	y -= 15;
	arrowPath.lineTo(x, y);
	x += 20;
	arrowPath.lineTo(x, y);
	x -= 10;
	y += 15;
	arrowPath.lineTo(x, y);
	canvas.drawPath(arrowPath, arrowPaint);

	Rect r = new Rect(x - 5, 1, x + 5, y - 15);
	canvas.drawRect(r, arrowPaint);
    }

    private void drawGeoCache(Canvas canvas, int radius, int bearingGC) {
	int cx = (int) (getWidth() / 2 + Math.sin(bearingGC * Math.PI / 180) * radius) - cacheBitmap.getWidth() / 2;
	int cy = (int) (getHeight() / 2 - Math.cos(bearingGC * Math.PI / 180) * radius) - cacheBitmap.getHeight() / 2;
	canvas.drawBitmap(cacheBitmap, cx, cy, arrowPaint);	
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
	invalidate();
    }
}
