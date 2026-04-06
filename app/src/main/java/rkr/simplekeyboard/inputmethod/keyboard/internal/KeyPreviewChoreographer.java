package rkr.simplekeyboard.inputmethod.keyboard.internal;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import java.util.ArrayDeque;
import java.util.HashMap;
import rkr.simplekeyboard.inputmethod.keyboard.Key;
import rkr.simplekeyboard.inputmethod.latin.common.CoordinateUtils;
import rkr.simplekeyboard.inputmethod.latin.utils.ViewLayoutUtils;
public final class KeyPreviewChoreographer {
    private final ArrayDeque<KeyPreviewView> mFreeKeyPreviewViews = new ArrayDeque<>();
    private final HashMap<Key,KeyPreviewView> mShowingKeyPreviewViews = new HashMap<>();
    private final KeyPreviewDrawParams mParams;
    public KeyPreviewChoreographer(final KeyPreviewDrawParams params) {
        mParams = params;
    }
    public KeyPreviewView getKeyPreviewView(final Key key, final ViewGroup placerView) {
        KeyPreviewView keyPreviewView = mShowingKeyPreviewViews.remove(key);
        if (keyPreviewView != null) {
            keyPreviewView.setScaleX(1);
            keyPreviewView.setScaleY(1);
            return keyPreviewView;
        }
        keyPreviewView = mFreeKeyPreviewViews.poll();
        if (keyPreviewView != null) {
            keyPreviewView.setScaleX(1);
            keyPreviewView.setScaleY(1);
            return keyPreviewView;
        }
        final Context context = placerView.getContext();
        keyPreviewView = new KeyPreviewView(context, null );
        keyPreviewView.setBackgroundResource(mParams.mPreviewBackgroundResId);
        placerView.addView(keyPreviewView, ViewLayoutUtils.newLayoutParam(placerView, 0, 0));
        return keyPreviewView;
    }
    public void dismissKeyPreview(final Key key, final boolean withAnimation) {
        if (key == null) {
            return;
        }
        final KeyPreviewView keyPreviewView = mShowingKeyPreviewViews.get(key);
        if (keyPreviewView == null) {
            return;
        }
        final Object tag = keyPreviewView.getTag();
        if (withAnimation) {
            if (tag instanceof KeyPreviewAnimators) {
                final KeyPreviewAnimators animators = (KeyPreviewAnimators)tag;
                animators.startDismiss();
                return;
            }
        }
        mShowingKeyPreviewViews.remove(key);
        if (tag instanceof Animator) {
            ((Animator)tag).cancel();
        }
        keyPreviewView.setTag(null);
        keyPreviewView.setVisibility(View.INVISIBLE);
        mFreeKeyPreviewViews.add(keyPreviewView);
    }
    public void placeAndShowKeyPreview(final Key key, final KeyboardIconsSet iconsSet,
            final KeyDrawParams drawParams, final int[] keyboardOrigin,
            final ViewGroup placerView, final boolean withAnimation,
            final int backgroundColor) {
        final KeyPreviewView keyPreviewView = getKeyPreviewView(key, placerView);
        placeKeyPreview(key, keyPreviewView, iconsSet, drawParams, keyboardOrigin, backgroundColor);
        showKeyPreview(key, keyPreviewView, withAnimation);
    }
    private void placeKeyPreview(final Key key, final KeyPreviewView keyPreviewView,
            final KeyboardIconsSet iconsSet, final KeyDrawParams drawParams,
            final int[] originCoords, final int backgroundColor) {
        keyPreviewView.setPreviewVisual(key, iconsSet, drawParams, backgroundColor);
        keyPreviewView.measure(
                ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        mParams.setGeometry(keyPreviewView);
        final int previewWidth = Math.max(keyPreviewView.getMeasuredWidth(), mParams.mMinPreviewWidth);
        final int previewHeight = mParams.mPreviewHeight;
        final int keyWidth = key.getWidth();
        int previewX = key.getX() - (previewWidth - keyWidth) / 2
                + CoordinateUtils.x(originCoords);
        final int previewY = key.getY() - previewHeight + mParams.mPreviewOffset
                + CoordinateUtils.y(originCoords);
        ViewLayoutUtils.placeViewAt(
                keyPreviewView, previewX, previewY, previewWidth, previewHeight);
    }
    void showKeyPreview(final Key key, final KeyPreviewView keyPreviewView,
            final boolean withAnimation) {
        if (!withAnimation) {
            keyPreviewView.setVisibility(View.VISIBLE);
            mShowingKeyPreviewViews.put(key, keyPreviewView);
            return;
        }
        final Animator dismissAnimator = createDismissAnimator(key, keyPreviewView);
        final KeyPreviewAnimators animators = new KeyPreviewAnimators(dismissAnimator);
        keyPreviewView.setTag(animators);
        showKeyPreview(key, keyPreviewView, false );
    }
    private Animator createDismissAnimator(final Key key, final KeyPreviewView keyPreviewView) {
        final Animator dismissAnimator = mParams.createDismissAnimator(keyPreviewView);
        dismissAnimator.addListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(final Animator animator) {
                dismissKeyPreview(key, false );
            }
        });
        return dismissAnimator;
    }
    private static class KeyPreviewAnimators extends AnimatorListenerAdapter {
        private final Animator mDismissAnimator;
        public KeyPreviewAnimators(final Animator dismissAnimator) {
            mDismissAnimator = dismissAnimator;
        }
        public void startDismiss() {
            mDismissAnimator.start();
        }
    }
}
