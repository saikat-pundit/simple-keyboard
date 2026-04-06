package rkr.simplekeyboard.inputmethod.compat;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PreferenceManagerCompat {
    public static Context getDeviceContext(Context context) {
        return context.createDeviceProtectedStorageContext();
    }

    public static SharedPreferences getDeviceSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(getDeviceContext(context));
    }
}
