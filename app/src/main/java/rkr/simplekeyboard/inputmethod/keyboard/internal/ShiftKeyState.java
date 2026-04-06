package rkr.simplekeyboard.inputmethod.keyboard.internal;

import android.util.Log;

/* package */ final class ShiftKeyState extends ModifierKeyState {
    private static final int PRESSING_ON_SHIFTED = 3; // both temporary shifted & shift locked
    private static final int IGNORING = 4;

    public ShiftKeyState(String name) {
        super(name);
    }

    @Override
    public void onOtherKeyPressed() {
        int oldState = mState;
        if (oldState == PRESSING) {
            mState = CHORDING;
        } else if (oldState == PRESSING_ON_SHIFTED) {
            mState = IGNORING;
        }
        if (DEBUG)
            Log.d(TAG, mName + ".onOtherKeyPressed: " + toString(oldState) + " > " + this);
    }

    public void onPressOnShifted() {
        mState = PRESSING_ON_SHIFTED;
    }

    public boolean isPressingOnShifted() {
        return mState == PRESSING_ON_SHIFTED;
    }

    public boolean isIgnoring() {
        return mState == IGNORING;
    }

    @Override
    public String toString() {
        return toString(mState);
    }

    @Override
    protected String toString(int state) {
        switch (state) {
        case PRESSING_ON_SHIFTED: return "PRESSING_ON_SHIFTED";
        case IGNORING: return "IGNORING";
        default: return super.toString(state);
        }
    }
}
