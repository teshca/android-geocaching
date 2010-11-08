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
	private static final int HALF_PI_GRAD = 90;
	
	// if distance(m) > BIG_DISTANCE_VALUE
	// 	show "(x/1000) km" else "x m"
	private static final int BIG_DISTANCE_VALUE = 10000; 
	
	private static final DecimalFormat BIG_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0.0");
	private static final DecimalFormat SMALL_DISTANCE_NUMBER_FORMAT = new DecimalFormat("0");
	private static final String BIG_DISTANCE_VALUE_NAME = "km";
	private static final String SMALL_DISTANCE_VALUE_NAME = "m";

	private float azimuthToNorth; // in degrees
	private float azimuthToCache; // in degrees
	private float distanceToGeoCache; // in meters

	public CompassView(Context context) {
		super(context);
		azimuthToNorth = 0;
		azimuthToCache = 0;
		distanceToGeoCache=0;
	}

	public CompassView(Context context, AttributeSet attrs) {
		super(context, attrs);
		azimuthToNorth = 0;
		azimuthToCache = 0;
		distanceToGeoCache=0;
	}

	public CompassView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		azimuthToNorth = 0;
		azimuthToCache = 0;
		distanceToGeoCache=0;
	}

	@Override
	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		
		//Convert angles relative mobile view direction
		float azimuthNorthRel = azimuthToNorth + HALF_PI_GRAD;
		float azimuthGCRel = -azimuthToCache + azimuthToNorth + HALF_PI_GRAD;
		
		//Calculate radiuses of circles and their center
		int canvasHeight = canvas.getHeight();
		int canvasWidth = canvas.getWidth();
		int bigRadius = Math.min(canvasHeight, canvasWidth) / 2 - DEFAULT_PADDING;
		int smallRadius = bigRadius - 2 * DEFAULT_PADDING;
		int center = Math.min(canvasHeight, canvasWidth) / 2;
				
		//Paints initialization: north circle
		Paint northCirclePaint = new Paint();
		northCirclePaint.setColor(Color.BLUE);
		northCirclePaint.setStyle(Style.STROKE);
		northCirclePaint.setFakeBoldText(true);
		northCirclePaint.setAntiAlias(true);
		//-----------------------north text
		Paint northTextPaint = new Paint();
		northTextPaint.setColor(Color.BLUE);
		northTextPaint.setFakeBoldText(true);
		northTextPaint.setAntiAlias(true);
		//-----------------------text rectangle background
		Paint textBGPaint = new Paint();
		textBGPaint.setColor(Color.BLACK);
		//-----------------------cache text
		Paint cacheTextPaint = new Paint();
		cacheTextPaint.setColor(Color.RED);
		cacheTextPaint.setFakeBoldText(true);
		cacheTextPaint.setAntiAlias(true);
		cacheTextPaint.setStyle(Style.STROKE);
		
		//Calculate position of text
		int northX = (int) Math.round(smallRadius * Math.cos(DEG2RAD * azimuthNorthRel)) + center;
		int northY = (int) -Math.round(smallRadius * Math.sin(DEG2RAD * azimuthNorthRel)) + center;
		int cacheX = (int) Math.round(bigRadius * Math.cos(DEG2RAD * azimuthGCRel)) + center;
		int cacheY = (int) -Math.round(bigRadius * Math.sin(DEG2RAD * azimuthGCRel)) + center;
		
		//Build string of distance
		String textDistance = "";
		if (distanceToGeoCache >= BIG_DISTANCE_VALUE) {
			textDistance = BIG_DISTANCE_NUMBER_FORMAT.format(distanceToGeoCache / 1000) + " " + BIG_DISTANCE_VALUE_NAME;
		} else {
			textDistance = SMALL_DISTANCE_NUMBER_FORMAT.format(distanceToGeoCache) + " " + SMALL_DISTANCE_VALUE_NAME;
		}
				
		//Calculate rectangle which contain text
		Rect northTextBounds = new Rect();
		northTextPaint.getTextBounds(NORTH_NAME, 0, NORTH_NAME.length(), northTextBounds);
		Rect cacheTextBounds = new Rect();
		cacheTextPaint.getTextBounds(CACHE_NAME, 0, CACHE_NAME.length(), cacheTextBounds);
		cacheTextPaint.getTextBounds(textDistance, 0, textDistance.length(), cacheTextBounds);

		
		//Drawing
		canvas.drawCircle(center, center, smallRadius, northCirclePaint);
		canvas.drawRect(northX - TEXT_PADDING, northTextBounds.top + northY - TEXT_PADDING, northX + northTextBounds.right + TEXT_PADDING, northY + TEXT_PADDING, textBGPaint);
		canvas.drawText(NORTH_NAME, northX, northY, northTextPaint);
		canvas.drawCircle(center, center, bigRadius, cacheTextPaint);
		canvas.drawRect(cacheX - TEXT_PADDING, cacheTextBounds.top + cacheY - TEXT_PADDING, cacheX + cacheTextBounds.right + TEXT_PADDING, cacheY + TEXT_PADDING, textBGPaint);
		canvas.drawText(CACHE_NAME, cacheX, cacheY, cacheTextPaint);
		canvas.drawLine(center, center, center, center - smallRadius, northTextPaint);
		canvas.drawText(textDistance, center - cacheTextBounds.centerX(), center + Math.abs(cacheTextBounds.height()) + DEFAULT_PADDING, cacheTextPaint);

		invalidate();
	}

	
	/**
	 * @param angle - user azimuth in degrees
	 */
	public void setAzimuthToNorth(float angle) {
		this.azimuthToNorth = angle;
	}

	/**
	 * @param azimuthToGeoCache - azimuth to geocache in degrees
	 */
	public void setAzimuthToGeoCache(float azimuthToGeoCache) {
		this.azimuthToCache = azimuthToGeoCache;
	}

	
	/**
	 * @param dist - distance to geocache in meters
	 */
	public void setDistanceToGeoCache(float dist) {
		this.distanceToGeoCache = dist;
	}
}
