package su.geocaching.android.ui;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.view.View;
import android.widget.Checkable;
import android.widget.FrameLayout;
import su.geocaching.android.controller.Controller;

public class FavoritesFolderRow extends FrameLayout implements Checkable {
    private static final ColorDrawable checkedBackgroundDrawable = new ColorDrawable();

    static {
        checkedBackgroundDrawable.setColor(Controller.getInstance().getResourceManager().getColor(R.color.dashboard_text_color));
        checkedBackgroundDrawable.setAlpha(0x3F);
    }

    private boolean mChecked;

    public FavoritesFolderRow(Context context) {
        super(context);
        View.inflate(context, R.layout.favorites_folder_row, this);
    }

    public void setChecked(boolean checked) {
        mChecked = checked;
        setBackgroundDrawable(checked ? checkedBackgroundDrawable : null);
    }

    public boolean isChecked() {
        return mChecked;
    }

    public void toggle() {
        setChecked(!mChecked);
    }
}