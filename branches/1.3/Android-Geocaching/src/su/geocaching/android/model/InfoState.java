package su.geocaching.android.model;

/**
 * @author dmitry
 */
public class InfoState {

  private int cacheId;
  private int scroll;
  private int pageState;
  private int width;
  private float scale;
  private String errorMessage;

  public InfoState(int cacheId, int scroll, int pageState, int width, float scale, String errorMessage) {
    this.cacheId = cacheId;
    this.scroll = scroll;
    this.pageState = pageState;
    this.width = width;
    this.scale = scale;
    this.errorMessage = errorMessage;
  }

  public int getCacheId() {
    return cacheId;
  }

  public int getScroll() {
    return scroll;
  }

  public int getPageState() {
    return pageState;
  }

  public int getWidth() {
    return width;
  }

  public float getScale() {
    return scale;
  }

  public String getErrorMessage() {
     return errorMessage;
  }
}
