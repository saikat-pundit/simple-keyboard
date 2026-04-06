package rkr.simplekeyboard.inputmethod.latin.utils;
import android.content.Context;
import android.view.ContextThemeWrapper;
import rkr.simplekeyboard.inputmethod.R;
public final class DialogUtils {
    private DialogUtils() {
    }
    public static Context getPlatformDialogThemeContext(final Context context) {
        return new ContextThemeWrapper(context, R.style.platformDialogTheme);
    }
}
