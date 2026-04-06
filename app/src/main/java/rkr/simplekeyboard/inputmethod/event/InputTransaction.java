package rkr.simplekeyboard.inputmethod.event;

import rkr.simplekeyboard.inputmethod.latin.settings.SettingsValues;

/**
 * An object encapsulating a single transaction for input.
 */
public class InputTransaction {
    // UPDATE_LATER is stronger than UPDATE_NOW. The reason for this is, if we have to update later,
    // it's because something will change that we can't evaluate now, which means that even if we
    // re-evaluate now we'll have to do it again later. The only case where that wouldn't apply
    // would be if we needed to update now to find out the new state right away, but then we
    // can't do it with this deferred mechanism anyway.
    public static final int SHIFT_NO_UPDATE = 0;
    public static final int SHIFT_UPDATE_NOW = 1;
    public static final int SHIFT_UPDATE_LATER = 2;

    // Initial conditions
    public final SettingsValues mSettingsValues;

    // Outputs
    private int mRequiredShiftUpdate = SHIFT_NO_UPDATE;

    public InputTransaction(final SettingsValues settingsValues) {
        mSettingsValues = settingsValues;
    }

    /**
     * Indicate that this transaction requires some type of shift update.
     * @param updateType What type of shift update this requires.
     */
    public void requireShiftUpdate(final int updateType) {
        mRequiredShiftUpdate = Math.max(mRequiredShiftUpdate, updateType);
    }

    /**
     * Gets what type of shift update this transaction requires.
     * @return The shift update type.
     */
    public int getRequiredShiftUpdate() {
        return mRequiredShiftUpdate;
    }
}
