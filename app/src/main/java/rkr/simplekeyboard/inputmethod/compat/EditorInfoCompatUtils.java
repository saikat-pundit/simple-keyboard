package rkr.simplekeyboard.inputmethod.compat;

import android.os.LocaleList;
import android.view.inputmethod.EditorInfo;

import java.util.Locale;

public final class EditorInfoCompatUtils {
    private EditorInfoCompatUtils() {
        // This utility class is not publicly instantiable.
    }

    public static String imeActionName(final int imeOptions) {
        final int actionId = imeOptions & EditorInfo.IME_MASK_ACTION;
        switch (actionId) {
        case EditorInfo.IME_ACTION_UNSPECIFIED:
            return "actionUnspecified";
        case EditorInfo.IME_ACTION_NONE:
            return "actionNone";
        case EditorInfo.IME_ACTION_GO:
            return "actionGo";
        case EditorInfo.IME_ACTION_SEARCH:
            return "actionSearch";
        case EditorInfo.IME_ACTION_SEND:
            return "actionSend";
        case EditorInfo.IME_ACTION_NEXT:
            return "actionNext";
        case EditorInfo.IME_ACTION_DONE:
            return "actionDone";
        case EditorInfo.IME_ACTION_PREVIOUS:
            return "actionPrevious";
        default:
            return "actionUnknown(" + actionId + ")";
        }
    }

    public static Locale getPrimaryHintLocale(final EditorInfo editorInfo) {
        if (editorInfo == null) {
            return null;
        }

        LocaleList localeList = editorInfo.hintLocales;
        if (localeList != null && !localeList.isEmpty())
            return localeList.get(0);

        return null;
    }
}
