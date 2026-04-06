package rkr.simplekeyboard.inputmethod.latin;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.inputmethodservice.InputMethodService;
import android.os.IBinder;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.style.RelativeSizeSpan;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.Executors;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.compat.PreferenceManagerCompat;
import rkr.simplekeyboard.inputmethod.latin.common.LocaleUtils;
import rkr.simplekeyboard.inputmethod.latin.settings.Settings;
import rkr.simplekeyboard.inputmethod.latin.utils.DialogUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.SubtypePreferenceUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.LocaleResourceUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.SubtypeLocaleUtils;
public class RichInputMethodManager {
    private static final String TAG = RichInputMethodManager.class.getSimpleName();
    private RichInputMethodManager() {
    }
    private static final RichInputMethodManager sInstance = new RichInputMethodManager();
    private InputMethodManager mImmService;
    private SubtypeList mSubtypeList;
    public static RichInputMethodManager getInstance() {
        sInstance.checkInitialized();
        return sInstance;
    }
    public static void init(final Context context) {
        sInstance.initInternal(context);
    }
    private boolean isInitialized() {
        return mImmService != null;
    }
    private void checkInitialized() {
        if (!isInitialized()) {
            throw new RuntimeException(TAG + " is used before initialization");
        }
    }
    private void initInternal(final Context context) {
        if (isInitialized()) {
            return;
        }
        mImmService = (InputMethodManager)context.getSystemService(Context.INPUT_METHOD_SERVICE);
        LocaleResourceUtils.init(context);
        mSubtypeList = new SubtypeList(context);
    }
    public void reloadSubtypes(final Context context) {
        mSubtypeList.reload(context);
    }
        public void setSubtypeChangeHandler(final SubtypeChangedListener listener) {
        mSubtypeList.setSubtypeChangeHandler(listener);
    }
        public interface SubtypeChangedListener {
        void onCurrentSubtypeChanged();
    }
        private static class SubtypeList {
                private List<Subtype> mSubtypes;
                private int mCurrentSubtypeIndex;
        private final SharedPreferences mPrefs;
        private SubtypeChangedListener mSubtypeChangedListener;
                public SubtypeList(final Context context) {
            mPrefs = PreferenceManagerCompat.getDeviceSharedPreferences(context);
            reload(context);
        }
        public void reload(final Context context) {
            final String prefSubtypes = Settings.readPrefSubtypes(mPrefs);
            final List<Subtype> subtypes = SubtypePreferenceUtils.createSubtypesFromPref(
                    prefSubtypes, context.getResources());
            if (subtypes == null || subtypes.size() < 1) {
                mSubtypes = SubtypeLocaleUtils.getDefaultSubtypes(context.getResources());
            } else {
                mSubtypes = subtypes;
            }
            mCurrentSubtypeIndex = 0;
        }
                public void setSubtypeChangeHandler(final SubtypeChangedListener listener) {
            mSubtypeChangedListener = listener;
        }
                public void notifySubtypeChanged() {
            if (mSubtypeChangedListener != null) {
                mSubtypeChangedListener.onCurrentSubtypeChanged();
            }
        }
                public synchronized Set<Subtype> getAllForLocale(final String locale) {
            final Set<Subtype> subtypes = new HashSet<>();
            for (final Subtype subtype: mSubtypes) {
                if (subtype.getLocale().equals(locale))
                    subtypes.add(subtype);
            }
            return subtypes;
        }
                public synchronized Set<Subtype> getAll(final boolean sortForDisplay) {
            final Set<Subtype> subtypes;
            if (sortForDisplay) {
                subtypes = new TreeSet<>(new Comparator<Subtype>() {
                    @Override
                    public int compare(Subtype a, Subtype b) {
                        if (a.equals(b)) {
                            return 0;
                        }
                        final int result = a.getName().compareToIgnoreCase(b.getName());
                        if (result != 0) {
                            return result;
                        }
                        return a.hashCode() > b.hashCode() ? 1 : -1;
                    }
                });
            } else {
                subtypes = new HashSet<>();
            }
            subtypes.addAll(mSubtypes);
            return subtypes;
        }
                public synchronized int size() {
            return mSubtypes.size();
        }
                private void saveSubtypeListPref() {
            final String prefSubtypes = SubtypePreferenceUtils.createPrefSubtypes(mSubtypes);
            Settings.writePrefSubtypes(mPrefs, prefSubtypes);
        }
                public synchronized boolean addSubtype(final Subtype subtype) {
            if (mSubtypes.contains(subtype)) {
                return true;
            }
            if (!mSubtypes.add(subtype)) {
                return false;
            }
            saveSubtypeListPref();
            return true;
        }
                public synchronized boolean removeSubtype(final Subtype subtype) {
            if (mSubtypes.size() == 1) {
                return false;
            }
            final int index = mSubtypes.indexOf(subtype);
            if (index < 0) {
                return true;
            }
            final boolean subtypeChanged;
            if (mCurrentSubtypeIndex == index) {
                mCurrentSubtypeIndex = 0;
                subtypeChanged = true;
            } else {
                if (mCurrentSubtypeIndex > index) {
                    mCurrentSubtypeIndex--;
                }
                subtypeChanged = false;
            }
            mSubtypes.remove(index);
            saveSubtypeListPref();
            if (subtypeChanged) {
                notifySubtypeChanged();
            }
            return true;
        }
                public synchronized void resetSubtypeCycleOrder() {
            if (mCurrentSubtypeIndex == 0) {
                return;
            }
            Collections.rotate(mSubtypes.subList(0, mCurrentSubtypeIndex + 1), 1);
            mCurrentSubtypeIndex = 0;
            saveSubtypeListPref();
        }
                public synchronized boolean setCurrentSubtype(final Subtype subtype) {
            if (getCurrentSubtype().equals(subtype)) {
                return true;
            }
            for (int i = 0; i < mSubtypes.size(); i++) {
                if (mSubtypes.get(i).equals(subtype)) {
                    setCurrentSubtype(i);
                    return true;
                }
            }
            return false;
        }
                public synchronized boolean setCurrentSubtype(final Locale locale) {
            final ArrayList<Locale> enabledLocales = new ArrayList<>(mSubtypes.size());
            for (final Subtype subtype : mSubtypes) {
                enabledLocales.add(subtype.getLocaleObject());
            }
            final Locale bestLocale = LocaleUtils.findBestLocale(locale, enabledLocales);
            if (bestLocale != null) {
                for (int i = 0; i < mSubtypes.size(); i++) {
                    final Subtype subtype = mSubtypes.get(i);
                    if (bestLocale.equals(subtype.getLocaleObject())) {
                        setCurrentSubtype(i);
                        return true;
                    }
                }
            }
            return false;
        }
                private void setCurrentSubtype(final int index) {
            if (mCurrentSubtypeIndex == index)
            {
                return;
            }
            mCurrentSubtypeIndex = index;
            if (index != 0) {
                resetSubtypeCycleOrder();
            }
            notifySubtypeChanged();
        }
                public synchronized boolean switchToNextSubtype(final boolean notifyChangeOnCycle) {
            final int nextIndex = mCurrentSubtypeIndex + 1;
            if (nextIndex >= mSubtypes.size()) {
                mCurrentSubtypeIndex = 0;
                if (!notifyChangeOnCycle) {
                    return false;
                }
            } else {
                mCurrentSubtypeIndex = nextIndex;
            }
            notifySubtypeChanged();
            return true;
        }
                public synchronized Subtype getCurrentSubtype() {
            return mSubtypes.get(mCurrentSubtypeIndex);
        }
    }
        public Set<Subtype> getEnabledSubtypes(final boolean sortForDisplay) {
        return mSubtypeList.getAll(sortForDisplay);
    }
        public Set<Subtype> getEnabledSubtypesForLocale(final String locale) {
        return mSubtypeList.getAllForLocale(locale);
    }
        public boolean hasMultipleEnabledSubtypes() {
        return mSubtypeList.size() > 1;
    }
        public boolean addSubtype(final Subtype subtype) {
        return mSubtypeList.addSubtype(subtype);
    }
        public boolean removeSubtype(final Subtype subtype) {
        return mSubtypeList.removeSubtype(subtype);
    }
        public void resetSubtypeCycleOrder() {
        mSubtypeList.resetSubtypeCycleOrder();
    }
        public boolean setCurrentSubtype(final Subtype subtype) {
        return mSubtypeList.setCurrentSubtype(subtype);
    }
        public boolean setCurrentSubtype(final Locale locale) {
        return mSubtypeList.setCurrentSubtype(locale);
    }
        public boolean switchToNextInputMethod(final IBinder token, final boolean onlyCurrentIme) {
        if (onlyCurrentIme) {
            if (!hasMultipleEnabledSubtypes()) {
                return false;
            }
            return mSubtypeList.switchToNextSubtype(true);
        }
        if (mSubtypeList.switchToNextSubtype(false)) {
            return true;
        }
        if (mImmService.switchToNextInputMethod(token, false)) {
            return true;
        }
        if (hasMultipleEnabledSubtypes()) {
            mSubtypeList.notifySubtypeChanged();
            return true;
        }
        return false;
    }
        public Subtype getCurrentSubtype() {
        return mSubtypeList.getCurrentSubtype();
    }
        public boolean shouldOfferSwitchingToOtherInputMethods(final IBinder binder) {
        return mImmService.shouldOfferSwitchingToNextInputMethod(binder);
    }
        public AlertDialog showSubtypePicker(final Context context, final IBinder windowToken,
                                         final InputMethodService inputMethodService) {
        if (windowToken == null) {
            return null;
        }
        final CharSequence title = context.getString(R.string.change_keyboard);
        final List<SubtypeInfo> subtypeInfoList = getEnabledSubtypeInfoOfAllImes(context);
        if (subtypeInfoList.size() < 2) {
            return null;
        }
        final CharSequence[] items = new CharSequence[subtypeInfoList.size()];
        final Subtype currentSubtype = getCurrentSubtype();
        int currentSubtypeIndex = 0;
        int i = 0;
        for (final SubtypeInfo subtypeInfo : subtypeInfoList) {
            if (subtypeInfo.virtualSubtype != null
                    && subtypeInfo.virtualSubtype.equals(currentSubtype)) {
                currentSubtypeIndex = i;
            }
            final SpannableString itemTitle;
            final SpannableString itemSubtitle;
            if (!TextUtils.isEmpty(subtypeInfo.subtypeName)) {
                itemTitle = new SpannableString(subtypeInfo.subtypeName);
                itemSubtitle = new SpannableString("\n" + subtypeInfo.imeName);
            } else {
                itemTitle = new SpannableString(subtypeInfo.imeName);
                itemSubtitle = new SpannableString("");
            }
            itemTitle.setSpan(new RelativeSizeSpan(0.9f), 0,itemTitle.length(),
                    Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            itemSubtitle.setSpan(new RelativeSizeSpan(0.85f), 0,itemSubtitle.length(),
                    Spannable.SPAN_EXCLUSIVE_INCLUSIVE);
            items[i++] = new SpannableStringBuilder().append(itemTitle).append(itemSubtitle);
        }
        final DialogInterface.OnClickListener listener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface di, int position) {
                di.dismiss();
                int i = 0;
                for (final SubtypeInfo subtypeInfo : subtypeInfoList) {
                    if (i == position) {
                        if (subtypeInfo.virtualSubtype != null) {
                            setCurrentSubtype(subtypeInfo.virtualSubtype);
                        } else {
                            switchToTargetIme(subtypeInfo.imiId, subtypeInfo.systemSubtype,
                                    inputMethodService);
                        }
                        break;
                    }
                    i++;
                }
            }
        };
        final AlertDialog.Builder builder = new AlertDialog.Builder(
                DialogUtils.getPlatformDialogThemeContext(context));
        builder.setSingleChoiceItems(items, currentSubtypeIndex, listener).setTitle(title);
        final AlertDialog dialog = builder.create();
        dialog.setCancelable(true);
        dialog.setCanceledOnTouchOutside(true);
        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams lp = window.getAttributes();
        lp.token = windowToken;
        lp.type = WindowManager.LayoutParams.TYPE_APPLICATION_ATTACHED_DIALOG;
        window.setAttributes(lp);
        window.addFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
        dialog.show();
        return dialog;
    }
        private List<SubtypeInfo> getEnabledSubtypeInfoOfAllImes(final Context context) {
        final List<SubtypeInfo> subtypeInfoList = new ArrayList<>();
        final PackageManager packageManager = context.getPackageManager();
        final Set<InputMethodInfo> imiList = new TreeSet<>(new Comparator<InputMethodInfo>() {
            @Override
            public int compare(InputMethodInfo a, InputMethodInfo b) {
                if (a.equals(b)) {
                    return 0;
                }
                final String labelA = a.loadLabel(packageManager).toString();
                final String labelB = b.loadLabel(packageManager).toString();
                final int result = labelA.compareToIgnoreCase(labelB);
                if (result != 0) {
                    return result;
                }
                return a.hashCode() > b.hashCode() ? 1 : -1;
            }
        });
        imiList.addAll(mImmService.getEnabledInputMethodList());
        for (final InputMethodInfo imi : imiList) {
            final CharSequence imeName = imi.loadLabel(packageManager);
            final String imiId = imi.getId();
            final String packageName = imi.getPackageName();
            if (packageName.equals(context.getPackageName())) {
                for (final Subtype subtype : getEnabledSubtypes(true)) {
                    final SubtypeInfo subtypeInfo = new SubtypeInfo();
                    subtypeInfo.virtualSubtype = subtype;
                    subtypeInfo.subtypeName = subtype.getName();
                    subtypeInfo.imeName = imeName;
                    subtypeInfo.imiId = imiId;
                    subtypeInfoList.add(subtypeInfo);
                }
                continue;
            }
            final List<InputMethodSubtype> subtypes =
                    mImmService.getEnabledInputMethodSubtypeList(imi, true);
            if (subtypes.isEmpty()) {
                final SubtypeInfo subtypeInfo = new SubtypeInfo();
                subtypeInfo.imeName = imeName;
                subtypeInfo.imiId = imiId;
                subtypeInfoList.add(subtypeInfo);
                continue;
            }
            final ApplicationInfo applicationInfo = imi.getServiceInfo().applicationInfo;
            for (final InputMethodSubtype subtype : subtypes) {
                if (subtype.isAuxiliary()) {
                    continue;
                }
                final SubtypeInfo subtypeInfo = new SubtypeInfo();
                subtypeInfo.systemSubtype = subtype;
                if (!subtype.overridesImplicitlyEnabledSubtype()) {
                    subtypeInfo.subtypeName = subtype.getDisplayName(context, packageName,
                            applicationInfo);
                }
                subtypeInfo.imeName = imeName;
                subtypeInfo.imiId = imiId;
                subtypeInfoList.add(subtypeInfo);
            }
        }
        return subtypeInfoList;
    }
        private static class SubtypeInfo {
        public InputMethodSubtype systemSubtype;
        public Subtype virtualSubtype;
        public CharSequence subtypeName;
        public CharSequence imeName;
        public String imiId;
    }
        private void switchToTargetIme(final String imiId, final InputMethodSubtype subtype,
                                   final InputMethodService context) {
        final IBinder token = context.getWindow().getWindow().getAttributes().token;
        if (token == null) {
            return;
        }
        final InputMethodManager imm = mImmService;
        Executors.newSingleThreadExecutor().execute(new Runnable() {
            @Override
            public void run() {
                imm.setInputMethodAndSubtype(token, imiId, subtype);
            }
        });
    }
}
