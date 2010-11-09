package su.geocaching.android.model.datastorage;

import su.geocaching.android.controller.filter.IFilter;

import java.util.LinkedList;

/**
 * Author: Yuri Denison Date: 04.11.2010 23:24:10
 */
public class SettingsStorage {
    private static SettingsStorage instance;

    public static SettingsStorage getInstance() {
	if (instance == null) {
	    synchronized (SettingsStorage.class) {
		if (instance == null) {
		    instance = new SettingsStorage();
		}
	    }
	}
	return instance;
    }

    // TODO create settings storage using android resources

    public LinkedList<IFilter> getFilters() {
	return null;
    }
}
