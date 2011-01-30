package su.geocaching.android.controller.compass;

import su.geocaching.android.ui.R;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Abstract class implements a strategy of drawing compass
 * 
 * @author Nikita Bumakov
 */
public abstract class CompassDrawningHelper {

	protected static final int DEFAULT_NEEDLE_WIDTH = 8;

	protected int center;
	protected int bgColor; // background color, taken from the xml
	protected int needleWidth;


	public CompassDrawningHelper(Context context) {	
		bgColor = Color.parseColor(context.getResources().getString(R.color.menu_background));
		needleWidth = DEFAULT_NEEDLE_WIDTH;
	}

	/**
	 * Draw a compass with a given direction of the arrow and the cache
	 * 
	 * @param canvas
	 *            - drawing canvas
	 * @param northDirection
	 *            - direction to the North relative to 0 degrees
	 * @param cacheDirection
	 *            - direction to the cache relative to 0 degrees
	 */
	public abstract void draw(Canvas canvas, float northDirection, float cacheDirection);

	/**
	 * called when resize the view
	 * 
	 * @param width
	 *            - new width
	 * @param height
	 *            - new height
	 */
	public abstract void onSizeChanged(int width, int height);
}