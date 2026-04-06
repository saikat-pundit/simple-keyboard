package rkr.simplekeyboard.inputmethod.latin.common;
import android.content.res.Resources;
import android.os.LocaleList;
import android.text.TextUtils;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import rkr.simplekeyboard.inputmethod.latin.utils.LocaleResourceUtils;
public final class LocaleUtils {
    private LocaleUtils() {
    }
    private static final HashMap<String, Locale> sLocaleCache = new HashMap<>();
        public static Locale constructLocaleFromString(final String localeString) {
        synchronized (sLocaleCache) {
            if (sLocaleCache.containsKey(localeString)) {
                return sLocaleCache.get(localeString);
            }
            final String[] elements = localeString.split("_", 3);
            final Locale locale;
            if (elements.length == 1) {
                locale = new Locale(elements[0] );
            } else if (elements.length == 2) {
                locale = new Locale(elements[0] , elements[1] );
            } else {
                locale = new Locale(elements[0] , elements[1] ,
                        elements[2] );
            }
            sLocaleCache.put(localeString, locale);
            return locale;
        }
    }
        public static String getLocaleString(final Locale locale) {
        if (!TextUtils.isEmpty(locale.getVariant())) {
            return locale.getLanguage() + "_" + locale.getCountry() + "_" + locale.getVariant();
        }
        if (!TextUtils.isEmpty(locale.getCountry())) {
            return locale.getLanguage() + "_" + locale.getCountry();
        }
        return locale.getLanguage();
    }
        public static Locale findBestLocale(final Locale localeToMatch,
                                        final Collection<Locale> options) {
        for (final Locale locale : options) {
            if (locale.equals(localeToMatch)) {
                return locale;
            }
        }
        for (final Locale locale : options) {
            if (locale.getLanguage().equals(localeToMatch.getLanguage()) &&
                    locale.getCountry().equals(localeToMatch.getCountry()) &&
                    locale.getVariant().equals(localeToMatch.getVariant())) {
                return locale;
            }
        }
        for (final Locale locale : options) {
            if (locale.getLanguage().equals(localeToMatch.getLanguage()) &&
                    locale.getCountry().equals(localeToMatch.getCountry())) {
                return locale;
            }
        }
        for (final Locale locale : options) {
            if (locale.getLanguage().equals(localeToMatch.getLanguage())) {
                return locale;
            }
        }
        return null;
    }
        public static List<Locale> getSystemLocales() {
        ArrayList<Locale> locales = new ArrayList<>();
        LocaleList localeList = Resources.getSystem().getConfiguration().getLocales();
        for (int i = 0; i < localeList.size(); i++) {
            locales.add(localeList.get(i));
        }
        return locales;
    }
        public static class LocaleComparator implements Comparator<Locale> {
        @Override
        public int compare(Locale a, Locale b) {
            if (a.equals(b)) {
                return 0;
            }
            final String aDisplay =
                    LocaleResourceUtils.getLocaleDisplayNameInSystemLocale(getLocaleString(a));
            final String bDisplay =
                    LocaleResourceUtils.getLocaleDisplayNameInSystemLocale(getLocaleString(b));
            final int result = aDisplay.compareToIgnoreCase(bDisplay);
            if (result != 0) {
                return result;
            }
            return a.hashCode() > b.hashCode() ? 1 : -1;
        }
    }
}
