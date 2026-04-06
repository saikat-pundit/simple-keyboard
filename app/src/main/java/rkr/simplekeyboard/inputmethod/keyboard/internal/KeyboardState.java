package rkr.simplekeyboard.inputmethod.keyboard.internal;
import android.text.TextUtils;
import android.util.Log;
import rkr.simplekeyboard.inputmethod.event.Event;
import rkr.simplekeyboard.inputmethod.latin.common.Constants;
import rkr.simplekeyboard.inputmethod.latin.utils.CapsModeUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.RecapitalizeStatus;
public final class KeyboardState {
    private static final String TAG = KeyboardState.class.getSimpleName();
    private static final boolean DEBUG_EVENT = false;
    private static final boolean DEBUG_INTERNAL_ACTION = false;
    public interface SwitchActions {
        boolean DEBUG_ACTION = false;
        void setAlphabetKeyboard();
        void setAlphabetManualShiftedKeyboard();
        void setAlphabetAutomaticShiftedKeyboard();
        void setAlphabetShiftLockedKeyboard();
        void setSymbolsKeyboard();
        void setSymbolsShiftedKeyboard();
                void requestUpdatingShiftState(final int autoCapsFlags, final int recapitalizeMode);
        boolean DEBUG_TIMER_ACTION = false;
        void startDoubleTapShiftKeyTimer();
        boolean isInDoubleTapShiftKeyTimeout();
        void cancelDoubleTapShiftKeyTimer();
    }
    private final SwitchActions mSwitchActions;
    private ShiftKeyState mShiftKeyState = new ShiftKeyState("Shift");
    private ModifierKeyState mSymbolKeyState = new ModifierKeyState("Symbol");
    private static final int SWITCH_STATE_ALPHA = 0;
    private static final int SWITCH_STATE_SYMBOL_BEGIN = 1;
    private static final int SWITCH_STATE_SYMBOL = 2;
    private static final int SWITCH_STATE_MOMENTARY_ALPHA_AND_SYMBOL = 3;
    private static final int SWITCH_STATE_MOMENTARY_SYMBOL_AND_MORE = 4;
    private int mSwitchState = SWITCH_STATE_ALPHA;
    private boolean mIsAlphabetMode;
    private AlphabetShiftState mAlphabetShiftState = new AlphabetShiftState();
    private boolean mIsSymbolShifted;
    private boolean mPrevMainKeyboardWasShiftLocked;
    private boolean mPrevSymbolsKeyboardWasShifted;
    private int mRecapitalizeMode;
    private boolean mIsInAlphabetUnshiftedFromShifted;
    private boolean mIsInDoubleTapShiftKey;
    public KeyboardState(final SwitchActions switchActions) {
        mSwitchActions = switchActions;
        mRecapitalizeMode = RecapitalizeStatus.NOT_A_RECAPITALIZE_MODE;
    }
    public void onLoadKeyboard(final int autoCapsFlags, final int recapitalizeMode) {
        if (DEBUG_EVENT) {
            Log.d(TAG, "onLoadKeyboard: " + stateToString(autoCapsFlags, recapitalizeMode));
        }
        mAlphabetShiftState.setShiftLocked(false);
        mPrevMainKeyboardWasShiftLocked = false;
        mPrevSymbolsKeyboardWasShifted = false;
        mShiftKeyState.onRelease();
        mSymbolKeyState.onRelease();
        setAlphabetKeyboard(autoCapsFlags, recapitalizeMode);
    }
    private static final int UNSHIFT = 0;
    private static final int MANUAL_SHIFT = 1;
    private static final int AUTOMATIC_SHIFT = 2;
    private static final int SHIFT_LOCK_SHIFTED = 3;
    private void setShifted(final int shiftMode) {
        if (DEBUG_INTERNAL_ACTION) {
            Log.d(TAG, "setShifted: shiftMode=" + shiftModeToString(shiftMode) + " " + this);
        }
        if (!mIsAlphabetMode) return;
        final int prevShiftMode;
        if (mAlphabetShiftState.isAutomaticShifted()) {
            prevShiftMode = AUTOMATIC_SHIFT;
        } else if (mAlphabetShiftState.isManualShifted()) {
            prevShiftMode = MANUAL_SHIFT;
        } else {
            prevShiftMode = UNSHIFT;
        }
        switch (shiftMode) {
        case AUTOMATIC_SHIFT:
            mAlphabetShiftState.setAutomaticShifted();
            if (shiftMode != prevShiftMode) {
                mSwitchActions.setAlphabetAutomaticShiftedKeyboard();
            }
            break;
        case MANUAL_SHIFT:
            mAlphabetShiftState.setShifted(true);
            if (shiftMode != prevShiftMode) {
                mSwitchActions.setAlphabetManualShiftedKeyboard();
            }
            break;
        case UNSHIFT:
            mAlphabetShiftState.setShifted(false);
            if (shiftMode != prevShiftMode) {
                mSwitchActions.setAlphabetKeyboard();
            }
            break;
        case SHIFT_LOCK_SHIFTED:
            mAlphabetShiftState.setShifted(true);
            break;
        }
    }
    private void setShiftLocked(final boolean shiftLocked) {
        if (DEBUG_INTERNAL_ACTION) {
            Log.d(TAG, "setShiftLocked: shiftLocked=" + shiftLocked + " " + this);
        }
        if (!mIsAlphabetMode) return;
        if (shiftLocked && (!mAlphabetShiftState.isShiftLocked()
                || mAlphabetShiftState.isShiftLockShifted())) {
            mSwitchActions.setAlphabetShiftLockedKeyboard();
        }
        if (!shiftLocked && mAlphabetShiftState.isShiftLocked()) {
            mSwitchActions.setAlphabetKeyboard();
        }
        mAlphabetShiftState.setShiftLocked(shiftLocked);
    }
    private void toggleAlphabetAndSymbols(final int autoCapsFlags, final int recapitalizeMode) {
        if (DEBUG_INTERNAL_ACTION) {
            Log.d(TAG, "toggleAlphabetAndSymbols: "
                    + stateToString(autoCapsFlags, recapitalizeMode));
        }
        if (mIsAlphabetMode) {
            mPrevMainKeyboardWasShiftLocked = mAlphabetShiftState.isShiftLocked();
            if (mPrevSymbolsKeyboardWasShifted) {
                setSymbolsShiftedKeyboard();
            } else {
                setSymbolsKeyboard();
            }
            mPrevSymbolsKeyboardWasShifted = false;
        } else {
            mPrevSymbolsKeyboardWasShifted = mIsSymbolShifted;
            setAlphabetKeyboard(autoCapsFlags, recapitalizeMode);
            if (mPrevMainKeyboardWasShiftLocked) {
                setShiftLocked(true);
            }
            mPrevMainKeyboardWasShiftLocked = false;
        }
    }
    private void resetKeyboardStateToAlphabet(final int autoCapsFlags, final int recapitalizeMode) {
        if (DEBUG_INTERNAL_ACTION) {
            Log.d(TAG, "resetKeyboardStateToAlphabet: "
                    + stateToString(autoCapsFlags, recapitalizeMode));
        }
        if (mIsAlphabetMode) return;
        mPrevSymbolsKeyboardWasShifted = mIsSymbolShifted;
        setAlphabetKeyboard(autoCapsFlags, recapitalizeMode);
        if (mPrevMainKeyboardWasShiftLocked) {
            setShiftLocked(true);
        }
        mPrevMainKeyboardWasShiftLocked = false;
    }
    private void toggleShiftInSymbols() {
        if (mIsSymbolShifted) {
            setSymbolsKeyboard();
        } else {
            setSymbolsShiftedKeyboard();
        }
    }
    private void setAlphabetKeyboard(final int autoCapsFlags, final int recapitalizeMode) {
        if (DEBUG_INTERNAL_ACTION) {
            Log.d(TAG, "setAlphabetKeyboard: " + stateToString(autoCapsFlags, recapitalizeMode));
        }
        mSwitchActions.setAlphabetKeyboard();
        mIsAlphabetMode = true;
        mIsSymbolShifted = false;
        mRecapitalizeMode = RecapitalizeStatus.NOT_A_RECAPITALIZE_MODE;
        mSwitchState = SWITCH_STATE_ALPHA;
        mSwitchActions.requestUpdatingShiftState(autoCapsFlags, recapitalizeMode);
    }
    private void setSymbolsKeyboard() {
        if (DEBUG_INTERNAL_ACTION) {
            Log.d(TAG, "setSymbolsKeyboard");
        }
        mSwitchActions.setSymbolsKeyboard();
        mIsAlphabetMode = false;
        mIsSymbolShifted = false;
        mRecapitalizeMode = RecapitalizeStatus.NOT_A_RECAPITALIZE_MODE;
        mAlphabetShiftState.setShiftLocked(false);
        mSwitchState = SWITCH_STATE_SYMBOL_BEGIN;
    }
    private void setSymbolsShiftedKeyboard() {
        if (DEBUG_INTERNAL_ACTION) {
            Log.d(TAG, "setSymbolsShiftedKeyboard");
        }
        mSwitchActions.setSymbolsShiftedKeyboard();
        mIsAlphabetMode = false;
        mIsSymbolShifted = true;
        mRecapitalizeMode = RecapitalizeStatus.NOT_A_RECAPITALIZE_MODE;
        mAlphabetShiftState.setShiftLocked(false);
        mSwitchState = SWITCH_STATE_SYMBOL_BEGIN;
    }
    public void onPressKey(final int code, final boolean isSinglePointer, final int autoCapsFlags,
            final int recapitalizeMode) {
        if (DEBUG_EVENT) {
            Log.d(TAG, "onPressKey: code=" + Constants.printableCode(code)
                    + " single=" + isSinglePointer
                    + " " + stateToString(autoCapsFlags, recapitalizeMode));
        }
        if (code != Constants.CODE_SHIFT) {
            mSwitchActions.cancelDoubleTapShiftKeyTimer();
        }
        if (code == Constants.CODE_SHIFT) {
            onPressShift();
        } else if (code == Constants.CODE_CAPSLOCK) {
        } else if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL) {
            onPressSymbol(autoCapsFlags, recapitalizeMode);
        } else {
            mShiftKeyState.onOtherKeyPressed();
            mSymbolKeyState.onOtherKeyPressed();
            if (!isSinglePointer && mIsAlphabetMode
                    && autoCapsFlags != TextUtils.CAP_MODE_CHARACTERS) {
                final boolean needsToResetAutoCaps =
                        (mAlphabetShiftState.isAutomaticShifted() && !mShiftKeyState.isChording())
                        || (mAlphabetShiftState.isManualShifted() && mShiftKeyState.isReleasing());
                if (needsToResetAutoCaps) {
                    mSwitchActions.setAlphabetKeyboard();
                }
            }
        }
    }
    public void onReleaseKey(final int code, final boolean withSliding, final int autoCapsFlags,
            final int recapitalizeMode) {
        if (DEBUG_EVENT) {
            Log.d(TAG, "onReleaseKey: code=" + Constants.printableCode(code)
                    + " sliding=" + withSliding
                    + " " + stateToString(autoCapsFlags, recapitalizeMode));
        }
        if (code == Constants.CODE_SHIFT) {
            onReleaseShift(withSliding, autoCapsFlags, recapitalizeMode);
        } else if (code == Constants.CODE_CAPSLOCK) {
            setShiftLocked(!mAlphabetShiftState.isShiftLocked());
        } else if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL) {
            onReleaseSymbol(withSliding, autoCapsFlags, recapitalizeMode);
        }
    }
    private void onPressSymbol(final int autoCapsFlags,
            final int recapitalizeMode) {
        toggleAlphabetAndSymbols(autoCapsFlags, recapitalizeMode);
        mSymbolKeyState.onPress();
        mSwitchState = SWITCH_STATE_MOMENTARY_ALPHA_AND_SYMBOL;
    }
    private void onReleaseSymbol(final boolean withSliding, final int autoCapsFlags,
            final int recapitalizeMode) {
        if (mSymbolKeyState.isChording()) {
            toggleAlphabetAndSymbols(autoCapsFlags, recapitalizeMode);
        } else if (!withSliding) {
            mPrevSymbolsKeyboardWasShifted = false;
        }
        mSymbolKeyState.onRelease();
    }
    public void onUpdateShiftState(final int autoCapsFlags, final int recapitalizeMode) {
        if (DEBUG_EVENT) {
            Log.d(TAG, "onUpdateShiftState: " + stateToString(autoCapsFlags, recapitalizeMode));
        }
        mRecapitalizeMode = recapitalizeMode;
        updateAlphabetShiftState(autoCapsFlags, recapitalizeMode);
    }
    public void onResetKeyboardStateToAlphabet(final int autoCapsFlags,
            final int recapitalizeMode) {
        if (DEBUG_EVENT) {
            Log.d(TAG, "onResetKeyboardStateToAlphabet: "
                    + stateToString(autoCapsFlags, recapitalizeMode));
        }
        resetKeyboardStateToAlphabet(autoCapsFlags, recapitalizeMode);
    }
    private void updateShiftStateForRecapitalize(final int recapitalizeMode) {
        switch (recapitalizeMode) {
        case RecapitalizeStatus.CAPS_MODE_ALL_UPPER:
            setShifted(SHIFT_LOCK_SHIFTED);
            break;
        case RecapitalizeStatus.CAPS_MODE_FIRST_WORD_UPPER:
            setShifted(AUTOMATIC_SHIFT);
            break;
        case RecapitalizeStatus.CAPS_MODE_ALL_LOWER:
        case RecapitalizeStatus.CAPS_MODE_ORIGINAL_MIXED_CASE:
        default:
            setShifted(UNSHIFT);
        }
    }
    private void updateAlphabetShiftState(final int autoCapsFlags, final int recapitalizeMode) {
        if (!mIsAlphabetMode) return;
        if (RecapitalizeStatus.NOT_A_RECAPITALIZE_MODE != recapitalizeMode) {
            updateShiftStateForRecapitalize(recapitalizeMode);
            return;
        }
        if (!mShiftKeyState.isReleasing()) {
            return;
        }
        if (!mAlphabetShiftState.isShiftLocked() && !mShiftKeyState.isIgnoring()) {
            if (mShiftKeyState.isReleasing() && autoCapsFlags != Constants.TextUtils.CAP_MODE_OFF) {
                setShifted(AUTOMATIC_SHIFT);
            } else {
                setShifted(mShiftKeyState.isChording() ? MANUAL_SHIFT : UNSHIFT);
            }
        }
    }
    private void onPressShift() {
        if (RecapitalizeStatus.NOT_A_RECAPITALIZE_MODE != mRecapitalizeMode) {
            return;
        }
        if (mIsAlphabetMode) {
            mIsInDoubleTapShiftKey = mSwitchActions.isInDoubleTapShiftKeyTimeout();
            if (!mIsInDoubleTapShiftKey) {
                mSwitchActions.startDoubleTapShiftKeyTimer();
            }
            if (mIsInDoubleTapShiftKey) {
                if (mAlphabetShiftState.isManualShifted() || mIsInAlphabetUnshiftedFromShifted) {
                    setShiftLocked(true);
                } else {
                }
            } else {
                if (mAlphabetShiftState.isShiftLocked()) {
                    setShifted(SHIFT_LOCK_SHIFTED);
                    mShiftKeyState.onPress();
                } else if (mAlphabetShiftState.isAutomaticShifted()) {
                    mShiftKeyState.onPress();
                } else if (mAlphabetShiftState.isShiftedOrShiftLocked()) {
                    mShiftKeyState.onPressOnShifted();
                } else {
                    setShifted(MANUAL_SHIFT);
                    mShiftKeyState.onPress();
                }
            }
        } else {
            toggleShiftInSymbols();
            mSwitchState = SWITCH_STATE_MOMENTARY_SYMBOL_AND_MORE;
            mShiftKeyState.onPress();
        }
    }
    private void onReleaseShift(final boolean withSliding, final int autoCapsFlags,
            final int recapitalizeMode) {
        if (RecapitalizeStatus.NOT_A_RECAPITALIZE_MODE != mRecapitalizeMode) {
            updateShiftStateForRecapitalize(mRecapitalizeMode);
        } else if (mIsAlphabetMode) {
            final boolean isShiftLocked = mAlphabetShiftState.isShiftLocked();
            mIsInAlphabetUnshiftedFromShifted = false;
            if (mIsInDoubleTapShiftKey) {
                mIsInDoubleTapShiftKey = false;
            } else if (mShiftKeyState.isChording()) {
                if (mAlphabetShiftState.isShiftLockShifted()) {
                    setShiftLocked(true);
                } else {
                    setShifted(UNSHIFT);
                }
                mShiftKeyState.onRelease();
                mSwitchActions.requestUpdatingShiftState(autoCapsFlags, recapitalizeMode);
                return;
            } else if (isShiftLocked && !mAlphabetShiftState.isShiftLockShifted()
                    && (mShiftKeyState.isPressing() || mShiftKeyState.isPressingOnShifted())
                    && !withSliding) {
            } else if (isShiftLocked && !mShiftKeyState.isIgnoring() && !withSliding) {
                setShiftLocked(false);
            } else if (mAlphabetShiftState.isShiftedOrShiftLocked()
                    && mShiftKeyState.isPressingOnShifted() && !withSliding) {
                setShifted(UNSHIFT);
                mIsInAlphabetUnshiftedFromShifted = true;
            } else if (mAlphabetShiftState.isAutomaticShifted() && mShiftKeyState.isPressing()
                    && !withSliding) {
                setShifted(UNSHIFT);
                mIsInAlphabetUnshiftedFromShifted = true;
            }
        } else {
            if (mShiftKeyState.isChording()) {
                toggleShiftInSymbols();
            }
        }
        mShiftKeyState.onRelease();
    }
    public void onFinishSlidingInput(final int autoCapsFlags, final int recapitalizeMode) {
        if (DEBUG_EVENT) {
            Log.d(TAG, "onFinishSlidingInput: " + stateToString(autoCapsFlags, recapitalizeMode));
        }
        switch (mSwitchState) {
        case SWITCH_STATE_MOMENTARY_ALPHA_AND_SYMBOL:
            toggleAlphabetAndSymbols(autoCapsFlags, recapitalizeMode);
            break;
        case SWITCH_STATE_MOMENTARY_SYMBOL_AND_MORE:
            toggleShiftInSymbols();
            break;
        }
    }
    private static boolean isSpaceOrEnter(final int c) {
        return c == Constants.CODE_SPACE || c == Constants.CODE_ENTER;
    }
    public void onEvent(final Event event, final int autoCapsFlags, final int recapitalizeMode) {
        final int code = event.isFunctionalKeyEvent() ? event.mKeyCode : event.mCodePoint;
        if (DEBUG_EVENT) {
            Log.d(TAG, "onEvent: code=" + Constants.printableCode(code)
                    + " " + stateToString(autoCapsFlags, recapitalizeMode));
        }
        switch (mSwitchState) {
        case SWITCH_STATE_MOMENTARY_ALPHA_AND_SYMBOL:
            if (code == Constants.CODE_SWITCH_ALPHA_SYMBOL) {
                if (mIsAlphabetMode) {
                    mSwitchState = SWITCH_STATE_ALPHA;
                } else {
                    mSwitchState = SWITCH_STATE_SYMBOL_BEGIN;
                }
            }
            break;
        case SWITCH_STATE_MOMENTARY_SYMBOL_AND_MORE:
            if (code == Constants.CODE_SHIFT) {
                mSwitchState = SWITCH_STATE_SYMBOL_BEGIN;
            }
            break;
        case SWITCH_STATE_SYMBOL_BEGIN:
            if (!isSpaceOrEnter(code) && (Constants.isLetterCode(code)
                    || code == Constants.CODE_OUTPUT_TEXT)) {
                mSwitchState = SWITCH_STATE_SYMBOL;
            }
            break;
        case SWITCH_STATE_SYMBOL:
            if (isSpaceOrEnter(code)) {
                toggleAlphabetAndSymbols(autoCapsFlags, recapitalizeMode);
                mPrevSymbolsKeyboardWasShifted = false;
            }
            break;
        }
        if (Constants.isLetterCode(code)) {
            updateAlphabetShiftState(autoCapsFlags, recapitalizeMode);
        }
    }
    static String shiftModeToString(final int shiftMode) {
        switch (shiftMode) {
        case UNSHIFT: return "UNSHIFT";
        case MANUAL_SHIFT: return "MANUAL";
        case AUTOMATIC_SHIFT: return "AUTOMATIC";
        default: return null;
        }
    }
    private static String switchStateToString(final int switchState) {
        switch (switchState) {
        case SWITCH_STATE_ALPHA: return "ALPHA";
        case SWITCH_STATE_SYMBOL_BEGIN: return "SYMBOL-BEGIN";
        case SWITCH_STATE_SYMBOL: return "SYMBOL";
        case SWITCH_STATE_MOMENTARY_ALPHA_AND_SYMBOL: return "MOMENTARY-ALPHA-SYMBOL";
        case SWITCH_STATE_MOMENTARY_SYMBOL_AND_MORE: return "MOMENTARY-SYMBOL-MORE";
        default: return null;
        }
    }
    @Override
    public String toString() {
        return "[keyboard=" + (mIsAlphabetMode ? mAlphabetShiftState.toString()
                : (mIsSymbolShifted ? "SYMBOLS_SHIFTED" : "SYMBOLS"))
                + " shift=" + mShiftKeyState
                + " symbol=" + mSymbolKeyState
                + " switch=" + switchStateToString(mSwitchState) + "]";
    }
    private String stateToString(final int autoCapsFlags, final int recapitalizeMode) {
        return this + " autoCapsFlags=" + CapsModeUtils.flagsToString(autoCapsFlags)
                + " recapitalizeMode=" + RecapitalizeStatus.modeToString(recapitalizeMode);
    }
}
