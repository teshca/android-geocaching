package su.geocaching.android.searchGeoCache;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.View;

/**
 * @author Android-Geocaching.su student project team
 * @since October 2010 
 * 	View which displays compas for searching geocache.
 */
public class CompasView extends View {

    public CompasView(Context context) {
	super(context);
    }
    
    public CompasView(Context context, AttributeSet attrs) {
	super(context,attrs);
    }
    
    public CompasView(Context context, AttributeSet attrs, int defStyle){
	super(context,attrs,defStyle);
    }

    @Override
    public void onDraw(Canvas canvas) {
	super.onDraw(canvas);
	//TODO: Draw compas, etc...
	invalidate();
    }
}
