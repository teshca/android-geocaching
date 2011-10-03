package su.geocaching.android.controller.managers;

import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.utils.CoordinateHelper;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;

import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckpointManager {

    private List<GeoCache> checkpoints;
    private GeoPoint lastInputGP;
    private int checkpointNumber = 0;
    private int cacheId;

    private DbManager dbm;
    private Controller controller;

    private CheckpointManager() {
        controller = Controller.getInstance();
        dbm = controller.getDbManager();
        checkpoints = new LinkedList<GeoCache>();
    }

    public CheckpointManager(int id) {
        this();
        cacheId = id;
        checkpoints = dbm.getCheckpointsArrayById(id);
        for (GeoCache checkpoint : checkpoints) {
            if (checkpoint.getId() > checkpointNumber) {
                checkpointNumber = checkpoint.getId();
            }
        }
    }

    /**
     * @return the cacheId
     */
    public int getCacheId() {
        return cacheId;
    }

    /**
     * @param latitudeE6
     * @param longitudeE6
     * @return
     */
    public GeoCacheOverlayItem addCheckpoint(int cacheId, String name, int latitudeE6, int longitudeE6) {
        deactivateCheckpoints();
        checkpointNumber++;
        GeoCache gc = new GeoCache();
        if (name == null || name.trim().equals("")) {
            gc.setName(String.format("%s %d", controller.getResourceManager().getString(R.string.checkpoint_dialog_title), checkpointNumber));
        } else {
            gc.setName(name.trim());
        }
        deactivateCheckpoints();
        
        gc.setLocationGeoPoint(new GeoPoint(latitudeE6, longitudeE6));
        gc.setType(GeoCacheType.CHECKPOINT);
        gc.setStatus(GeoCacheStatus.ACTIVE_CHECKPOINT);
        gc.setId(checkpointNumber);
        checkpoints.add(gc);

        dbm.addCheckpointGeoCache(gc, cacheId);
        controller.setSearchingGeoCache(gc);
        return new GeoCacheOverlayItem(gc, "", "");
    }

    public void deactivateCheckpoints() {
        for (GeoCache checkpoint : checkpoints) {
            if (checkpoint.getStatus() == GeoCacheStatus.ACTIVE_CHECKPOINT) {
                checkpoint.setStatus(GeoCacheStatus.NOT_ACTIVE_CHECKPOINT);
                dbm.updateCheckpointCacheStatus(cacheId, checkpoint.getId(), GeoCacheStatus.NOT_ACTIVE_CHECKPOINT);
            }
        }
    }

    private void removeCheckpointByIndex(int index) {
        GeoCache gc = checkpoints.get(index);
        if (gc.getStatus() == GeoCacheStatus.ACTIVE_CHECKPOINT) {
            controller.setSearchingGeoCache(controller.getDbManager().getCacheByID(cacheId));
        }
        dbm.deleteCheckpointCache(cacheId, gc.getId());
        checkpoints.remove(index);
    }

    public int removeCheckpoint(int id) {
        int index = findItemById(id);
        if (id != -1) {
            removeCheckpointByIndex(index);
        }
        return index;
    }

    public GeoCache getGeoCache(int id) {
        return checkpoints.get(findItemById(id));
    }

    /**
     * @return the activeItem
     */
    public int getActiveItem() {
        int i = 0;
        for (GeoCache checkpoint : checkpoints) {
            if (checkpoint.getStatus() == GeoCacheStatus.ACTIVE_CHECKPOINT) {
                break;
            }
            i++;
        }
        return i >= checkpoints.size() ? -1 : i;
    }

    /**
     * @param id the Id of active item
     */
    public void setActiveItem(int id) {
        int activeItem = findItemById(id);
        deactivateCheckpoints();
        checkpoints.get(activeItem).setStatus(GeoCacheStatus.ACTIVE_CHECKPOINT);
        controller.setSearchingGeoCache(checkpoints.get(activeItem));
        dbm.updateCheckpointCacheStatus(cacheId, checkpoints.get(activeItem).getId(), GeoCacheStatus.ACTIVE_CHECKPOINT);
    }

    private int findItemById(int id) {
        for (GeoCache item : checkpoints) {
            if (item.getId() == id) {
                return checkpoints.indexOf(item);
            }
        }
        return -1;
    }

    /**
     * @return the checkpoints
     */
    public List<GeoCache> getCheckpoints() {
        return checkpoints;
    }

    public void clear() {
        dbm.deleteCheckpointCache(cacheId);
        checkpoints.clear();
    }

    public void setLastInputGeoPoint(GeoPoint geoPoint) {
        lastInputGP = geoPoint;
    }

    public GeoPoint getLastInputGeoPoint() {
        return lastInputGP;
    }


    public static String insertCheckpointsLink(String text) {
        Pattern geoPattern = Pattern.compile("[N|S]\\s*(\\d+)\\s*(?:<sup>&#9702;</sup>|&rsquo;|\\D+)\\s*(\\d+)\\s*.\\s*(\\d+).{1,20}[E|W]\\s*(\\d+)\\s*(?:<sup>&#9702;</sup>|&rsquo;|\\D+)\\s*(\\d+)\\s*.\\s*(\\d+)(?:&rsquo;)?");
        Matcher pageMatcher = geoPattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (pageMatcher.find()) {
            int latitude;
            int longitude;
            try {
                int degrees = Integer.parseInt(pageMatcher.group(1));
                int minutes = Integer.parseInt(pageMatcher.group(2));
                double milliMinutes = Double.parseDouble("." + pageMatcher.group(3));
                latitude = CoordinateHelper.sexagesimalToCoordinateE6(degrees, minutes + milliMinutes);

                degrees = Integer.parseInt(pageMatcher.group(4));
                minutes = Integer.parseInt(pageMatcher.group(5));
                milliMinutes = Float.parseFloat("." + pageMatcher.group(6));

                longitude = CoordinateHelper.sexagesimalToCoordinateE6(degrees, (double)minutes + milliMinutes);
            } catch (Exception e) {
                continue;
            }

            pageMatcher.appendReplacement(sb, String.format("<a href=\"geo:%d,%d?q=\"><b>%s</b></a>", latitude, longitude, pageMatcher.group(0)));
        }

        pageMatcher.appendTail(sb);
        return sb.toString();
    }
}
