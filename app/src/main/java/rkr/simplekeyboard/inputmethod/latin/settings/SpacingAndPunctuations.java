package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.res.Resources;

import java.util.Arrays;
import java.util.Locale;

import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.latin.common.StringUtils;

public final class SpacingAndPunctuations {
    public final int[] mSortedWordSeparators;
    private final int mSentenceSeparator;
    private final int mAbbreviationMarker;
    private final int[] mSortedSentenceTerminators;
    public final boolean mUsesAmericanTypography;
    public final boolean mUsesGermanRules;

    public SpacingAndPunctuations(final Resources res) {
        mSortedWordSeparators = StringUtils.toSortedCodePointArray(
                res.getString(R.string.symbols_word_separators));
        mSortedSentenceTerminators = StringUtils.toSortedCodePointArray(
                res.getString(R.string.symbols_sentence_terminators));
        mSentenceSeparator = res.getInteger(R.integer.sentence_separator);
        mAbbreviationMarker = res.getInteger(R.integer.abbreviation_marker);
        final Locale locale = res.getConfiguration().locale;
        // Heuristic: we use American Typography rules because it's the most common rules for all
        // English variants. German rules (not "German typography") also have small gotchas.
        mUsesAmericanTypography = Locale.ENGLISH.getLanguage().equals(locale.getLanguage());
        mUsesGermanRules = Locale.GERMAN.getLanguage().equals(locale.getLanguage());
    }

    public boolean isWordSeparator(final int code) {
        return Arrays.binarySearch(mSortedWordSeparators, code) >= 0;
    }

    public boolean isSentenceTerminator(final int code) {
        return Arrays.binarySearch(mSortedSentenceTerminators, code) >= 0;
    }

    public boolean isAbbreviationMarker(final int code) {
        return code == mAbbreviationMarker;
    }

    public boolean isSentenceSeparator(final int code) {
        return code == mSentenceSeparator;
    }
}
