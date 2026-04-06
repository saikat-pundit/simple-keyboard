package rkr.simplekeyboard.inputmethod.latin.settings;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.view.inputmethod.EditorInfo;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.latin.InputAttributes;
public class SettingsValues {
    public static final float DEFAULT_SIZE_SCALE = 1.0f;
    public final SpacingAndPunctuations mSpacingAndPunctuations;
    public final boolean mHasHardwareKeyboard;
    public final int mDisplayOrientation;
    public final boolean mAutoCap;
    public final boolean mVibrateOn;
    public final boolean mSoundOn;
    public final boolean mKeyPreviewPopupOn;
    public final boolean mUseOnScreen;
    public final boolean mShowsLanguageSwitchKey;
    public final boolean mImeSwitchEnabled;
    public final int mKeyLongpressTimeout;
    public final boolean mShowSpecialChars;
    public final boolean mShowNumberRow;
    public final boolean mSpaceSwipeEnabled;
    public final boolean mDeleteSwipeEnabled;
    public final InputAttributes mInputAttributes;
    public final float mKeypressSoundVolume;
    public final int mKeyPreviewPopupDismissDelay;
    public final float mKeyboardHeightScale;
    public final int mBottomOffsetPortrait;
    public SettingsValues(final SharedPreferences prefs, final Resources res,
            final InputAttributes inputAttributes) {
        mSpacingAndPunctuations = new SpacingAndPunctuations(res);
        mInputAttributes = inputAttributes;
        mAutoCap = prefs.getBoolean(Settings.PREF_AUTO_CAP, true);
        mVibrateOn = Settings.readVibrationEnabled(prefs, res);
        mSoundOn = Settings.readKeypressSoundEnabled(prefs, res);
        mKeyPreviewPopupOn = Settings.readKeyPreviewPopupEnabled(prefs, res);
        mUseOnScreen = Settings.readUseOnScreenKeyboard(prefs);
        mShowsLanguageSwitchKey = Settings.readShowLanguageSwitchKey(prefs);
        mImeSwitchEnabled = Settings.readEnableImeSwitch(prefs);
        mHasHardwareKeyboard = Settings.readHasHardwareKeyboard(res.getConfiguration());
        mKeyLongpressTimeout = Settings.readKeyLongpressTimeout(prefs, res);
        mKeypressSoundVolume = Settings.readKeypressSoundVolume(prefs);
        mKeyPreviewPopupDismissDelay = res.getInteger(R.integer.config_key_preview_linger_timeout);
        mKeyboardHeightScale = Settings.readKeyboardHeight(prefs, DEFAULT_SIZE_SCALE);
        mBottomOffsetPortrait = Settings.readBottomOffsetPortrait(prefs);
        mDisplayOrientation = res.getConfiguration().orientation;
        mShowSpecialChars = Settings.readShowSpecialChars(prefs);
        mShowNumberRow = Settings.readShowNumberRow(prefs);
        mSpaceSwipeEnabled = Settings.readSpaceSwipeEnabled(prefs);
        mDeleteSwipeEnabled = Settings.readDeleteSwipeEnabled(prefs);
    }
    public boolean isWordSeparator(final int code) {
        return mSpacingAndPunctuations.isWordSeparator(code);
    }
    public boolean isLanguageSwitchKeyDisabled() {
        return !mShowsLanguageSwitchKey;
    }
    public boolean isSameInputType(final EditorInfo editorInfo) {
        return mInputAttributes.isSameInputType(editorInfo);
    }
    public boolean hasSameOrientation(final Configuration configuration) {
        return mDisplayOrientation == configuration.orientation;
    }
}
