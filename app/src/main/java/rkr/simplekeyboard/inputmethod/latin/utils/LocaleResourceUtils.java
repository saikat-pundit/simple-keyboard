package rkr.simplekeyboard.inputmethod.latin.utils;
import android.content.Context;
import android.content.res.Resources;
import java.util.HashMap;
import java.util.Locale;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.latin.common.LocaleUtils;
import rkr.simplekeyboard.inputmethod.latin.common.StringUtils;
public final class LocaleResourceUtils {
    private static final String RESOURCE_PACKAGE_NAME = R.class.getPackage().getName();
    private static volatile boolean sInitialized = false;
    private static final Object sInitializeLock = new Object();
    private static Resources sResources;
    private static final HashMap<String, Integer> sExceptionalLocaleDisplayedInRootLocale = new HashMap<>();
    private static final HashMap<String, Integer> sExceptionalLocaleToNameIdsMap = new HashMap<>();
    private static final String LOCALE_NAME_RESOURCE_PREFIX =
            "string/locale_name_";
    private static final String LOCALE_NAME_RESOURCE_IN_ROOT_LOCALE_PREFIX =
            "string/locale_name_in_root_locale_";
    private LocaleResourceUtils() {
    }
    public static void init(final Context context) {
        synchronized (sInitializeLock) {
            if (sInitialized == false) {
                initLocked(context);
                sInitialized = true;
            }
        }
    }
    public static void onLocalChange(final Context context) {
        sResources = context.getResources();
    }
    private static void initLocked(final Context context) {
        final Resources res = context.getResources();
        sResources = res;
        final String[] exceptionalLocaleInRootLocale = res.getStringArray(
                R.array.locale_displayed_in_root_locale);
        for (int i = 0; i < exceptionalLocaleInRootLocale.length; i++) {
            final String localeString = exceptionalLocaleInRootLocale[i];
            final String resourceName = LOCALE_NAME_RESOURCE_IN_ROOT_LOCALE_PREFIX + localeString;
            final int resId = res.getIdentifier(resourceName, null, RESOURCE_PACKAGE_NAME);
            sExceptionalLocaleDisplayedInRootLocale.put(localeString, resId);
        }
        final String[] exceptionalLocales = res.getStringArray(R.array.locale_exception_keys);
        for (int i = 0; i < exceptionalLocales.length; i++) {
            final String localeString = exceptionalLocales[i];
            final String resourceName = LOCALE_NAME_RESOURCE_PREFIX + localeString;
            final int resId = res.getIdentifier(resourceName, null, RESOURCE_PACKAGE_NAME);
            sExceptionalLocaleToNameIdsMap.put(localeString, resId);
        }
    }
    private static Locale getDisplayLocale(final String localeString) {
        if (sExceptionalLocaleDisplayedInRootLocale.containsKey(localeString)) {
            return Locale.ROOT;
        }
        return LocaleUtils.constructLocaleFromString(localeString);
    }
        public static String getLocaleDisplayNameInSystemLocale(
            final String localeString) {
        final Locale displayLocale = sResources.getConfiguration().locale;
        return getLocaleDisplayNameInternal(localeString, displayLocale);
    }
        public static String getLocaleDisplayNameInLocale(final String localeString) {
        final Locale displayLocale = getDisplayLocale(localeString);
        return getLocaleDisplayNameInternal(localeString, displayLocale);
    }
        public static String getLanguageDisplayNameInSystemLocale(
            final String localeString) {
        final Locale displayLocale = sResources.getConfiguration().locale;
        final String languageString;
        if (sExceptionalLocaleDisplayedInRootLocale.containsKey(localeString)) {
            languageString = localeString;
        } else {
            languageString = LocaleUtils.constructLocaleFromString(localeString).getLanguage();
        }
        return getLocaleDisplayNameInternal(languageString, displayLocale);
    }
        public static String getLanguageDisplayNameInLocale(final String localeString) {
        final Locale displayLocale = getDisplayLocale(localeString);
        final String languageString;
        if (sExceptionalLocaleDisplayedInRootLocale.containsKey(localeString)) {
            languageString = localeString;
        } else {
            languageString = LocaleUtils.constructLocaleFromString(localeString).getLanguage();
        }
        return getLocaleDisplayNameInternal(languageString, displayLocale);
    }
    private static String getLocaleDisplayNameInternal(final String localeString,
                                                       final Locale displayLocale) {
        final Integer exceptionalNameResId;
        if (displayLocale.equals(Locale.ROOT)
                && sExceptionalLocaleDisplayedInRootLocale.containsKey(localeString)) {
            exceptionalNameResId = sExceptionalLocaleDisplayedInRootLocale.get(localeString);
        } else if (sExceptionalLocaleToNameIdsMap.containsKey(localeString)) {
            exceptionalNameResId = sExceptionalLocaleToNameIdsMap.get(localeString);
        } else {
            exceptionalNameResId = null;
        }
        final String displayName;
        if (exceptionalNameResId != null) {
            displayName = sResources.getString(exceptionalNameResId);
        } else {
            displayName = LocaleUtils.constructLocaleFromString(localeString)
                    .getDisplayName(displayLocale);
        }
        return StringUtils.capitalizeEachWord(displayName, displayLocale);
    }
}
