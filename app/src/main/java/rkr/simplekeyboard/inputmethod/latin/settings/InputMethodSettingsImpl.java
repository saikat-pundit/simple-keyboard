package rkr.simplekeyboard.inputmethod.latin.settings;
import android.content.Context;
import android.content.RestrictionsManager;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceScreen;
import android.text.TextUtils;
import java.util.Set;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat;
import rkr.simplekeyboard.inputmethod.latin.Subtype;
import rkr.simplekeyboard.inputmethod.latin.RichInputMethodManager;
 class InputMethodSettingsImpl {
    private Preference mSubtypeEnablerPreference;
    private RichInputMethodManager mRichImm;
        public boolean init(final Context context, final PreferenceScreen prefScreen) {
        RichInputMethodManager.init(context);
        mRichImm = RichInputMethodManager.getInstance();
        final SharedPreferences prefs = PreferenceManagerCompat.getDeviceSharedPreferences(context);
        final RestrictionsManager restrictionsMgr = (RestrictionsManager) context.getSystemService(Context.RESTRICTIONS_SERVICE);
        final Set<String> restrictionKeys = Settings.loadRestrictions(restrictionsMgr, prefs);
        mSubtypeEnablerPreference = new Preference(context);
        mSubtypeEnablerPreference.setTitle(R.string.select_language);
        mSubtypeEnablerPreference.setFragment(LanguagesSettingsFragment.class.getName());
        mSubtypeEnablerPreference.setEnabled(!restrictionKeys.contains(Settings.PREF_ENABLED_SUBTYPES));
        prefScreen.addPreference(mSubtypeEnablerPreference);
        updateEnabledSubtypeList();
        return true;
    }
    private static String getEnabledSubtypesLabel(final RichInputMethodManager richImm) {
        if (richImm == null) {
            return null;
        }
        final Set<Subtype> subtypes = richImm.getEnabledSubtypes(true);
        final StringBuilder sb = new StringBuilder();
        for (final Subtype subtype : subtypes) {
            if (sb.length() > 0) {
                sb.append(", ");
            }
            sb.append(subtype.getName());
        }
        return sb.toString();
    }
    public void updateEnabledSubtypeList() {
        if (mSubtypeEnablerPreference != null) {
            final String summary = getEnabledSubtypesLabel(mRichImm);
            if (!TextUtils.isEmpty(summary)) {
                mSubtypeEnablerPreference.setSummary(summary);
            }
        }
    }
}
