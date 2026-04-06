package rkr.simplekeyboard.inputmethod.keyboard.internal;
import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.content.res.TypedArray;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import rkr.simplekeyboard.inputmethod.R;
public final class KeyPreviewDrawParams {
    public final int mPreviewOffset;
    public final int mPreviewHeight;
    public final int mMinPreviewWidth;
    public final int mPreviewBackgroundResId;
    private final int mDismissAnimatorResId;
    private int mLingerTimeout;
    private boolean mShowPopup = true;
    private int mVisibleWidth;
    private int mVisibleHeight;
    private int mVisibleOffset;
    public KeyPreviewDrawParams(final TypedArray mainKeyboardViewAttr) {
        mPreviewOffset = mainKeyboardViewAttr.getDimensionPixelOffset(
                R.styleable.MainKeyboardView_keyPreviewOffset, 0);
        mPreviewHeight = mainKeyboardViewAttr.getDimensionPixelSize(
                R.styleable.MainKeyboardView_keyPreviewHeight, 0);
        mMinPreviewWidth = mainKeyboardViewAttr.getDimensionPixelSize(
                R.styleable.MainKeyboardView_keyPreviewWidth, 0);
        mPreviewBackgroundResId = mainKeyboardViewAttr.getResourceId(
                R.styleable.MainKeyboardView_keyPreviewBackground, 0);
        mLingerTimeout = mainKeyboardViewAttr.getInt(
                R.styleable.MainKeyboardView_keyPreviewLingerTimeout, 0);
        mDismissAnimatorResId = mainKeyboardViewAttr.getResourceId(
                R.styleable.MainKeyboardView_keyPreviewDismissAnimator, 0);
    }
    public void setVisibleOffset(final int previewVisibleOffset) {
        mVisibleOffset = previewVisibleOffset;
    }
    public int getVisibleOffset() {
        return mVisibleOffset;
    }
    public void setGeometry(final View previewTextView) {
        final int previewWidth = Math.max(previewTextView.getMeasuredWidth(), mMinPreviewWidth);
        mVisibleWidth = previewWidth - previewTextView.getPaddingLeft()
                - previewTextView.getPaddingRight();
        mVisibleHeight = mPreviewHeight - previewTextView.getPaddingTop()
                - previewTextView.getPaddingBottom();
        setVisibleOffset(mPreviewOffset - previewTextView.getPaddingBottom());
    }
    public int getVisibleWidth() {
        return mVisibleWidth;
    }
    public int getVisibleHeight() {
        return mVisibleHeight;
    }
    public void setPopupEnabled(final boolean enabled, final int lingerTimeout) {
        mShowPopup = enabled;
        mLingerTimeout = lingerTimeout;
    }
    public boolean isPopupEnabled() {
        return mShowPopup;
    }
    public int getLingerTimeout() {
        return mLingerTimeout;
    }
    private static final AccelerateInterpolator ACCELERATE_INTERPOLATOR =
            new AccelerateInterpolator();
    public Animator createDismissAnimator(final View target) {
        final Animator animator = AnimatorInflater.loadAnimator(
                target.getContext(), mDismissAnimatorResId);
        animator.setTarget(target);
        animator.setInterpolator(ACCELERATE_INTERPOLATOR);
        return animator;
    }
}
