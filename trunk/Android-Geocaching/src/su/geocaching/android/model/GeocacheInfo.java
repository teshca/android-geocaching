package su.geocaching.android.model;

/**
 * @author dmitry
 */
public class GeocacheInfo {

  private int cacheId;
  private int scroll;
  private int pageState;
  private int width;
  private float scale;

  public GeocacheInfo(int cacheId, int scroll, int pageState, int width, float scale) {
    this.cacheId = cacheId;
    this.scroll = scroll;
    this.pageState = pageState;
    this.width = width;
    this.scale = scale;
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
}
