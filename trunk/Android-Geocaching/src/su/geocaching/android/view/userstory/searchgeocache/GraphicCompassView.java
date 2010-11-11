package su.geocaching.android.view.userstory.searchgeocache;

import java.text.DecimalFormat;

import android.R;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 * @description View which displays compass contains of bitmaps for searching
 *              geocache.
 */
public class GraphicCompassView extends View {
    private static final int DEFAULT_PADDING = 15;
    private static final int DEFAULT_TEXT_SIZE = 20;

    // if distance(m) > BIG_DISTANCE_VALUE
    // show "(x/1000) km" else "x m"
    private static final int BIG_DISTANCE_VALUE = 10000;

    private static final DecimalFormat BIG_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0.0");
    private static final DecimalFormat SMALL_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0");
    private static final String BIG_DISTANCE_VALUE_NAME = "km";
    private static final String SMALL_DISTANCE_VALUE_NAME = "m";

    private float azimuthToNorth; // in degrees
    private float azimuthToCache; // in degrees
    private float distanceToGeoCache; // in meters

    public GraphicCompassView(Context context) {
	super(context);
	azimuthToNorth = 0;
	azimuthToCache = 0;
	distanceToGeoCache = 0;
    }

    public GraphicCompassView(Context context, AttributeSet attrs) {
	super(context, attrs);
	azimuthToNorth = 0;
	azimuthToCache = 0;
	distanceToGeoCache = 0;
    }

    public GraphicCompassView(Context context, AttributeSet attrs, int defStyle) {
	super(context, attrs, defStyle);
	azimuthToNorth = 0;
	azimuthToCache = 0;
	distanceToGeoCache = 0;
    }

    @Override
    public void onDraw(Canvas canvas) {
	super.onDraw(canvas);

	// Convert angles relative mobile view direction
	float azimuthNorthRel = azimuthToNorth;
	float azimuthGCRel = azimuthToCache + azimuthToNorth;

	// Calculate radiuses of circles and their center
	int canvasHeight = canvas.getHeight();
	int canvasWidth = canvas.getWidth();
	int bigRadius = Math.min(canvasHeight, canvasWidth) / 2 - DEFAULT_PADDING;
	int smallRadius = bigRadius - 2 * DEFAULT_PADDING;
	int center = Math.min(canvasHeight, canvasWidth) / 2;

	// Decoding resources
	Bitmap arrow = BitmapFactory.decodeResource(getContext().getResources(), su.geocaching.android.view.R.drawable.arrow);
	Bitmap geoCacheCircle = BitmapFactory.decodeResource(getContext().getResources(), su.geocaching.android.view.R.drawable.gccircle);
	Bitmap rectangle = BitmapFactory.decodeResource(getContext().getResources(), su.geocaching.android.view.R.drawable.rectangle);
	Bitmap windrose = BitmapFactory.decodeResource(getContext().getResources(), su.geocaching.android.view.R.drawable.northcircle);

	// Scaling bitmaps
	Matrix gcRotateMatrix = new Matrix();
	Matrix windroseRotateMatrix = new Matrix();
	Matrix arrowRotateMatrix = new Matrix();
	Matrix rectRotateMatrix = new Matrix();
	float scaleGCPicX = (float) (2 * bigRadius) / geoCacheCircle.getWidth();
	float scaleGCPicY = (float) (2 * bigRadius) / geoCacheCircle.getHeight();
	float scaleWRPicX = (float) (2 * smallRadius) / windrose.getWidth();
	float scaleWRPicY = (float) (2 * smallRadius) / windrose.getHeight();
	float scaleArrowPicX = 1;
	float scaleArrowPicY = (float) smallRadius / arrow.getHeight();
	float scaleRectPicX = (float) smallRadius / rectangle.getWidth();
	float scaleRectPicY = (float) ((0.5 * smallRadius) / rectangle.getHeight());
	gcRotateMatrix.setScale(scaleGCPicX, scaleGCPicY);
	windroseRotateMatrix.setScale(scaleWRPicX, scaleWRPicY);
	arrowRotateMatrix.setScale(scaleArrowPicX, scaleArrowPicY);
	rectRotateMatrix.setScale(scaleRectPicX, scaleRectPicY);
	geoCacheCircle = Bitmap.createBitmap(geoCacheCircle, 0, 0, geoCacheCircle.getWidth(), geoCacheCircle.getHeight(), gcRotateMatrix, false);
	windrose = Bitmap.createBitmap(windrose, 0, 0, windrose.getWidth(), windrose.getHeight(), windroseRotateMatrix, false);
	arrow = Bitmap.createBitmap(arrow, 0, 0, arrow.getWidth(), arrow.getHeight(), arrowRotateMatrix, false);
	rectangle = Bitmap.createBitmap(rectangle, 0, 0, rectangle.getWidth(), rectangle.getHeight(), rectRotateMatrix, false);

	// Rotate bitmaps
	gcRotateMatrix = new Matrix();
	windroseRotateMatrix = new Matrix();
	gcRotateMatrix.postRotate(azimuthGCRel, bigRadius, bigRadius);
	windroseRotateMatrix.setRotate(azimuthNorthRel);
	geoCacheCircle = Bitmap.createBitmap(geoCacheCircle, 0, 0, geoCacheCircle.getWidth(), geoCacheCircle.getHeight(), gcRotateMatrix, false);
	windrose = Bitmap.createBitmap(windrose, 0, 0, windrose.getWidth(), windrose.getHeight(), windroseRotateMatrix, false);

	// Draw bitmaps
	canvas.drawBitmap(windrose, center - windrose.getWidth() / 2, center - windrose.getHeight() / 2, null);
	canvas.drawBitmap(geoCacheCircle, center - geoCacheCircle.getWidth() / 2, center - geoCacheCircle.getHeight() / 2, null);
	canvas.drawBitmap(arrow, center - arrow.getWidth() / 2, center - smallRadius, null);
	canvas.drawBitmap(rectangle, center - rectangle.getWidth() / 2, center + DEFAULT_PADDING, null);

	// Build string of distance
	String textDistance = "";
	if (distanceToGeoCache >= BIG_DISTANCE_VALUE) {
	    textDistance = BIG_DISTANCE_NUMBER_FORMAT.format(distanceToGeoCache / 1000) + " " + BIG_DISTANCE_VALUE_NAME;
	} else {
	    textDistance = SMALL_DISTANCE_NUMBER_FORMAT.format(distanceToGeoCache) + " " + SMALL_DISTANCE_VALUE_NAME;
	}

	// Draw text
	Rect textBounds = new Rect();
	Paint textPaint = new Paint();
	textPaint.setAntiAlias(true);
	textPaint.setColor(Color.RED);
	textPaint.setFakeBoldText(true);
	textPaint.setTextSize(DEFAULT_TEXT_SIZE);
	textPaint.getTextBounds(textDistance, 0, textDistance.length(), textBounds);
	canvas.drawText(textDistance, center - textBounds.centerX(), center + rectangle.getHeight() + DEFAULT_PADDING, textPaint);

	invalidate();
    }

    /**
     * @param angle
     *            - user azimuth in degrees
     */
    public void setAzimuthToNorth(float angle) {
	this.azimuthToNorth = angle;
    }

    /**
     * @param azimuthToGeoCache
     *            - azimuth to geocache in degrees
     */
    public void setAzimuthToGeoCache(float azimuthToGeoCache) {
	this.azimuthToCache = azimuthToGeoCache;
    }

    /**
     * @param dist
     *            - distance to geocache in meters
     */
    public void setDistanceToGeoCache(float dist) {
	this.distanceToGeoCache = dist;
    }
}
