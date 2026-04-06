package rkr.simplekeyboard.inputmethod.keyboard;
public class KeyDetector {
    private final int mKeyHysteresisDistanceSquared;
    private final int mKeyHysteresisDistanceForSlidingModifierSquared;
    private Keyboard mKeyboard;
    private int mCorrectionX;
    private int mCorrectionY;
    public KeyDetector() {
        this(0.0f , 0.0f );
    }
        public KeyDetector(final float keyHysteresisDistance,
            final float keyHysteresisDistanceForSlidingModifier) {
        mKeyHysteresisDistanceSquared = (int)(keyHysteresisDistance * keyHysteresisDistance);
        mKeyHysteresisDistanceForSlidingModifierSquared = (int)(
                keyHysteresisDistanceForSlidingModifier * keyHysteresisDistanceForSlidingModifier);
    }
    public void setKeyboard(final Keyboard keyboard, final float correctionX,
            final float correctionY) {
        if (keyboard == null) {
            throw new NullPointerException();
        }
        mCorrectionX = (int)correctionX;
        mCorrectionY = (int)correctionY;
        mKeyboard = keyboard;
    }
    public int getKeyHysteresisDistanceSquared(final boolean isSlidingFromModifier) {
        return isSlidingFromModifier
                ? mKeyHysteresisDistanceForSlidingModifierSquared : mKeyHysteresisDistanceSquared;
    }
    public int getTouchX(final int x) {
        return x + mCorrectionX;
    }
    public int getTouchY(final int y) {
        return y + mCorrectionY;
    }
    public Keyboard getKeyboard() {
        return mKeyboard;
    }
    public boolean alwaysAllowsKeySelectionByDraggingFinger() {
        return false;
    }
        public Key detectHitKey(final int x, final int y) {
        if (mKeyboard == null) {
            return null;
        }
        final int touchX = getTouchX(x);
        final int touchY = getTouchY(y);
        for (final Key key: mKeyboard.getNearestKeys(touchX, touchY)) {
            if (key.isOnKey(touchX, touchY)) {
                return key;
            }
        }
        return null;
    }
}
