package rkr.simplekeyboard.inputmethod.keyboard;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat;
import rkr.simplekeyboard.inputmethod.latin.settings.Settings;
public final class KeyboardTheme {
    private static final String TAG = KeyboardTheme.class.getSimpleName();
    public static final String KEYBOARD_THEME_KEY = "pref_keyboard_theme_20140509";
    public static final int THEME_ID_LIGHT_BORDER = 1;
    public static final int THEME_ID_DARK_BORDER = 2;
    public static final int THEME_ID_LIGHT = 3;
    public static final int THEME_ID_DARK = 4;
    public static final int THEME_ID_SYSTEM = 5;
    public static final int THEME_ID_SYSTEM_BORDER = 6;
    public static final int DEFAULT_THEME_ID = THEME_ID_SYSTEM;
        static final KeyboardTheme[] KEYBOARD_THEMES = {
        new KeyboardTheme(THEME_ID_SYSTEM, "LXXSystem", R.style.KeyboardTheme_LXX_System, false),
        new KeyboardTheme(THEME_ID_SYSTEM_BORDER, "LXXSystemBorder", R.style.KeyboardTheme_LXX_System_Border, false),
        new KeyboardTheme(THEME_ID_LIGHT, "LXXLight", R.style.KeyboardTheme_LXX_Light, true),
        new KeyboardTheme(THEME_ID_DARK, "LXXDark", R.style.KeyboardTheme_LXX_Dark, true),
        new KeyboardTheme(THEME_ID_LIGHT_BORDER, "LXXLightBorder", R.style.KeyboardTheme_LXX_Light_Border, true),
        new KeyboardTheme(THEME_ID_DARK_BORDER, "LXXDarkBorder", R.style.KeyboardTheme_LXX_Dark_Border, true),
    };
    public final int mThemeId;
    public final int mStyleId;
    public final String mThemeName;
    public final boolean mCustomColorSupport;
    private KeyboardTheme(
            final int themeId,
            final String themeName,
            final int styleId,
            final boolean customColorSupport) {
        mThemeId = themeId;
        mThemeName = themeName;
        mStyleId = styleId;
        mCustomColorSupport = customColorSupport;
    }
    @Override
    public boolean equals(final Object o) {
        if (o == this) return true;
        return (o instanceof KeyboardTheme) && ((KeyboardTheme)o).mThemeId == mThemeId;
    }
    @Override
    public int hashCode() {
        return mThemeId;
    }
        static KeyboardTheme searchKeyboardThemeById(final int themeId) {
        for (final KeyboardTheme theme : KEYBOARD_THEMES) {
            if (theme.mThemeId == themeId) {
                return theme;
            }
        }
        return null;
    }
        static KeyboardTheme getDefaultKeyboardTheme() {
        return searchKeyboardThemeById(DEFAULT_THEME_ID);
    }
    public static String getKeyboardThemeName(final int themeId) {
        final KeyboardTheme theme = searchKeyboardThemeById(themeId);
        return theme.mThemeName;
    }
    public static void saveKeyboardThemeId(final int themeId, final SharedPreferences prefs) {
        prefs.edit().putString(KEYBOARD_THEME_KEY, Integer.toString(themeId)).apply();
    }
    public static KeyboardTheme getKeyboardTheme(final Context context) {
        final SharedPreferences prefs = PreferenceManagerCompat.getDeviceSharedPreferences(context);
        return getKeyboardTheme(prefs);
    }
    public static KeyboardTheme getKeyboardTheme(final SharedPreferences prefs) {
        final String themeIdString = prefs.getString(KEYBOARD_THEME_KEY, null);
        if (themeIdString == null) {
            return searchKeyboardThemeById(DEFAULT_THEME_ID);
        }
        try {
            final int themeId = Integer.parseInt(themeIdString);
            final KeyboardTheme theme = searchKeyboardThemeById(themeId);
            if (theme != null) {
                return theme;
            }
            Log.w(TAG, "Unknown keyboard theme in preference: " + themeIdString);
        } catch (final NumberFormatException e) {
            Log.w(TAG, "Illegal keyboard theme in preference: " + themeIdString, e);
        }
        prefs.edit().remove(KEYBOARD_THEME_KEY).remove(Settings.PREF_KEYBOARD_COLOR).apply();
        return getDefaultKeyboardTheme();
    }
}
