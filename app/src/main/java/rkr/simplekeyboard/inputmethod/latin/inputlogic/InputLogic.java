package rkr.simplekeyboard.inputmethod.latin.inputlogic;
import android.os.SystemClock;
import android.text.TextUtils;
import android.view.KeyCharacterMap;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import rkr.simplekeyboard.inputmethod.event.Event;
import rkr.simplekeyboard.inputmethod.event.InputTransaction;
import rkr.simplekeyboard.inputmethod.latin.LatinIME;
import rkr.simplekeyboard.inputmethod.latin.RichInputConnection;
import rkr.simplekeyboard.inputmethod.latin.common.Constants;
import rkr.simplekeyboard.inputmethod.latin.common.StringUtils;
import rkr.simplekeyboard.inputmethod.latin.settings.SettingsValues;
import rkr.simplekeyboard.inputmethod.latin.utils.InputTypeUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.RecapitalizeStatus;
import rkr.simplekeyboard.inputmethod.latin.utils.SubtypeLocaleUtils;
public final class InputLogic {
    final LatinIME mLatinIME;
    public final RichInputConnection mConnection;
    private final RecapitalizeStatus mRecapitalizeStatus = new RecapitalizeStatus();
        public InputLogic(final LatinIME latinIME) {
        mLatinIME = latinIME;
        mConnection = new RichInputConnection(latinIME);
    }
        public void startInput() {
        mRecapitalizeStatus.disable();
    }
    public void clearCaches() {
        mConnection.clearCaches();
    }
        public void onSubtypeChanged() {
        startInput();
    }
        public InputTransaction onTextInput(final SettingsValues settingsValues, final Event event) {
        final String rawText = event.getTextToCommit().toString();
        final InputTransaction inputTransaction = new InputTransaction(settingsValues);
        final String text = performSpecificTldProcessingOnTextInput(rawText);
        mConnection.commitText(text, 1);
        inputTransaction.requireShiftUpdate(InputTransaction.SHIFT_UPDATE_NOW);
        return inputTransaction;
    }
        public void onUpdateSelection(final int newSelStart, final int newSelEnd) {
        mConnection.updateSelection(newSelStart, newSelEnd);
    }
    public void reloadTextCache() {
        mConnection.reloadTextCache();
        mRecapitalizeStatus.enable();
        mRecapitalizeStatus.stop();
    }
        public InputTransaction onCodeInput(final SettingsValues settingsValues, final Event event) {
        final InputTransaction inputTransaction = new InputTransaction(settingsValues);
        Event currentEvent = event;
        while (null != currentEvent) {
            if (currentEvent.isConsumed()) {
                handleConsumedEvent(currentEvent);
            } else if (currentEvent.isFunctionalKeyEvent()) {
                handleFunctionalEvent(currentEvent, inputTransaction);
            } else {
                handleNonFunctionalEvent(currentEvent, inputTransaction);
            }
            currentEvent = currentEvent.mNextEvent;
        }
        return inputTransaction;
    }
        private void handleConsumedEvent(final Event event) {
        final CharSequence textToCommit = event.getTextToCommit();
        if (!TextUtils.isEmpty(textToCommit)) {
            mConnection.commitText(textToCommit, 1);
        }
    }
        private void handleFunctionalEvent(final Event event, final InputTransaction inputTransaction) {
        switch (event.mKeyCode) {
            case Constants.CODE_DELETE:
                handleBackspaceEvent(event, inputTransaction);
                break;
            case Constants.CODE_SHIFT:
                performRecapitalization();
                inputTransaction.requireShiftUpdate(InputTransaction.SHIFT_UPDATE_NOW);
                break;
            case Constants.CODE_CAPSLOCK:
                break;
            case Constants.CODE_SYMBOL_SHIFT:
                break;
            case Constants.CODE_SWITCH_ALPHA_SYMBOL:
                break;
            case Constants.CODE_SETTINGS:
                onSettingsKeyPressed();
                break;
            case Constants.CODE_PASTE:
                mConnection.pasteClipboard();
                break;
            case Constants.CODE_ACTION_NEXT:
                performEditorAction(EditorInfo.IME_ACTION_NEXT);
                break;
            case Constants.CODE_ACTION_PREVIOUS:
                performEditorAction(EditorInfo.IME_ACTION_PREVIOUS);
                break;
            case Constants.CODE_LANGUAGE_SWITCH:
                handleLanguageSwitchKey();
                break;
            case Constants.CODE_SHIFT_ENTER:
                sendDownUpKeyEvent(KeyEvent.KEYCODE_ENTER, KeyEvent.META_SHIFT_ON);
                break;
            default:
                throw new RuntimeException("Unknown key code : " + event.mKeyCode);
        }
    }
        private void handleNonFunctionalEvent(final Event event,
            final InputTransaction inputTransaction) {
        switch (event.mCodePoint) {
            case Constants.CODE_ENTER:
                final EditorInfo editorInfo = getCurrentInputEditorInfo();
                final int imeOptionsActionId =
                        InputTypeUtils.getImeOptionsActionIdFromEditorInfo(editorInfo);
                if (InputTypeUtils.IME_ACTION_CUSTOM_LABEL == imeOptionsActionId) {
                    performEditorAction(editorInfo.actionId);
                } else if (EditorInfo.IME_ACTION_NONE != imeOptionsActionId) {
                    performEditorAction(imeOptionsActionId);
                } else {
                    handleNonSpecialCharacterEvent(event, inputTransaction);
                }
                break;
            default:
                handleNonSpecialCharacterEvent(event, inputTransaction);
                break;
        }
    }
        private void handleNonSpecialCharacterEvent(final Event event,
            final InputTransaction inputTransaction) {
        final int codePoint = event.mCodePoint;
        if (inputTransaction.mSettingsValues.isWordSeparator(codePoint)
                || Character.getType(codePoint) == Character.OTHER_SYMBOL) {
            handleSeparatorEvent(event, inputTransaction);
        } else {
            handleNonSeparatorEvent(event);
        }
    }
        private void handleNonSeparatorEvent(final Event event) {
        sendKeyCodePoint(event.mCodePoint);
    }
        private void handleSeparatorEvent(final Event event, final InputTransaction inputTransaction) {
        sendKeyCodePoint(event.mCodePoint);
        inputTransaction.requireShiftUpdate(InputTransaction.SHIFT_UPDATE_NOW);
    }
        private void handleBackspaceEvent(final Event event, final InputTransaction inputTransaction) {
        final int shiftUpdateKind =
                event.isKeyRepeat() && mConnection.getExpectedSelectionStart() > 0
                ? InputTransaction.SHIFT_UPDATE_LATER : InputTransaction.SHIFT_UPDATE_NOW;
        inputTransaction.requireShiftUpdate(shiftUpdateKind);
        if (mConnection.hasSelection()) {
            mConnection.deleteSelectedText();
        } else {
            final int codePointBeforeCursor = mConnection.getCodePointBeforeCursor();
            if (codePointBeforeCursor == Constants.NOT_A_CODE) {
                sendDownUpKeyEvent(KeyEvent.KEYCODE_DEL);
            } else {
                final int numChars = Character.isSupplementaryCodePoint(codePointBeforeCursor) ? 2 : 1;
                mConnection.deleteTextBeforeCursor(numChars);
            }
        }
    }
        private void handleLanguageSwitchKey() {
        mLatinIME.switchToNextSubtype();
    }
        private void performRecapitalization() {
        if (!mConnection.hasSelection() || !mRecapitalizeStatus.mIsEnabled()) {
            return;
        }
        final int selectionStart = mConnection.getExpectedSelectionStart();
        final int selectionEnd = mConnection.getExpectedSelectionEnd();
        final int numCharsSelected = selectionEnd - selectionStart;
        if (numCharsSelected > Constants.MAX_CHARACTERS_FOR_RECAPITALIZATION) {
            return;
        }
        if (!mRecapitalizeStatus.isStarted()
                || !mRecapitalizeStatus.isSetAt(selectionStart, selectionEnd)) {
            final CharSequence selectedText = mConnection.getSelectedText();
            if (TextUtils.isEmpty(selectedText)) return;
            mRecapitalizeStatus.start(selectionStart, selectionEnd, selectedText.toString(), mLatinIME.getCurrentLayoutLocale());
            mRecapitalizeStatus.trim();
        }
        mConnection.beginBatchEdit();
        mConnection.setSelection(selectionStart, selectionStart);
        mRecapitalizeStatus.rotate();
        mConnection.replaceText(selectionStart, selectionEnd, mRecapitalizeStatus.getRecapitalizedString());
        mConnection.setSelection(mRecapitalizeStatus.getNewCursorStart(), mRecapitalizeStatus.getNewCursorEnd());
        mConnection.endBatchEdit();
    }
        public int getCurrentAutoCapsState(final SettingsValues settingsValues,
                                       final String layoutSetName) {
        if (!settingsValues.mAutoCap || !layoutUsesAutoCaps(layoutSetName)) {
            return Constants.TextUtils.CAP_MODE_OFF;
        }
        final EditorInfo ei = getCurrentInputEditorInfo();
        if (ei == null) return Constants.TextUtils.CAP_MODE_OFF;
        final int inputType = ei.inputType;
        return mConnection.getCursorCapsMode(inputType, settingsValues.mSpacingAndPunctuations);
    }
    private boolean layoutUsesAutoCaps(final String layoutSetName) {
        switch (layoutSetName) {
            case SubtypeLocaleUtils.LAYOUT_ARABIC:
            case SubtypeLocaleUtils.LAYOUT_BENGALI:
            case SubtypeLocaleUtils.LAYOUT_BENGALI_AKKHOR:
            case SubtypeLocaleUtils.LAYOUT_BENGALI_UNIJOY:
            case SubtypeLocaleUtils.LAYOUT_FARSI:
            case SubtypeLocaleUtils.LAYOUT_GEORGIAN:
            case SubtypeLocaleUtils.LAYOUT_HEBREW:
            case SubtypeLocaleUtils.LAYOUT_HINDI:
            case SubtypeLocaleUtils.LAYOUT_HINDI_COMPACT:
            case SubtypeLocaleUtils.LAYOUT_KANNADA:
            case SubtypeLocaleUtils.LAYOUT_KHMER:
            case SubtypeLocaleUtils.LAYOUT_LAO:
            case SubtypeLocaleUtils.LAYOUT_MALAYALAM:
            case SubtypeLocaleUtils.LAYOUT_MARATHI:
            case SubtypeLocaleUtils.LAYOUT_NEPALI_ROMANIZED:
            case SubtypeLocaleUtils.LAYOUT_NEPALI_TRADITIONAL:
            case SubtypeLocaleUtils.LAYOUT_TAMIL:
            case SubtypeLocaleUtils.LAYOUT_TELUGU:
            case SubtypeLocaleUtils.LAYOUT_THAI:
            case SubtypeLocaleUtils.LAYOUT_URDU:
                return false;
            default:
                return true;
        }
    }
    public int getCurrentRecapitalizeState() {
        if (!mRecapitalizeStatus.isStarted()
                || !mRecapitalizeStatus.isSetAt(mConnection.getExpectedSelectionStart(),
                        mConnection.getExpectedSelectionEnd())) {
            return RecapitalizeStatus.NOT_A_RECAPITALIZE_MODE;
        }
        return mRecapitalizeStatus.getCurrentMode();
    }
        private EditorInfo getCurrentInputEditorInfo() {
        return mLatinIME.getCurrentInputEditorInfo();
    }
        private void performEditorAction(final int actionId) {
        mConnection.performEditorAction(actionId);
    }
        private String performSpecificTldProcessingOnTextInput(final String text) {
        if (text.length() <= 1 || text.charAt(0) != Constants.CODE_PERIOD
                || !Character.isLetter(text.charAt(1))) {
            return text;
        }
        final int codePointBeforeCursor = mConnection.getCodePointBeforeCursor();
        if (Constants.CODE_PERIOD == codePointBeforeCursor) {
            return text.substring(1);
        }
        return text;
    }
        private void onSettingsKeyPressed() {
        mLatinIME.launchSettings();
    }
        public void sendDownUpKeyEvent(final int keyCode) {
        sendDownUpKeyEvent(keyCode, 0);
    }
    public void sendDownUpKeyEvent(final int keyCode, final int metaState) {
        final long eventTime = SystemClock.uptimeMillis();
        mConnection.sendKeyEvent(new KeyEvent(eventTime, eventTime,
                KeyEvent.ACTION_DOWN, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
        mConnection.sendKeyEvent(new KeyEvent(SystemClock.uptimeMillis(), eventTime,
                KeyEvent.ACTION_UP, keyCode, 0, metaState, KeyCharacterMap.VIRTUAL_KEYBOARD, 0,
                KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
    }
    private void sendKeyCodePoint(final int codePoint) {
        if (codePoint >= '0' && codePoint <= '9') {
            sendDownUpKeyEvent(codePoint - '0' + KeyEvent.KEYCODE_0);
            return;
        }
        mConnection.commitText(StringUtils.newSingleCodePointString(codePoint), 1);
    }
}
