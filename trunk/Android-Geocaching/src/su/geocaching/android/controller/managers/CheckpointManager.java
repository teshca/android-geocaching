package su.geocaching.android.controller.managers;

import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.utils.Sexagesimal;
import su.geocaching.android.model.GeoCache;
import su.geocaching.android.model.GeoCacheStatus;
import su.geocaching.android.model.GeoCacheType;
import su.geocaching.android.model.GeoPoint;
import su.geocaching.android.ui.R;

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

    public GeoCache addCheckpoint(int cacheId, String name, GeoPoint geoPoint) {
        deactivateCheckpoints();
        checkpointNumber++;
        GeoCache gc = new GeoCache();
        if (name == null || name.trim().equals("")) {
            gc.setName(String.format("%s %d", controller.getResourceManager().getString(R.string.checkpoint_dialog_title), checkpointNumber));
        } else {
            gc.setName(name.trim());
        }

        gc.setGeoPoint(geoPoint);
        gc.setType(GeoCacheType.CHECKPOINT);
        gc.setStatus(GeoCacheStatus.ACTIVE_CHECKPOINT);
        gc.setId(checkpointNumber);
        checkpoints.add(gc);

        dbm.addCheckpointGeoCache(gc, cacheId);
        controller.setCurrentSearchPoint(gc);
        return gc;
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
                controller.setCurrentSearchPoint(controller.getDbManager().getCacheByID(cacheId));
            }
            dbm.deleteCheckpointCache(cacheId, checkPoint.getId());
            checkpoints.remove(index);
        }
        return index;
    }

    /**
     * @param id
     *         the Id of active item
     */
    public void setActiveItem(int id) {
        int activeItemIndex = findIndexById(id);
        deactivateCheckpoints();
        GeoCache activeItem = checkpoints.get(activeItemIndex);
        activeItem.setStatus(GeoCacheStatus.ACTIVE_CHECKPOINT);
        controller.setCurrentSearchPoint(activeItem);
        dbm.updateCheckpointCacheStatus(cacheId, activeItem.getId(), GeoCacheStatus.ACTIVE_CHECKPOINT);
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
        controller.setCurrentSearchPoint(controller.getDbManager().getCacheByID(cacheId));
        dbm.deleteCheckpointCache(cacheId);
        checkpoints.clear();
    }

    //TODO: (?:<\D+?>)?'(?:</\D+?>)?          <--->        <strong>'</strong>
    private static final String degrees = "(?:<sup>(?:&#9702;|0|o|O)</sup>|\\s+|&#176;|&deg;|&nbsp;|\\D{2,6}|градусов)";
    private static final String minutes = "(?:&rsquo;|'|&#39;|мин)";
    private static final String delimiter = "[,\\.]";
    private static final String coordinatePattern = "(\\d+)\\s*" + degrees + "\\s*(\\d+)\\s*" + delimiter + "\\s*(\\d+)";
    private static final Pattern geoPattern = Pattern.compile("[N|S]?\\s*" + coordinatePattern + "\\D+" + coordinatePattern + "\\s*" + minutes + "?");

    public static String insertCheckpointsLink(String text) {
        if (text == null) throw new IllegalArgumentException("text is null");

        text = text.replace("</strong><strong>", ""); // prepare string. actually this is server side responsibility

        Matcher pageMatcher = geoPattern.matcher(text);
        StringBuffer sb = new StringBuffer();

        while (pageMatcher.find()) {
            int latitudeE6;
            int longitudeE6;
            try {
                int degrees = Integer.parseInt(pageMatcher.group(1));
                int minutes = Integer.parseInt(pageMatcher.group(2));
                double milliMinutes = Double.parseDouble("." + pageMatcher.group(3));
                latitudeE6 = new Sexagesimal(degrees, (double) minutes + milliMinutes).toCoordinateE6();

                degrees = Integer.parseInt(pageMatcher.group(4));
                minutes = Integer.parseInt(pageMatcher.group(5));
                milliMinutes = Float.parseFloat("." + pageMatcher.group(6));
                longitudeE6 = new Sexagesimal(degrees, (double) minutes + milliMinutes).toCoordinateE6();
            } catch (Exception e) {
                continue;
            }
            pageMatcher.appendReplacement(sb, String.format("<a href=\"geo:%d,%d\" style=\"color: rgb(86,144,93)\"><b>%s</b></a>", latitudeE6, longitudeE6, pageMatcher.group(0)));
        }

        pageMatcher.appendTail(sb);
        return sb.toString();
    }

    public void activateCheckpoint(GeoCache checkpoint) {
        if (checkpoint.getType() == GeoCacheType.CHECKPOINT) {
            setActiveItem(checkpoint.getId());
        } else {
            deactivateCheckpoints();
            Controller.getInstance().setCurrentSearchPoint(checkpoint);
        }
    }
}
