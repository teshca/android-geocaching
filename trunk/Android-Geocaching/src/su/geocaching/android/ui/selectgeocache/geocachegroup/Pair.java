package su.geocaching.android.ui.selectgeocache.geocachegroup;

/**
 * @author Yuri Denison; yuri.denison@gmail.com
 * @since 17.02.11
 */

public class Pair {
    public int x;
    public int y;

    public Pair(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public boolean equals(Pair pair) {
        return x == pair.x && y == pair.y;
    }
}
