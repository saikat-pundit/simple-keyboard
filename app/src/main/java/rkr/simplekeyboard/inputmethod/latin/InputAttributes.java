package rkr.simplekeyboard.inputmethod.latin;
import android.text.InputType;
import android.util.Log;
import android.view.inputmethod.EditorInfo;
import rkr.simplekeyboard.inputmethod.latin.utils.InputTypeUtils;
public final class InputAttributes {
    private final String TAG = InputAttributes.class.getSimpleName();
    final public String mTargetApplicationPackageName;
    final public boolean mInputTypeNoAutoCorrect;
    final public boolean mIsPasswordField;
    final public boolean mShouldShowSuggestions;
    final public boolean mApplicationSpecifiedCompletionOn;
    final public boolean mShouldInsertSpacesAutomatically;
        final private int mInputType;
    public InputAttributes(final EditorInfo editorInfo, final boolean isFullscreenMode) {
        mTargetApplicationPackageName = null != editorInfo ? editorInfo.packageName : null;
        final int inputType = null != editorInfo ? editorInfo.inputType : 0;
        final int inputClass = inputType & InputType.TYPE_MASK_CLASS;
        mInputType = inputType;
        mIsPasswordField = InputTypeUtils.isPasswordInputType(inputType)
                || InputTypeUtils.isVisiblePasswordInputType(inputType);
        if (inputClass != InputType.TYPE_CLASS_TEXT) {
            if (null == editorInfo) {
                Log.w(TAG, "No editor info for this field. Bug?");
            } else if (InputType.TYPE_NULL == inputType) {
                Log.i(TAG, "InputType.TYPE_NULL is specified");
            } else if (inputClass == 0) {
                Log.w(TAG, String.format("Unexpected input class: inputType=0x%08x"
                        + " imeOptions=0x%08x", inputType, editorInfo.imeOptions));
            }
            mShouldShowSuggestions = false;
            mInputTypeNoAutoCorrect = false;
            mApplicationSpecifiedCompletionOn = false;
            mShouldInsertSpacesAutomatically = false;
            return;
        }
        final int variation = inputType & InputType.TYPE_MASK_VARIATION;
        final boolean flagNoSuggestions =
                0 != (inputType & InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        final boolean flagMultiLine =
                0 != (inputType & InputType.TYPE_TEXT_FLAG_MULTI_LINE);
        final boolean flagAutoCorrect =
                0 != (inputType & InputType.TYPE_TEXT_FLAG_AUTO_CORRECT);
        final boolean flagAutoComplete =
                0 != (inputType & InputType.TYPE_TEXT_FLAG_AUTO_COMPLETE);
        final boolean shouldSuppressSuggestions = mIsPasswordField
                || InputTypeUtils.isEmailVariation(variation)
                || InputType.TYPE_TEXT_VARIATION_URI == variation
                || InputType.TYPE_TEXT_VARIATION_FILTER == variation
                || flagNoSuggestions
                || flagAutoComplete;
        mShouldShowSuggestions = !shouldSuppressSuggestions;
        mShouldInsertSpacesAutomatically = InputTypeUtils.isAutoSpaceFriendlyType(inputType);
        mInputTypeNoAutoCorrect =
                (variation == InputType.TYPE_TEXT_VARIATION_WEB_EDIT_TEXT && !flagAutoCorrect)
                || flagNoSuggestions
                || (!flagAutoCorrect && !flagMultiLine);
        mApplicationSpecifiedCompletionOn = flagAutoComplete && isFullscreenMode;
    }
    public boolean isSameInputType(final EditorInfo editorInfo) {
        return editorInfo.inputType == mInputType;
    }
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
