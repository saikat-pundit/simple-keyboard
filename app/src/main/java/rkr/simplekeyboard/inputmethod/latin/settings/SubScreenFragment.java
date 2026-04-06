package rkr.simplekeyboard.inputmethod.latin.settings;
import android.app.backup.BackupManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceGroup;
import android.preference.PreferenceScreen;
import android.util.Log;
import java.util.Set;
import rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat;
public abstract class SubScreenFragment extends PreferenceFragment
        implements OnSharedPreferenceChangeListener {
    private OnSharedPreferenceChangeListener mSharedPreferenceChangeListener;
    static void setPreferenceEnabled(final String prefKey, final boolean enabled,
            final PreferenceScreen screen) {
        final Preference preference = screen.findPreference(prefKey);
        if (preference != null) {
            preference.setEnabled(enabled);
        }
    }
    static void removePreference(final String prefKey, final PreferenceScreen screen) {
        final Preference preference = screen.findPreference(prefKey);
        if (preference != null) {
            screen.removePreference(preference);
        }
    }
    final void setPreferenceEnabled(final String prefKey, final boolean enabled) {
        setPreferenceEnabled(prefKey, enabled, getPreferenceScreen());
    }
    final void removePreference(final String prefKey) {
        removePreference(prefKey, getPreferenceScreen());
    }
    final SharedPreferences getSharedPreferences() {
        return PreferenceManagerCompat.getDeviceSharedPreferences(getActivity());
    }
    @Override
    public void addPreferencesFromResource(final int preferencesResId) {
        super.addPreferencesFromResource(preferencesResId);
        final Set<String> restrictionKeys = getSharedPreferences().getStringSet(Settings.ACTIVE_RESTRICTIONS, null);
        if (restrictionKeys != null && !restrictionKeys.isEmpty()) {
            final PreferenceGroup group = getPreferenceScreen();
            final int count = group.getPreferenceCount();
            for (int index = 0; index < count; index++) {
                final Preference preference = group.getPreference(index);
                if (restrictionKeys.contains(preference.getKey())) {
                    preference.setEnabled(false);
                }
            }
        }
    }
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        super.getPreferenceManager().setStorageDeviceProtected();
        mSharedPreferenceChangeListener = new OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
                final SubScreenFragment fragment = SubScreenFragment.this;
                final Context context = fragment.getActivity();
                if (context == null || fragment.getPreferenceScreen() == null) {
                    final String tag = fragment.getClass().getSimpleName();
                    Log.w(tag, "onSharedPreferenceChanged called before activity starts.");
                    return;
                }
                new BackupManager(context).dataChanged();
                fragment.onSharedPreferenceChanged(prefs, key);
            }
        };
        getSharedPreferences().registerOnSharedPreferenceChangeListener(
                mSharedPreferenceChangeListener);
    }
    @Override
    public void onDestroy() {
        getSharedPreferences().unregisterOnSharedPreferenceChangeListener(
                mSharedPreferenceChangeListener);
        super.onDestroy();
    }
    @Override
    public void onSharedPreferenceChanged(final SharedPreferences prefs, final String key) {
    }
}
