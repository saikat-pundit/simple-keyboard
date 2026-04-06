package rkr.simplekeyboard.inputmethod.latin.utils;
import java.util.Locale;
import java.util.Set;
import rkr.simplekeyboard.inputmethod.latin.Subtype;
import rkr.simplekeyboard.inputmethod.latin.RichInputMethodManager;
public final class LanguageOnSpacebarUtils {
    public static final int FORMAT_TYPE_NONE = 0;
    public static final int FORMAT_TYPE_LANGUAGE_ONLY = 1;
    public static final int FORMAT_TYPE_FULL_LOCALE = 2;
    private LanguageOnSpacebarUtils() {
    }
    public static int getLanguageOnSpacebarFormatType(final Subtype subtype) {
        final Locale locale = subtype.getLocaleObject();
        if (locale == null) {
            return FORMAT_TYPE_NONE;
        }
        final String keyboardLanguage = locale.getLanguage();
        final String keyboardLayout = subtype.getKeyboardLayoutSet();
        int sameLanguageAndLayoutCount = 0;
        final Set<Subtype> enabledSubtypes =
                RichInputMethodManager.getInstance().getEnabledSubtypes(false);
        for (final Subtype enabledSubtype : enabledSubtypes) {
            final String language = enabledSubtype.getLocaleObject().getLanguage();
            if (keyboardLanguage.equals(language)
                    && keyboardLayout.equals(enabledSubtype.getKeyboardLayoutSet())) {
                sameLanguageAndLayoutCount++;
            }
        }
        return sameLanguageAndLayoutCount > 1 ? FORMAT_TYPE_FULL_LOCALE
                : FORMAT_TYPE_LANGUAGE_ONLY;
    }
}
