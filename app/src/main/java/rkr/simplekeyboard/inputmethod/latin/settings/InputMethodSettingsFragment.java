package rkr.simplekeyboard.inputmethod.latin.settings;

import android.content.Context;
import android.os.Bundle;
import android.preference.PreferenceFragment;

import rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat;

/**
 * This is a helper class for an IME's settings preference fragment. It's recommended for every
 * IME to have its own settings preference fragment which inherits this class.
 */
public abstract class InputMethodSettingsFragment extends PreferenceFragment {
    private final InputMethodSettingsImpl mSettings = new InputMethodSettingsImpl();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        final Context context = getActivity();
        setPreferenceScreen(getPreferenceManager().createPreferenceScreen(
                PreferenceManagerCompat.getDeviceContext(context)));
        mSettings.init(context, getPreferenceScreen());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        mSettings.updateEnabledSubtypeList();
    }
}
