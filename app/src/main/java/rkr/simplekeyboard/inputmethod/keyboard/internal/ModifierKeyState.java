package rkr.simplekeyboard.inputmethod.keyboard.internal;

import android.util.Log;

/* package */ class ModifierKeyState {
    protected static final String TAG = ModifierKeyState.class.getSimpleName();
    protected static final boolean DEBUG = false;

    protected static final int RELEASING = 0;
    protected static final int PRESSING = 1;
    protected static final int CHORDING = 2;

    protected final String mName;
    protected int mState = RELEASING;

    public ModifierKeyState(String name) {
        mName = name;
    }

    public void onPress() {
        mState = PRESSING;
    }

    public void onRelease() {
        mState = RELEASING;
    }

    public void onOtherKeyPressed() {
        final int oldState = mState;
        if (oldState == PRESSING)
            mState = CHORDING;
        if (DEBUG)
            Log.d(TAG, mName + ".onOtherKeyPressed: " + toString(oldState) + " > " + this);
    }

    public boolean isPressing() {
        return mState == PRESSING;
    }

    public boolean isReleasing() {
        return mState == RELEASING;
    }

    public boolean isChording() {
        return mState == CHORDING;
    }

    @Override
    public String toString() {
        return toString(mState);
    }

    protected String toString(int state) {
        switch (state) {
        case RELEASING: return "RELEASING";
        case PRESSING: return "PRESSING";
        case CHORDING: return "CHORDING";
        default: return "UNKNOWN";
        }
    }
}
