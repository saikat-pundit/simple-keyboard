package rkr.simplekeyboard.inputmethod.latin.utils;
import java.util.HashSet;
import rkr.simplekeyboard.inputmethod.latin.settings.AppearanceSettingsFragment;
import rkr.simplekeyboard.inputmethod.latin.settings.KeyPressSettingsFragment;
import rkr.simplekeyboard.inputmethod.latin.settings.LanguagesSettingsFragment;
import rkr.simplekeyboard.inputmethod.latin.settings.PreferencesSettingsFragment;
import rkr.simplekeyboard.inputmethod.latin.settings.SettingsFragment;
import rkr.simplekeyboard.inputmethod.latin.settings.SingleLanguageSettingsFragment;
import rkr.simplekeyboard.inputmethod.latin.settings.ThemeSettingsFragment;
public class FragmentUtils {
    private static final HashSet<String> sLatinImeFragments = new HashSet<>();
    static {
        sLatinImeFragments.add(PreferencesSettingsFragment.class.getName());
        sLatinImeFragments.add(KeyPressSettingsFragment.class.getName());
        sLatinImeFragments.add(AppearanceSettingsFragment.class.getName());
        sLatinImeFragments.add(ThemeSettingsFragment.class.getName());
        sLatinImeFragments.add(SettingsFragment.class.getName());
        sLatinImeFragments.add(LanguagesSettingsFragment.class.getName());
        sLatinImeFragments.add(SingleLanguageSettingsFragment.class.getName());
    }
    public static boolean isValidFragment(String fragmentName) {
        return sLatinImeFragments.contains(fragmentName);
    }
}
