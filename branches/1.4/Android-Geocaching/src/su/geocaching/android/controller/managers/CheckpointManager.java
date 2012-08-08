package su.geocaching.android.controller.managers;

import com.google.android.maps.GeoPoint;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.utils.Sexagesimal;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.ui.R;
import su.geocaching.android.ui.geocachemap.GeoCacheOverlayItem;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CheckpointManager {

    private List<GeoCache> checkpoints;
    private int checkpointNumber = 0;
    private int cacheId;

    private DbManager dbm;
    private Controller controller;

    public CheckpointManager(int id) {
        controller = Controller.getInstance();
        dbm = controller.getDbManager();
        
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

    public GeoCacheOverlayItem addCheckpoint(int cacheId, String name, int latitudeE6, int longitudeE6) {
        deactivateCheckpoints();
        checkpointNumber++;
        GeoCache gc = new GeoCache();
        if (name == null || name.trim().equals("")) {
            gc.setName(String.format("%s %d", controller.getResourceManager().getString(R.string.checkpoint_dialog_title), checkpointNumber));
        } else {
            gc.setName(name.trim());
        }
        
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

    public int removeCheckpoint(int id) {
        int index = findIndexById(id);
        if (index != -1) {
            GeoCache checkPoint = checkpoints.get(index);
            if (checkPoint.getStatus() == GeoCacheStatus.ACTIVE_CHECKPOINT) {
                controller.setSearchingGeoCache(controller.getDbManager().getCacheByID(cacheId));
            }
            dbm.deleteCheckpointCache(cacheId, checkPoint.getId());
            checkpoints.remove(index);
        }
        return index;
    }

    public GeoCache getGeoCache(int id) {
        return checkpoints.get(findIndexById(id));
    }

    /**
     * @param id the Id of active item
     */
    public void setActiveItem(int id) {
        int activeItem = findIndexById(id);
        deactivateCheckpoints();
        checkpoints.get(activeItem).setStatus(GeoCacheStatus.ACTIVE_CHECKPOINT);
        controller.setSearchingGeoCache(checkpoints.get(activeItem));
        dbm.updateCheckpointCacheStatus(cacheId, checkpoints.get(activeItem).getId(), GeoCacheStatus.ACTIVE_CHECKPOINT);
    }

    private int findIndexById(int id) {
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
        controller.setSearchingGeoCache(controller.getDbManager().getCacheByID(cacheId));
        dbm.deleteCheckpointCache(cacheId);
        checkpoints.clear();
    }

    //TODO: (?:<\D+?>)?'(?:</\D+?>)?          <--->        <strong>'</strong>
    private static final String degPattern = "(?:<sup>&#9702;</sup>|&#176;|\\D+)";
    private static final String coordinatePattern = "(\\d+)\\s*" + degPattern + "\\s*(\\d+)\\s*.\\s*(\\d+)";
    private static final Pattern geoPattern = Pattern.compile("[N|S]\\s*" + coordinatePattern + "\\D{1,}[E|W]\\s*" + coordinatePattern + "(?:&rsquo;|'|&#39;)?");
    public static String insertCheckpointsLink(String text) {
        if (text == null) throw new IllegalArgumentException("text is null");

        Matcher pageMatcher = geoPattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (pageMatcher.find()) {
            int latitude;
            int longitude;
            try {
                int degrees = Integer.parseInt(pageMatcher.group(1));
                int minutes = Integer.parseInt(pageMatcher.group(2));
                double milliMinutes = Double.parseDouble("." + pageMatcher.group(3));
                latitude = new Sexagesimal(degrees, (double)minutes + milliMinutes).toCoordinateE6();

                degrees = Integer.parseInt(pageMatcher.group(4));
                minutes = Integer.parseInt(pageMatcher.group(5));
                milliMinutes = Float.parseFloat("." + pageMatcher.group(6));
                longitude = new Sexagesimal(degrees, (double)minutes + milliMinutes).toCoordinateE6();
            } catch (Exception e) {
                continue;
            }

            pageMatcher.appendReplacement(sb, String.format("<a href=\"geo:%d,%d?q=\"><b>%s</b></a>", latitude, longitude, pageMatcher.group(0)));
        }

        pageMatcher.appendTail(sb);
        return sb.toString();
    }
}
