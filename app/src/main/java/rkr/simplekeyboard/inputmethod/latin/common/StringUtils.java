package rkr.simplekeyboard.inputmethod.latin.common;
import android.text.TextUtils;
import java.util.Arrays;
import java.util.Locale;
public final class StringUtils {
    private StringUtils() {
    }
    public static int codePointCount(final CharSequence text) {
        if (TextUtils.isEmpty(text)) {
            return 0;
        }
        return Character.codePointCount(text, 0, text.length());
    }
    public static String newSingleCodePointString(final int codePoint) {
        if (Character.charCount(codePoint) == 1) {
            return String.valueOf((char) codePoint);
        }
        return new String(Character.toChars(codePoint));
    }
    public static boolean containsInArray(final String text,
            final String[] array) {
        for (final String element : array) {
            if (text.equals(element)) {
                return true;
            }
        }
        return false;
    }
    public static int[] toCodePointArray(final CharSequence charSequence) {
        return toCodePointArray(charSequence, 0, charSequence.length());
    }
    private static final int[] EMPTY_CODEPOINTS = {};
        public static int[] toCodePointArray(final CharSequence charSequence,
            final int startIndex, final int endIndex) {
        final int length = charSequence.length();
        if (length <= 0) {
            return EMPTY_CODEPOINTS;
        }
        final int[] codePoints =
                new int[Character.codePointCount(charSequence, startIndex, endIndex)];
        copyCodePointsAndReturnCodePointCount(codePoints, charSequence, startIndex, endIndex,
                false );
        return codePoints;
    }
        public static int copyCodePointsAndReturnCodePointCount(final int[] destination,
            final CharSequence charSequence, final int startIndex, final int endIndex,
            final boolean downCase) {
        int destIndex = 0;
        for (int index = startIndex; index < endIndex;
                index = Character.offsetByCodePoints(charSequence, index, 1)) {
            final int codePoint = Character.codePointAt(charSequence, index);
            destination[destIndex] = downCase ? Character.toLowerCase(codePoint) : codePoint;
            destIndex++;
        }
        return destIndex;
    }
    public static int[] toSortedCodePointArray(final String string) {
        final int[] codePoints = toCodePointArray(string);
        Arrays.sort(codePoints);
        return codePoints;
    }
    public static boolean isIdenticalAfterUpcase(final String text) {
        final int length = text.length();
        int i = 0;
        while (i < length) {
            final int codePoint = text.codePointAt(i);
            if (Character.isLetter(codePoint) && !Character.isUpperCase(codePoint)) {
                return false;
            }
            i += Character.charCount(codePoint);
        }
        return true;
    }
    public static boolean isIdenticalAfterDowncase(final String text) {
        final int length = text.length();
        int i = 0;
        while (i < length) {
            final int codePoint = text.codePointAt(i);
            if (Character.isLetter(codePoint) && !Character.isLowerCase(codePoint)) {
                return false;
            }
            i += Character.charCount(codePoint);
        }
        return true;
    }
    public static boolean isIdenticalAfterCapitalizeEachWord(final String text) {
        boolean needsCapsNext = true;
        final int len = text.length();
        for (int i = 0; i < len; i = text.offsetByCodePoints(i, 1)) {
            final int codePoint = text.codePointAt(i);
            if (Character.isLetter(codePoint)) {
                if ((needsCapsNext && !Character.isUpperCase(codePoint))
                        || (!needsCapsNext && !Character.isLowerCase(codePoint))) {
                    return false;
                }
            }
            needsCapsNext = Character.isWhitespace(codePoint);
        }
        return true;
    }
    public static String capitalizeEachWord(final String text, final Locale locale) {
        final StringBuilder builder = new StringBuilder();
        boolean needsCapsNext = true;
        final int len = text.length();
        for (int i = 0; i < len; i = text.offsetByCodePoints(i, 1)) {
            final String nextChar = text.substring(i, text.offsetByCodePoints(i, 1));
            if (needsCapsNext) {
                builder.append(toTitleCaseOfKeyLabel(nextChar, locale));
            } else {
                builder.append(toLowerCaseOfKeyLabel(nextChar, locale));
            }
            needsCapsNext = Character.isWhitespace(nextChar.codePointAt(0));
        }
        return builder.toString();
    }
    private static final String LANGUAGE_GREEK = "el";
    private static Locale getLocaleUsedForToTitleCase(final Locale locale) {
        if (LANGUAGE_GREEK.equals(locale.getLanguage())) {
            return Locale.ROOT;
        }
        return locale;
    }
    public static String toLowerCase(final String text, final Locale locale) {
        final StringBuilder builder = new StringBuilder();
        final int len = text.length();
        for (int i = 0; i < len; i = text.offsetByCodePoints(i, 1)) {
            final String nextChar = text.substring(i, text.offsetByCodePoints(i, 1));
            builder.append(toLowerCaseOfKeyLabel(nextChar, locale));
        }
        return builder.toString();
    }
    public static String toUpperCase(final String text, final Locale locale) {
        final StringBuilder builder = new StringBuilder();
        final int len = text.length();
        for (int i = 0; i < len; i = text.offsetByCodePoints(i, 1)) {
            final String nextChar = text.substring(i, text.offsetByCodePoints(i, 1));
            builder.append(toTitleCaseOfKeyLabel(nextChar, locale));
        }
        return builder.toString();
    }
    public static String toLowerCaseOfKeyLabel(final String label, final Locale locale) {
        if (label == null) {
            return null;
        }
        switch (label) {
            case "\u1E9E":
                return "\u00DF";
            default:
                return label.toLowerCase(getLocaleUsedForToTitleCase(locale));
        }
    }
    public static String toTitleCaseOfKeyLabel(final String label, final Locale locale) {
        if (label == null) {
            return null;
        }
        switch (label) {
            case "\u00DF":
                return "\u1E9E";
            default:
                return label.toUpperCase(getLocaleUsedForToTitleCase(locale));
        }
    }
    public static int toTitleCaseOfKeyCode(final int code, final Locale locale) {
        if (!Constants.isLetterCode(code)) {
            return code;
        }
        final String label = newSingleCodePointString(code);
        final String titleCaseLabel = toTitleCaseOfKeyLabel(label, locale);
        return codePointCount(titleCaseLabel) == 1
                ? titleCaseLabel.codePointAt(0) : Constants.CODE_UNSPECIFIED;
    }
}
