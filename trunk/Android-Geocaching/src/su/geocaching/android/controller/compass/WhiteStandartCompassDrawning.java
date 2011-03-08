package su.geocaching.android.controller.compass;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.ui.R;
import android.content.Context;
import android.graphics.BitmapFactory;

public class WhiteStandartCompassDrawning extends StandartCompassDrawning {

	public WhiteStandartCompassDrawning(Context context) {
		super(context);
		roseBitmap = BitmapFactory.decodeResource(Controller.getInstance().getResourceManager().getResources(), R.drawable.compass_rose_pale);
	}

}
