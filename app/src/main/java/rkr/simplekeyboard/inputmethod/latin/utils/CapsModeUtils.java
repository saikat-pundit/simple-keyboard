package rkr.simplekeyboard.inputmethod.latin.utils;
import android.text.InputType;
import android.text.TextUtils;
import java.util.ArrayList;
import rkr.simplekeyboard.inputmethod.latin.common.Constants;
import rkr.simplekeyboard.inputmethod.latin.settings.SpacingAndPunctuations;
public final class CapsModeUtils {
    private CapsModeUtils() {
    }
        private static boolean isStartPunctuation(final int codePoint) {
        return (codePoint == Constants.CODE_DOUBLE_QUOTE || codePoint == Constants.CODE_SINGLE_QUOTE
                || codePoint == Constants.CODE_INVERTED_QUESTION_MARK
                || codePoint == Constants.CODE_INVERTED_EXCLAMATION_MARK
                || Character.getType(codePoint) == Character.START_PUNCTUATION);
    }
        public static int getCapsMode(final CharSequence cs, final int reqModes,
            final SpacingAndPunctuations spacingAndPunctuations) {
        if ((reqModes & (TextUtils.CAP_MODE_WORDS | TextUtils.CAP_MODE_SENTENCES)) == 0) {
            return TextUtils.CAP_MODE_CHARACTERS & reqModes;
        }
        int i;
        for (i = cs.length(); i > 0; i--) {
            final char c = cs.charAt(i - 1);
            if (!isStartPunctuation(c)) {
                break;
            }
        }
        final int newCapIndex = i;
        char prevChar = Constants.CODE_SPACE;
        while (i > 0) {
            prevChar = cs.charAt(i - 1);
            if (!Character.isSpaceChar(prevChar) && prevChar != Constants.CODE_TAB) {
                break;
            }
            i--;
        }
        if (i <= 0 || Character.isWhitespace(prevChar)) {
            if (spacingAndPunctuations.mUsesGermanRules) {
                boolean hasNewLine = false;
                while (--i >= 0 && Character.isWhitespace(prevChar)) {
                    if (Constants.CODE_ENTER == prevChar) {
                        hasNewLine = true;
                    }
                    prevChar = cs.charAt(i);
                }
                if (Constants.CODE_COMMA == prevChar && hasNewLine) {
                    return (TextUtils.CAP_MODE_CHARACTERS | TextUtils.CAP_MODE_WORDS) & reqModes;
                }
            }
            return (TextUtils.CAP_MODE_CHARACTERS | TextUtils.CAP_MODE_WORDS
                    | TextUtils.CAP_MODE_SENTENCES) & reqModes;
        }
        if (newCapIndex == i) {
            if (spacingAndPunctuations.isWordSeparator(cs.charAt(cs.length() - 1))) {
                return (TextUtils.CAP_MODE_CHARACTERS | TextUtils.CAP_MODE_WORDS) & reqModes;
            }
            return TextUtils.CAP_MODE_CHARACTERS & reqModes;
        }
        if ((reqModes & TextUtils.CAP_MODE_SENTENCES) == 0) {
            return (TextUtils.CAP_MODE_CHARACTERS | TextUtils.CAP_MODE_WORDS) & reqModes;
        }
        if (spacingAndPunctuations.mUsesAmericanTypography) {
            for (; i > 0; i--) {
                final char c = cs.charAt(i - 1);
                if (c != Constants.CODE_DOUBLE_QUOTE && c != Constants.CODE_SINGLE_QUOTE
                        && Character.getType(c) != Character.END_PUNCTUATION) {
                    break;
                }
            }
        }
        if (i <= 0) {
            return TextUtils.CAP_MODE_CHARACTERS & reqModes;
        }
        char c = cs.charAt(--i);
        if (spacingAndPunctuations.isSentenceTerminator(c)
                && !spacingAndPunctuations.isAbbreviationMarker(c)) {
            return (TextUtils.CAP_MODE_CHARACTERS | TextUtils.CAP_MODE_WORDS
                    | TextUtils.CAP_MODE_SENTENCES) & reqModes;
        }
        if (!spacingAndPunctuations.isSentenceSeparator(c) || i <= 0) {
            return (TextUtils.CAP_MODE_CHARACTERS | TextUtils.CAP_MODE_WORDS) & reqModes;
        }
        final int START = 0;
        final int WORD = 1;
        final int PERIOD = 2;
        final int LETTER = 3;
        final int NUMBER = 4;
        final int caps = (TextUtils.CAP_MODE_CHARACTERS | TextUtils.CAP_MODE_WORDS
                | TextUtils.CAP_MODE_SENTENCES) & reqModes;
        final int noCaps = (TextUtils.CAP_MODE_CHARACTERS | TextUtils.CAP_MODE_WORDS) & reqModes;
        int state = START;
        while (i > 0) {
            c = cs.charAt(--i);
            switch (state) {
            case START:
                if (Character.isLetter(c)) {
                    state = WORD;
                } else if (Character.isWhitespace(c)) {
                    return noCaps;
                } else if (Character.isDigit(c) && spacingAndPunctuations.mUsesGermanRules) {
                    state = NUMBER;
                } else {
                    return caps;
                }
                break;
            case WORD:
                if (Character.isLetter(c)) {
                    state = WORD;
                } else if (spacingAndPunctuations.isSentenceSeparator(c)) {
                    state = PERIOD;
                } else {
                    return caps;
                }
                break;
            case PERIOD:
                if (Character.isLetter(c)) {
                    state = LETTER;
                } else {
                    return caps;
                }
                break;
            case LETTER:
                if (Character.isLetter(c)) {
                    state = LETTER;
                } else if (spacingAndPunctuations.isSentenceSeparator(c)) {
                    state = PERIOD;
                } else {
                    return noCaps;
                }
                break;
            case NUMBER:
                if (Character.isLetter(c)) {
                    state = WORD;
                } else if (Character.isDigit(c)) {
                    state = NUMBER;
                } else {
                    return noCaps;
                }
            }
        }
        return (START == state || LETTER == state) ? noCaps : caps;
    }
        public static String flagsToString(final int capsFlags) {
        final int capsFlagsMask = TextUtils.CAP_MODE_CHARACTERS | TextUtils.CAP_MODE_WORDS
                | TextUtils.CAP_MODE_SENTENCES;
        if ((capsFlags & ~capsFlagsMask) != 0) {
            return "unknown<0x" + Integer.toHexString(capsFlags) + ">";
        }
        final ArrayList<String> builder = new ArrayList<>();
        if ((capsFlags & android.text.TextUtils.CAP_MODE_CHARACTERS) != 0) {
            builder.add("characters");
        }
        if ((capsFlags & android.text.TextUtils.CAP_MODE_WORDS) != 0) {
            builder.add("words");
        }
        if ((capsFlags & android.text.TextUtils.CAP_MODE_SENTENCES) != 0) {
            builder.add("sentences");
        }
        return builder.isEmpty() ? "none" : TextUtils.join("|", builder);
    }
}
