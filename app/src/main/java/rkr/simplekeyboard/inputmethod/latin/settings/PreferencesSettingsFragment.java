package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;

import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.keyboard.KeyboardLayoutSet;

/**
 * "Preferences" settings sub screen.
 *
 * This settings sub screen handles the following input preferences.
 * - Auto-capitalization
 * - Show separate number row
 * - Show special characters
 * - Show language switch key
 * - Show on-screen keyboard
 * - Switch to other keyboards
 * - Space swipe cursor move
 * - Delete swipe
 */
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
