package rkr.simplekeyboard.inputmethod.keyboard.internal;
import android.util.Log;
import android.view.MotionEvent;
import rkr.simplekeyboard.inputmethod.keyboard.Key;
import rkr.simplekeyboard.inputmethod.keyboard.KeyDetector;
import rkr.simplekeyboard.inputmethod.keyboard.PointerTracker;
import rkr.simplekeyboard.inputmethod.latin.common.CoordinateUtils;
public final class NonDistinctMultitouchHelper {
    private static final String TAG = NonDistinctMultitouchHelper.class.getSimpleName();
    private static final int MAIN_POINTER_TRACKER_ID = 0;
    private int mOldPointerCount = 1;
    private Key mOldKey;
    private int[] mLastCoords = CoordinateUtils.newInstance();
    public void processMotionEvent(final MotionEvent me, final KeyDetector keyDetector) {
        final int pointerCount = me.getPointerCount();
        final int oldPointerCount = mOldPointerCount;
        mOldPointerCount = pointerCount;
        if (pointerCount > 1 && oldPointerCount > 1) {
            return;
        }
        final PointerTracker mainTracker = PointerTracker.getPointerTracker(
                MAIN_POINTER_TRACKER_ID);
        final int action = me.getActionMasked();
        final int index = me.getActionIndex();
        final long eventTime = me.getEventTime();
        final long downTime = me.getDownTime();
        if (oldPointerCount == 1 && pointerCount == 1) {
            if (me.getPointerId(index) == mainTracker.mPointerId) {
                mainTracker.processMotionEvent(me, keyDetector);
                return;
            }
            injectMotionEvent(action, me.getX(index), me.getY(index), downTime, eventTime,
                    mainTracker, keyDetector);
            return;
        }
        if (oldPointerCount == 1 && pointerCount == 2) {
            mainTracker.getLastCoordinates(mLastCoords);
            final int x = CoordinateUtils.x(mLastCoords);
            final int y = CoordinateUtils.y(mLastCoords);
            mOldKey = mainTracker.getKeyOn(x, y);
            injectMotionEvent(MotionEvent.ACTION_UP, x, y, downTime, eventTime,
                    mainTracker, keyDetector);
            return;
        }
        if (oldPointerCount == 2 && pointerCount == 1) {
            final int x = (int)me.getX(index);
            final int y = (int)me.getY(index);
            final Key newKey = mainTracker.getKeyOn(x, y);
            if (mOldKey != newKey) {
                injectMotionEvent(MotionEvent.ACTION_DOWN, x, y, downTime, eventTime,
                        mainTracker, keyDetector);
                if (action == MotionEvent.ACTION_UP) {
                    injectMotionEvent(MotionEvent.ACTION_UP, x, y, downTime, eventTime,
                            mainTracker, keyDetector);
                }
            }
            return;
        }
        Log.w(TAG, "Unknown touch panel behavior: pointer count is "
                + pointerCount + " (previously " + oldPointerCount + ")");
    }
    private static void injectMotionEvent(final int action, final float x, final float y,
            final long downTime, final long eventTime, final PointerTracker tracker,
            final KeyDetector keyDetector) {
        final MotionEvent me = MotionEvent.obtain(
                downTime, eventTime, action, x, y, 0 );
        try {
            tracker.processMotionEvent(me, keyDetector);
        } finally {
            me.recycle();
        }
    }
}
