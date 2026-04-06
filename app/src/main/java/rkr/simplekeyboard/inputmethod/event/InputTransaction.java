package rkr.simplekeyboard.inputmethod.event;
import rkr.simplekeyboard.inputmethod.latin.settings.SettingsValues;
public class InputTransaction {
    public static final int SHIFT_NO_UPDATE = 0;
    public static final int SHIFT_UPDATE_NOW = 1;
    public static final int SHIFT_UPDATE_LATER = 2;
    public final SettingsValues mSettingsValues;
    private int mRequiredShiftUpdate = SHIFT_NO_UPDATE;
    public InputTransaction(final SettingsValues settingsValues) {
        mSettingsValues = settingsValues;
    }
        public void requireShiftUpdate(final int updateType) {
        mRequiredShiftUpdate = Math.max(mRequiredShiftUpdate, updateType);
    }
        public int getRequiredShiftUpdate() {
        return mRequiredShiftUpdate;
    }
}
