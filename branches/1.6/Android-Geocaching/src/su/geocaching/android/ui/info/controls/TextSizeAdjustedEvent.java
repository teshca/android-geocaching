package su.geocaching.android.ui.info.controls;

public class TextSizeAdjustedEvent {
    private float textSize;

    public TextSizeAdjustedEvent(float newTextSize) {
        textSize = newTextSize;
    }

    public float getTextSize() {
        return textSize;
    }
}
