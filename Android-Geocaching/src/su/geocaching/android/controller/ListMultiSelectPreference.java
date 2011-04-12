package su.geocaching.android.controller;

/**
 * Created by IntelliJ IDEA.
 * User: Anatoliy
 * Date: 08.04.11
 * Time: 22:21
 * To change this template use File | Settings | File Templates.
 */

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.preference.ListPreference;
import android.preference.Preference;
import android.util.AttributeSet;
import su.geocaching.android.ui.R;

/**
 * A {@link Preference} that displays a list of entries as
 * a dialog and allows multiple selections
 * <p>
 * This preference will store a string into the SharedPreferences. This string will be the values selected
 * from the {@link #setEntryValues(CharSequence[])} array.
 * </p>
 */
public class ListMultiSelectPreference extends ListPreference {

    //Need to make sure the SEPARATOR is unique and weird enough that it doesn't match one of the entries.
    //Not using any fancy symbols because this is interpreted as a regex for splitting strings.
    private static final String SEPARATOR = "OV=I=XseparatorX=I=VO";
    private static final String DEFAULT_VALUE = "ALL";

    private boolean[] mClickedDialogEntryIndices;

    public ListMultiSelectPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        mClickedDialogEntryIndices = new boolean[getEntries().length];
    }


    @Override
    public void setEntries(CharSequence[] entries) {
        super.setEntries(entries);
        mClickedDialogEntryIndices = new boolean[entries.length];
    }

    @Override
    protected void onPrepareDialogBuilder(Builder builder) {
        CharSequence[] entries = getEntries();
        CharSequence[] entryValues = getEntryValues();

        if (entries == null || entryValues == null || entries.length != entryValues.length) {
            throw new IllegalStateException(
                    "ListPreference requires an entries array and an entryValues array which are both the same length");
        }

        restoreCheckedEntries();
        builder.setMultiChoiceItems(entries, mClickedDialogEntryIndices,
                new DialogInterface.OnMultiChoiceClickListener() {
                    public void onClick(DialogInterface dialog, int which, boolean val) {
                        mClickedDialogEntryIndices[which] = val;
                    }
                });
    }

    public static String[] parseStoredValue(CharSequence val) {
        if ("".equals(val))
            return null;
        else
            return ((String) val).split(SEPARATOR);
    }

    private void restoreCheckedEntries() {
        CharSequence[] entryValues = getEntryValues();

        String[] vals = parseStoredValue(getValue());
        if (vals != null) {
            if (vals[0].trim().equals(DEFAULT_VALUE)) {
                for (int i = 0; i < getEntries().length; i++) {
                    mClickedDialogEntryIndices[i] = true;
                }
            } else {
                for (int i = 0; i < entryValues.length; i++) {
                    for (int j = 0; j < vals.length; j++) {
                        if (entryValues[i].equals(vals[j].trim())) {
                            mClickedDialogEntryIndices[i] = true;
                            break;
                        } else {
                            mClickedDialogEntryIndices[i] = false;
                        }
                    }
                }
            }
        }
    }

    @Override
    protected void onDialogClosed(boolean positiveResult) {
        super.onDialogClosed(positiveResult);

        CharSequence[] entryValues = getEntryValues();
        if (positiveResult && entryValues != null) {
            StringBuffer value = new StringBuffer();
            for (int i = 0; i < entryValues.length; i++) {
                if (mClickedDialogEntryIndices[i]) {
                    value.append(entryValues[i]).append(SEPARATOR);
                }
            }

            if (callChangeListener(value)) {
                String val = value.toString();
                if (val.length() > 0)
                    val = val.substring(0, val.length() - SEPARATOR.length());
                setValue(val);
            }
        }
    }
}

