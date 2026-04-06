package rkr.simplekeyboard.inputmethod.keyboard.internal;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.util.Xml;
import org.xmlpull.v1.XmlPullParser;
import java.util.ArrayDeque;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.keyboard.Key;
import rkr.simplekeyboard.inputmethod.keyboard.Keyboard;
import rkr.simplekeyboard.inputmethod.latin.utils.ResourceUtils;
public final class KeyboardRow {
    private static final String TAG = KeyboardRow.class.getSimpleName();
    private static final float FLOAT_THRESHOLD = 0.0001f;
    private static final int KEYWIDTH_NOT_ENUM = 0;
    private static final int KEYWIDTH_FILL_RIGHT = -1;
    private final KeyboardParams mParams;
        private final float mY;
        private final float mRowHeight;
        private final float mKeyTopPadding;
        private final float mKeyBottomPadding;
        private float mNextKeyXPos;
        private float mCurrentX;
        private float mCurrentKeyWidth;
        private float mCurrentKeyLeftPadding;
        private float mCurrentKeyRightPadding;
        private boolean mLastKeyWasSpacer = false;
        private float mLastKeyRightEdge = 0;
    private final ArrayDeque<RowAttributes> mRowAttributesStack = new ArrayDeque<>();
    private static class RowAttributes {
                public final float mDefaultKeyPaddedWidth;
                public final int mDefaultKeyLabelFlags;
                public final int mDefaultBackgroundType;
                public RowAttributes(final TypedArray keyAttr, final float defaultKeyPaddedWidth,
                final float keyboardWidth) {
            mDefaultKeyPaddedWidth = ResourceUtils.getFraction(keyAttr,
                    R.styleable.Keyboard_Key_keyWidth, keyboardWidth, defaultKeyPaddedWidth);
            mDefaultKeyLabelFlags = keyAttr.getInt(R.styleable.Keyboard_Key_keyLabelFlags, 0);
            mDefaultBackgroundType = keyAttr.getInt(R.styleable.Keyboard_Key_backgroundType,
                    Key.BACKGROUND_TYPE_NORMAL);
        }
                public RowAttributes(final TypedArray keyAttr, final RowAttributes defaultRowAttr,
                final float keyboardWidth) {
            mDefaultKeyPaddedWidth = ResourceUtils.getFraction(keyAttr,
                    R.styleable.Keyboard_Key_keyWidth, keyboardWidth,
                    defaultRowAttr.mDefaultKeyPaddedWidth);
            mDefaultKeyLabelFlags = keyAttr.getInt(R.styleable.Keyboard_Key_keyLabelFlags, 0)
                    | defaultRowAttr.mDefaultKeyLabelFlags;
            mDefaultBackgroundType = keyAttr.getInt(R.styleable.Keyboard_Key_backgroundType,
                    defaultRowAttr.mDefaultBackgroundType);
        }
    }
    public KeyboardRow(final Resources res, final KeyboardParams params,
            final XmlPullParser parser, final float y) {
        mParams = params;
        final TypedArray keyboardAttr = res.obtainAttributes(Xml.asAttributeSet(parser),
                R.styleable.Keyboard);
        if (y < FLOAT_THRESHOLD) {
            mKeyTopPadding = params.mTopPadding;
        } else {
            mKeyTopPadding = 0;
        }
        final float baseRowHeight = ResourceUtils.getDimensionOrFraction(keyboardAttr,
                R.styleable.Keyboard_rowHeight, params.mBaseHeight, params.mDefaultRowHeight);
        float keyHeight = baseRowHeight - params.mVerticalGap;
        final float rowEndY = y + mKeyTopPadding + keyHeight + params.mVerticalGap;
        final float keyboardBottomEdge = params.mOccupiedHeight - params.mBottomPadding;
        if (rowEndY > keyboardBottomEdge - FLOAT_THRESHOLD) {
            final float keyEndY = y + mKeyTopPadding + keyHeight;
            final float keyOverflow = keyEndY - keyboardBottomEdge;
            if (keyOverflow > FLOAT_THRESHOLD) {
                if (Math.round(keyOverflow) > 0) {
                    Log.e(TAG, "The row is too tall to fit in the keyboard (" + keyOverflow
                            + " px). The height was reduced to fit.");
                }
                keyHeight = Math.max(keyboardBottomEdge - y - mKeyTopPadding, 0);
            }
            mKeyBottomPadding = Math.max(params.mOccupiedHeight - keyEndY, 0);
        } else {
            mKeyBottomPadding = params.mVerticalGap;
        }
        mRowHeight = mKeyTopPadding + keyHeight + mKeyBottomPadding;
        keyboardAttr.recycle();
        final TypedArray keyAttr = res.obtainAttributes(Xml.asAttributeSet(parser),
                R.styleable.Keyboard_Key);
        mRowAttributesStack.push(new RowAttributes(
                keyAttr, params.mDefaultKeyPaddedWidth, params.mBaseWidth));
        keyAttr.recycle();
        mY = y + mKeyTopPadding;
        mLastKeyRightEdge = 0;
        mNextKeyXPos = params.mLeftPadding;
    }
    public void pushRowAttributes(final TypedArray keyAttr) {
        final RowAttributes newAttributes = new RowAttributes(
                keyAttr, mRowAttributesStack.peek(), mParams.mBaseWidth);
        mRowAttributesStack.push(newAttributes);
    }
    public void popRowAttributes() {
        mRowAttributesStack.pop();
    }
    private float getDefaultKeyPaddedWidth() {
        return mRowAttributesStack.peek().mDefaultKeyPaddedWidth;
    }
    public int getDefaultKeyLabelFlags() {
        return mRowAttributesStack.peek().mDefaultKeyLabelFlags;
    }
    public int getDefaultBackgroundType() {
        return mRowAttributesStack.peek().mDefaultBackgroundType;
    }
        public void updateXPos(final TypedArray keyAttr) {
        if (keyAttr == null || !keyAttr.hasValue(R.styleable.Keyboard_Key_keyXPos)) {
            return;
        }
        final float keyXPos = ResourceUtils.getFraction(keyAttr, R.styleable.Keyboard_Key_keyXPos,
                mParams.mBaseWidth, 0) + mParams.mLeftPadding;
        if (keyXPos + FLOAT_THRESHOLD < mLastKeyRightEdge) {
            Log.e(TAG, "The specified keyXPos (" + keyXPos
                    + ") is smaller than the next available x position (" + mLastKeyRightEdge
                    + "). The x position was increased to avoid overlapping keys.");
            mNextKeyXPos = mLastKeyRightEdge;
        } else {
            mNextKeyXPos = keyXPos;
        }
    }
        public void setCurrentKey(final TypedArray keyAttr, final boolean isSpacer) {
        final float defaultGap = mParams.mHorizontalGap / 2;
        updateXPos(keyAttr);
        final float keyboardRightEdge = mParams.mOccupiedWidth - mParams.mRightPadding;
        float keyWidth;
        if (isSpacer) {
            final float leftGap = Math.min(mNextKeyXPos - mLastKeyRightEdge - defaultGap,
                    defaultGap);
            mCurrentX = mNextKeyXPos - leftGap;
            keyWidth = getKeyWidth(keyAttr) + leftGap;
            if (mCurrentX + keyWidth + FLOAT_THRESHOLD < keyboardRightEdge) {
                keyWidth += defaultGap;
            }
            mCurrentKeyLeftPadding = 0;
            mCurrentKeyRightPadding = 0;
        } else {
            mCurrentX = mNextKeyXPos;
            if (mLastKeyRightEdge < FLOAT_THRESHOLD || mLastKeyWasSpacer) {
                mCurrentKeyLeftPadding = mCurrentX - mLastKeyRightEdge;
            } else {
                mCurrentKeyLeftPadding = (mCurrentX - mLastKeyRightEdge) / 2;
            }
            keyWidth = getKeyWidth(keyAttr);
            mCurrentKeyRightPadding = defaultGap;
        }
        final float keyOverflow = mCurrentX + keyWidth - keyboardRightEdge;
        if (keyOverflow > FLOAT_THRESHOLD) {
            if (Math.round(keyOverflow) > 0) {
                Log.e(TAG, "The " + (isSpacer ? "spacer" : "key")
                        + " is too wide to fit in the keyboard (" + keyOverflow
                        + " px). The width was reduced to fit.");
            }
            keyWidth = Math.max(keyboardRightEdge - mCurrentX, 0);
        }
        mCurrentKeyWidth = keyWidth;
        mLastKeyRightEdge = mCurrentX + keyWidth;
        mLastKeyWasSpacer = isSpacer;
        mNextKeyXPos = mLastKeyRightEdge + (isSpacer ? defaultGap : mParams.mHorizontalGap);
    }
    private float getKeyWidth(final TypedArray keyAttr) {
        if (keyAttr == null) {
            return getDefaultKeyPaddedWidth() - mParams.mHorizontalGap;
        }
        final int widthType = ResourceUtils.getEnumValue(keyAttr,
                R.styleable.Keyboard_Key_keyWidth, KEYWIDTH_NOT_ENUM);
        switch (widthType) {
            case KEYWIDTH_FILL_RIGHT:
                final float keyboardRightEdge = mParams.mOccupiedWidth - mParams.mRightPadding;
                return keyboardRightEdge - mCurrentX;
            default:
                return ResourceUtils.getFraction(keyAttr, R.styleable.Keyboard_Key_keyWidth,
                        mParams.mBaseWidth, getDefaultKeyPaddedWidth()) - mParams.mHorizontalGap;
        }
    }
    public float getRowHeight() {
        return mRowHeight;
    }
    public float getKeyY() {
        return mY;
    }
    public float getKeyX() {
        return mCurrentX;
    }
    public float getKeyWidth() {
        return mCurrentKeyWidth;
    }
    public float getKeyHeight() {
        return mRowHeight - mKeyTopPadding - mKeyBottomPadding;
    }
    public float getKeyTopPadding() {
        return mKeyTopPadding;
    }
    public float getKeyBottomPadding() {
        return mKeyBottomPadding;
    }
    public float getKeyLeftPadding() {
        return mCurrentKeyLeftPadding;
    }
    public float getKeyRightPadding() {
        return mCurrentKeyRightPadding;
    }
}
