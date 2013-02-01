package su.geocaching.android.ui.preferences;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import com.actionbarsherlock.app.SherlockPreferenceActivity;
import com.actionbarsherlock.view.MenuItem;
import su.geocaching.android.controller.Controller;
import su.geocaching.android.controller.ListMultiSelectPreference;
import su.geocaching.android.controller.managers.NavigationManager;
import su.geocaching.android.ui.R;

/**
 * @author: Yuri Denison
 * @since: 23.02.11
 */
public class MapPreferenceActivity extends SherlockPreferenceActivity {

    private static final String MAP_PREFERENCE_ACTIVITY_NAME = "/preferences/Map";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Controller.getInstance().getGoogleAnalyticsManager().trackActivityLaunch(MAP_PREFERENCE_ACTIVITY_NAME);
        getSupportActionBar().setHomeButtonEnabled(true);
        addPreferencesFromResource(R.xml.map_preference);

        final ListPreference mapType = (ListPreference) findPreference(getString(R.string.prefer_map_type_key));
        mapType.setOnPreferenceChangeListener(updateStatusOnListPreferenceChangeListener);
        mapType.setSummary(mapType.getEntry());

        final ListPreference iconsType = (ListPreference) findPreference(getString(R.string.prefer_icon_key));
        iconsType.setOnPreferenceChangeListener(updateStatusOnListPreferenceChangeListener);
        iconsType.setSummary(iconsType.getEntry());

        final ListMultiSelectPreference typeFilter = (ListMultiSelectPreference) findPreference(getString(R.string.cache_filter_type));
        typeFilter.setOnPreferenceChangeListener(updateStatusOnListMultiSelectPreferenceChangeListener);
        typeFilter.setSummary(getFilterSummary(typeFilter, typeFilter.getValue()));

        final ListMultiSelectPreference statusFilter = (ListMultiSelectPreference) findPreference(getString(R.string.cache_filter_status));
        statusFilter.setOnPreferenceChangeListener(updateStatusOnListMultiSelectPreferenceChangeListener);
        statusFilter.setSummary(getFilterSummary(statusFilter, statusFilter.getValue()));
    }

    private Preference.OnPreferenceChangeListener updateStatusOnListPreferenceChangeListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final ListPreference listPreference = (ListPreference) preference;
                    if (listPreference == null) return false;
                    preference.setSummary(listPreference.getEntries()[listPreference.findIndexOfValue((String) newValue)]);
                    return true;
                }
            };

    private Preference.OnPreferenceChangeListener updateStatusOnListMultiSelectPreferenceChangeListener =
            new Preference.OnPreferenceChangeListener() {
                @Override
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    final ListMultiSelectPreference listPreference = (ListMultiSelectPreference) preference;
                    if (listPreference == null) return false;
                    preference.setSummary(getFilterSummary(listPreference, newValue));
                    return true;
                }
            };

    private CharSequence getFilterSummary(ListMultiSelectPreference listPreference, Object newValue) {
        CharSequence[] selectedValues = listPreference.getSelectedEntryValues((String) newValue);
        if (selectedValues == null)
            return getString(R.string.filter_warning);
        if (selectedValues.length == listPreference.getEntries().length)
            return getString(R.string.filter_disabled);

        String summary = "";
        for (CharSequence val : selectedValues) {
            if (summary.length() != 0) summary += ", ";
            summary += listPreference.getEntries()[listPreference.findIndexOfValue((String) val)];
        }
        return summary;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavigationManager.startDashboardActivity(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
