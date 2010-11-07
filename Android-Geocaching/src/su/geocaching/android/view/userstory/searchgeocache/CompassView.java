package su.geocaching.android.view.userstory.searchgeocache;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010
 *        View which displays compas for searching geocache.
 */
public class CompassView extends View {
	private static final int DEFAULT_PADDING = 7;

	private Context context;
	private float angle;	//hope in degrees
	
    public CompassView(Context context) {
        super(context);
        this.context =  context;
        angle=0;
    }

    public CompassView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context = context;
        angle=0;
    }

    public CompassView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.context = context;
        angle=0;
    }

    @Override
    public void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        //TODO: Draw compass, etc...
        angle=180;
        int canvasHeight = canvas.getHeight();
        int canvasWidth = canvas.getWidth();
        int bigRadius = Math.min(canvasHeight, canvasWidth)/2 - DEFAULT_PADDING;
        int smallRadius = bigRadius - DEFAULT_PADDING;
        int center=Math.min(canvasHeight, canvasWidth)/2;
        Paint paint = new Paint();
        paint.setColor(Color.BLUE);
        paint.setStyle(Style.STROKE);
        paint.setFakeBoldText(true);
        paint.setAntiAlias(true);
        canvas.drawCircle(center, center, bigRadius, paint);

        invalidate();
    }
    
    public void setAngle(float angle) {
    	this.angle=angle;
    }
}
