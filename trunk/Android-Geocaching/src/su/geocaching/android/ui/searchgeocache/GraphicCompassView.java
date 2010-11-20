package su.geocaching.android.ui.searchgeocache;

import su.geocaching.android.ui.R;
import su.geocaching.android.utils.Helper;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 * @description View which displays compass contains of bitmaps for searching
 *              geocache.
 */
public class GraphicCompassView extends View {

    private static final String TAG = GraphicCompassView.class.getCanonicalName();

    private static final int DEFAULT_PADDING = 15; 

    private float azimuthToNorth; // in degrees
    private float azimuthToCache; // in degrees 

    private Matrix windroseRotateMatrix;
    private Bitmap compassBitmap;
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
	azimuthToNorth = 0;
	azimuthToCache = 0;	

	compassBitmap = BitmapFactory.decodeResource(getContext().getResources(), R.drawable.compass256);
	windroseRotateMatrix = new Matrix();
	paint = new Paint();
	paint.setAntiAlias(true);
	
	Log.d(TAG, ""+this.getHeight());
    }

    @Override
    public void onDraw(Canvas canvas) {
	super.onDraw(canvas);

	// Convert angles relative mobile view direction
	float azimuthNorthRel = azimuthToNorth;
	// float azimuthGCRel = azimuthToCache + azimuthToNorth;

	// Calculate radiuses of circles and their center
	int canvasHeight = canvas.getHeight();
	int canvasWidth = canvas.getWidth();
	int bigRadius = Math.min(canvasHeight, canvasWidth) / 2 - DEFAULT_PADDING;
	// int smallRadius = bigRadius - 2 * DEFAULT_PADDING;
	int center = Math.min(canvasHeight, canvasWidth) / 2;

	float scaleWRPicX = (float) (2 * bigRadius) / compassBitmap.getWidth();
	float scaleWRPicY = (float) (2 * bigRadius) / compassBitmap.getHeight();

	windroseRotateMatrix.setScale(scaleWRPicX, scaleWRPicY);
	windroseRotateMatrix.setRotate(-azimuthNorthRel);

	Bitmap windrose = Bitmap.createBitmap(compassBitmap, 0, 0, compassBitmap.getWidth(), compassBitmap.getHeight(), windroseRotateMatrix, false);
	canvas.drawBitmap(windrose, center - windrose.getWidth() / 2, center - windrose.getHeight() / 2, paint);	
    }

    /**
     * @param angle
     *            - user azimuth in degrees
     */
    public void setAzimuthToNorth(float angle) {
	this.azimuthToNorth = angle;
	invalidate();
    }

    /**
     * @param azimuthToGeoCache
     *            - azimuth to geocache in degrees
     */
    public void setAzimuthToGeoCache(float azimuthToGeoCache) {
	this.azimuthToCache = azimuthToGeoCache;
	invalidate();
    }

    /**
     * @param dist
     *            - distance to geocache in meters
     */
    @Deprecated
    public void setDistanceToGeoCache(float dist) {	
    }
}
