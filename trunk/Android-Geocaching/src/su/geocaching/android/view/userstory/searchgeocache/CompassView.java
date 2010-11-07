package su.geocaching.android.view.userstory.searchgeocache;

import java.text.DecimalFormat;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * @description View which displays compas for searching geocache.
 */
public class CompassView extends View {
	private static final int DEFAULT_PADDING = 7;
	private static final int TEXT_PADDING = 2;
	private static final String NORTH_NAME = "N";
	private static final String CACHE_NAME = "GC";
	private static final double DEG2RAD = Math.PI / 180;
	private static final int BIG_DISTANCE_VALUE = 10000; // if distance(m)
	// greater than this
	// -
	// show (x/1000) km else x m
	private static final DecimalFormat BIG_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0.0");
	private static final DecimalFormat SMALL_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0");
	private static final String BIG_DISTANCE_VALUE_NAME = "km";
	private static final String SMALL_DISTANCE_VALUE_NAME = "m";

	private Context context;
	private float azimuthToNorth; // hope in degrees
	private float azimuthToCache; // hope in degrees
	private float distanceToGeoCache;
	private String test;

	public CompassView(Context context) {
		super(context);
		this.context = context;
		azimuthToNorth = 0;
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.context = context;
		azimuthToNorth = 0;
	}

	public CompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.context = context;
		azimuthToNorth = 0;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
//		azimuthToNorth = 15;
//		azimuthToCache = -15;
		float azimuthNorthRel=azimuthToNorth+90;
		float azimuthGCRel = -azimuthToCache + azimuthToNorth+90;
		int canvasHeight = canvas.getHeight();
		int canvasWidth = canvas.getWidth();
		int bigRadius = Math.min(canvasHeight, canvasWidth) / 2 - DEFAULT_PADDING;
		int smallRadius = bigRadius - 2 * DEFAULT_PADDING;
		int center = Math.min(canvasHeight, canvasWidth) / 2;
		Paint paint = new Paint();
		paint.setColor(Color.BLUE);
		paint.setStyle(Style.STROKE);
		paint.setFakeBoldText(true);
		paint.setAntiAlias(true);
		canvas.drawCircle(center, center, smallRadius, paint);

		int northX = (int) Math.round(smallRadius * Math.cos(DEG2RAD * azimuthNorthRel)) + center;
		int northY = (int) -Math.round(smallRadius * Math.sin(DEG2RAD * azimuthNorthRel)) + center;

		int cacheX = (int) Math.round(bigRadius * Math.cos(DEG2RAD * azimuthGCRel)) + center;
		int cacheY = (int) -Math.round(bigRadius * Math.sin(DEG2RAD * azimuthGCRel)) + center;

		Paint northTextPaint = new Paint();
		northTextPaint.setColor(Color.BLUE);
		northTextPaint.setFakeBoldText(true);
		northTextPaint.setAntiAlias(true);
		Rect northTextBounds = new Rect();
		northTextPaint.getTextBounds(NORTH_NAME, 0, NORTH_NAME.length(), northTextBounds);
		Paint northRectPaint = new Paint();
		northRectPaint.setColor(Color.BLACK);
		canvas.drawRect(northX - TEXT_PADDING, northTextBounds.top + northY - TEXT_PADDING, northX + northTextBounds.right + TEXT_PADDING, northY + TEXT_PADDING, northRectPaint);
		canvas.drawText(NORTH_NAME, northX, northY, northTextPaint);

		Paint cacheTextPaint = new Paint();
		cacheTextPaint.setColor(Color.RED);
		cacheTextPaint.setFakeBoldText(true);
		cacheTextPaint.setAntiAlias(true);
		cacheTextPaint.setStyle(Style.STROKE);
		canvas.drawCircle(center, center, bigRadius, cacheTextPaint);

		Rect cacheTextBounds = new Rect();
		cacheTextPaint.getTextBounds(CACHE_NAME, 0, CACHE_NAME.length(), cacheTextBounds);
		Paint cacheRectPaint = new Paint();
		cacheRectPaint.setColor(Color.BLACK);
		canvas.drawRect(cacheX - TEXT_PADDING, cacheTextBounds.top + cacheY - TEXT_PADDING, cacheX + cacheTextBounds.right + TEXT_PADDING, cacheY + TEXT_PADDING, cacheRectPaint);
		canvas.drawText(CACHE_NAME, cacheX, cacheY, cacheTextPaint);

		canvas.drawLine(center, center, center, center-smallRadius, northTextPaint);

		String textDistance = "";
		if (distanceToGeoCache >= BIG_DISTANCE_VALUE) {
			textDistance = BIG_DISTANCE_NUMBER_FORMAT.format(distanceToGeoCache / 1000) + " " + BIG_DISTANCE_VALUE_NAME;
		} else {
			textDistance = SMALL_DISTANCE_NUMBER_FORMAT.format(distanceToGeoCache) + " " + SMALL_DISTANCE_VALUE_NAME;
		}
		cacheTextPaint.getTextBounds(textDistance, 0, textDistance.length(), cacheTextBounds);
		canvas.drawText(textDistance, center-cacheTextBounds.centerX(),
				center+Math.abs(cacheTextBounds.height())+DEFAULT_PADDING, cacheTextPaint);

		canvas.drawText(test, center-cacheTextBounds.centerX(),
				center+Math.abs(cacheTextBounds.height())+DEFAULT_PADDING+40, cacheTextPaint);
		
		invalidate();
	}

	public void setAzimuthToNorth(float angle) {
		this.azimuthToNorth = angle;
	}

	public void setAzimuthToGeoCache(float azimuthToGeoCache) {
		this.azimuthToCache = azimuthToGeoCache;
	}

	public void setDistanceToGeoCache(float dist) {
		this.distanceToGeoCache = dist;
	}
	
    
    public void setTest(String s) {
    	this.test = s;
    }
}
