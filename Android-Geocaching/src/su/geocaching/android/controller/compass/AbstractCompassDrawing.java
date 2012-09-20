package su.geocaching.android.controller.compass;

import android.graphics.Canvas;
import android.graphics.Color;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

/**
 * Abstract class implements a strategy of drawing compass
 * 
 * @author Nikita Bumakov
 */
public abstract class AbstractCompassDrawing {

    public static final String TYPE_CLASSIC = "CLASSIC"; //TODO: this taken from pref xml. Something must be done with this
    public static final String TYPE_PALE = "PALE"; //TODO: this taken from pref xml. Something must be done with this
    public static final String TYPE_PREVIEW = "PREVIEW"; //TODO: this taken from pref xml. Something must be done with this

    public static final String AZIMUTH_FORMAT = "%.1f°";

    protected int centerX, centerY;
    protected int needleWidth = 8;
    protected float distance;
    protected float declination;

    /**
     * @param distance
     *            the distance to set
     */
    public void setDistance(float distance) {
        this.distance = distance;
    }
    

    public void setDeclination(float declination) {
        this.declination = declination;        
    }

    /**
     * Draw a compass with a given direction of the compass's needle
     * 
     * @param canvas
     *            - drawing canvas
     * @param northDirection
     *            - direction to the North relative to 0 degrees
     */
    public abstract void draw(Canvas canvas, float northDirection);

    /**
     * called when resize the view
     * 
     * @param width
     *            - new width
     * @param height
     *            - new height
     */
    public abstract void onSizeChanged(int width, int height);

    /**
     * Draw cache arrow
     * 
     * @param canvas
     *            - drawing canvas
     * @param cacheDirection
     *            - direction to the cache relative to 0 degrees
     */
    public abstract void drawCacheArrow(Canvas canvas, float cacheDirection);

    public abstract void drawSourceType(Canvas canvas, CompassSourceType sourceType);

    public abstract String getType();
}