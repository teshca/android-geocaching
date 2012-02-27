package su.geocaching.android.ui.selectmap;

import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import su.geocaching.android.ui.UserLocationOverlayBase;

/**
 * @author: Yuri Denison
 * @since: 14.07.11
 */
public class StaticUserLocationOverlay extends UserLocationOverlayBase {
    private Drawable userLocationImage;

    StaticUserLocationOverlay(Drawable userLocationImage)
    {
        this.userLocationImage = userLocationImage;
    }

    @Override
    protected void drawUserLocation(Canvas canvas) {
        drawAt(canvas, userLocationImage, userPoint.x, userPoint.y, false);
    }

    @Override
    protected void onTapAction(){
        // TODO: implement on tap action.
    }
}