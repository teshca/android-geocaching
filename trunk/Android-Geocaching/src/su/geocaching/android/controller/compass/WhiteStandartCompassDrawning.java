package su.geocaching.android.controller.compass;

import android.content.Context;
import android.graphics.BitmapFactory;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;

public class WhiteStandartCompassDrawning extends StandartCompassDrawning {

    public WhiteStandartCompassDrawning(Context context) {
        super(context);
        roseBitmap = BitmapFactory.decodeResource(Controller.getInstance().getResourceManager().getResources(), R.drawable.compass_rose_pale);
    }

}
