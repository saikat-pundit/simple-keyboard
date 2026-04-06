package rkr.simplekeyboard.inputmethod.latin;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import rkr.simplekeyboard.inputmethod.keyboard.KeyboardLayoutSet;
import rkr.simplekeyboard.inputmethod.latin.utils.LocaleResourceUtils;
public final class SystemBroadcastReceiver extends BroadcastReceiver {
    private static final String TAG = SystemBroadcastReceiver.class.getSimpleName();
    @Override
    public void onReceive(final Context context, final Intent intent) {
        final String intentAction = intent.getAction();
        if (Intent.ACTION_LOCALE_CHANGED.equals(intentAction)) {
            Log.i(TAG, "System locale changed");
            RichInputMethodManager.getInstance().reloadSubtypes(context);
            LocaleResourceUtils.onLocalChange(context);
        }
    }
}
