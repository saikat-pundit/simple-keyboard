package rkr.simplekeyboard.inputmethod.latin;

import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.EditorInfo;

import rkr.simplekeyboard.inputmethod.latin.utils.InputTypeUtils;

/**
 * Class to hold attributes of the input field.
 */
public final class InputAttributes {
    private final String TAG = InputAttributes.class.getSimpleName();

    final public String mTargetApplicationPackageName;
    final public boolean mInputTypeNoAutoCorrect;
    final public boolean mIsPasswordField;
    final public boolean mShouldShowSuggestions;
    final public boolean mApplicationSpecifiedCompletionOn;
    final public boolean mShouldInsertSpacesAutomatically;
    /**
     * Whether the floating gesture preview should be disabled. If true, this should override the
     * corresponding keyboard settings preference, always suppressing the floating preview text.
     */
    final private int mInputType;

    public InputAttributes(final EditorInfo editorInfo, final boolean isFullscreenMode) {
        mTargetApplicationPackageName = null != editorInfo ? editorInfo.packageName : null;
        final int inputType = null != editorInfo ? editorInfo.inputType : 0;
        final int inputClass = inputType & InputType.TYPE_MASK_CLASS;
        mInputType = inputType;
        mIsPasswordField = InputTypeUtils.isPasswordInputType(inputType)
                || InputTypeUtils.isVisiblePasswordInputType(inputType);
        if (inputClass != InputType.TYPE_CLASS_TEXT) {
            // If we are not looking at a TYPE_CLASS_TEXT field, the following strange
            // cases may arise, so we do a couple sanity checks for them. If it's a
            // TYPE_CLASS_TEXT field, these special cases cannot happen, by construction
            // of the flags.
            if (null == editorInfo) {
                Log.w(TAG, "No editor info for this field. Bug?");
            } else if (InputType.TYPE_NULL == inputType) {
                // TODO: We should honor TYPE_NULL specification.
                Log.i(TAG, "InputType.TYPE_NULL is specified");
            } else if (inputClass == 0) {
                // TODO: is this check still necessary?
                Log.w(TAG, String.format("Unexpected input class: inputType=0x%08x"
                        + " imeOptions=0x%08x", inputType, editorInfo.imeOptions));
            }
            mShouldShowSuggestions = false;
            mInputTypeNoAutoCorrect = false;
            mApplicationSpecifiedCompletionOn = false;
            mShouldInsertSpacesAutomatically = false;
            return;
        }
        // inputClass == InputType.TYPE_CLASS_TEXT
        final int variation = inputType & InputType.TYPE_MASK_VARIATION;
        final boolean flagNoSuggestions =
                0 != (inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        final boolean flagMultiLine =
                0 != (inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        final boolean flagAutoCorrect =
                0 != (inputType & InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        final boolean flagAutoComplete =
                0 != (inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);

        // TODO: Have a helper method in InputTypeUtils
        // Make sure that passwords are not displayed in {@link SuggestionStripView}.
        final boolean shouldSuppressSuggestions = mIsPasswordField
                || InputTypeUtils.isEmailVariation(variation)
                || InputType.TYPE_TEXT_VARIATION_URI == variation
                || InputType.TYPE_TEXT_VARIATION_FILTER == variation
                || flagNoSuggestions
                || flagAutoComplete;
        mShouldShowSuggestions = !shouldSuppressSuggestions;

        mShouldInsertSpacesAutomatically = InputTypeUtils.isAutoSpaceFriendlyType(inputType);

        // If it's a browser edit field and auto correct is not ON explicitly, then
        // disable auto correction, but keep suggestions on.
        // If NO_SUGGESTIONS is set, don't do prediction.
        // If it's not multiline and the autoCorrect flag is not set, then don't correct
        mInputTypeNoAutoCorrect =
                (variation == InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT && !flagAutoCorrect)
                || flagNoSuggestions
                || (!flagAutoCorrect && !flagMultiLine);

        mApplicationSpecifiedCompletionOn = flagAutoComplete && isFullscreenMode;
    }

    public boolean isSameInputType(final EditorInfo editorInfo) {
        return editorInfo.inputType == mInputType;
    }

    // Pretty print
    @Override
    public String toString() {
        return String.format(
                "%s: inputType=0x%08x%s%s%s%s%s targetApp=%s\n", getClass().getSimpleName(),
                mInputType,
                (mInputTypeNoAutoCorrect ? " noAutoCorrect" : ""),
                (mIsPasswordField ? " password" : ""),
                (mShouldShowSuggestions ? " shouldShowSuggestions" : ""),
                (mApplicationSpecifiedCompletionOn ? " appSpecified" : ""),
                (mShouldInsertSpacesAutomatically ? " insertSpaces" : ""),
                mTargetApplicationPackageName);
    }
}
