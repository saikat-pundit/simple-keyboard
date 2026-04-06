package rkr.simplekeyboard.inputmethod.latin.settings;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.keyboard.KeyboardLayoutSet;
public final class PreferencesSettingsFragment extends SubScreenFragment {
    @Override
    public void onCreate(final Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(R.xml.prefs_screen_preferences);
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.BAKLAVA) {
            removePreference(Settings.PREF_USE_ON_SCREEN);
        }
    }
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
        if (key.equals(Settings.PREF_SHOW_SPECIAL_CHARS) ||
                key.equals(Settings.PREF_SHOW_NUMBER_ROW)) {
            KeyboardLayoutSet.onKeyboardThemeChanged();
        }
    }
}
