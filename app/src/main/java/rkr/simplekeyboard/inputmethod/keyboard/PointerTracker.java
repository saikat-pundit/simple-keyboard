package rkr.simplekeyboard.inputmethod.keyboard;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.util.Log;
import android.view.MotionEvent;
import java.util.ArrayList;
import rkr.simplekeyboard.inputmethod.R;
import rkr.simplekeyboard.inputmethod.keyboard.internal.BogusMoveEventDetector;
import rkr.simplekeyboard.inputmethod.keyboard.internal.DrawingProxy;
import rkr.simplekeyboard.inputmethod.keyboard.internal.PointerTrackerQueue;
import rkr.simplekeyboard.inputmethod.keyboard.internal.TimerProxy;
import rkr.simplekeyboard.inputmethod.latin.common.Constants;
import rkr.simplekeyboard.inputmethod.latin.common.CoordinateUtils;
import rkr.simplekeyboard.inputmethod.latin.define.DebugFlags;
import rkr.simplekeyboard.inputmethod.latin.settings.Settings;
public final class PointerTracker implements PointerTrackerQueue.Element {
    private static final String TAG = PointerTracker.class.getSimpleName();
    private static final boolean DEBUG_EVENT = false;
    private static final boolean DEBUG_MOVE_EVENT = false;
    private static final boolean DEBUG_LISTENER = false;
    private static boolean DEBUG_MODE = DebugFlags.DEBUG_ENABLED || DEBUG_EVENT;
    static final class PointerTrackerParams {
        public final boolean mKeySelectionByDraggingFinger;
        public final int mTouchNoiseThresholdTime;
        public final int mTouchNoiseThresholdDistance;
        public final int mKeyRepeatStartTimeout;
        public final int mKeyRepeatInterval;
        public final int mLongPressShiftLockTimeout;
        public PointerTrackerParams(final TypedArray mainKeyboardViewAttr) {
            mKeySelectionByDraggingFinger = mainKeyboardViewAttr.getBoolean(
                    R.styleable.MainKeyboardView_keySelectionByDraggingFinger, false);
            mTouchNoiseThresholdTime = mainKeyboardViewAttr.getInt(
                    R.styleable.MainKeyboardView_touchNoiseThresholdTime, 0);
            mTouchNoiseThresholdDistance = mainKeyboardViewAttr.getDimensionPixelSize(
                    R.styleable.MainKeyboardView_touchNoiseThresholdDistance, 0);
            mKeyRepeatStartTimeout = mainKeyboardViewAttr.getInt(
                    R.styleable.MainKeyboardView_keyRepeatStartTimeout, 0);
            mKeyRepeatInterval = mainKeyboardViewAttr.getInt(
                    R.styleable.MainKeyboardView_keyRepeatInterval, 0);
            mLongPressShiftLockTimeout = mainKeyboardViewAttr.getInt(
                    R.styleable.MainKeyboardView_longPressShiftLockTimeout, 0);
        }
    }
    private static PointerTrackerParams sParams;
    private static int sPointerStep = (int)(10.0 * Resources.getSystem().getDisplayMetrics().density);
    private static final ArrayList<PointerTracker> sTrackers = new ArrayList<>();
    private static final PointerTrackerQueue sPointerTrackerQueue = new PointerTrackerQueue();
    public final int mPointerId;
    private static DrawingProxy sDrawingProxy;
    private static TimerProxy sTimerProxy;
    private static KeyboardActionListener sListener = KeyboardActionListener.EMPTY_LISTENER;
    private KeyDetector mKeyDetector = new KeyDetector();
    private Keyboard mKeyboard;
    private final BogusMoveEventDetector mBogusMoveEventDetector = new BogusMoveEventDetector();
    private int[] mDownCoordinates = CoordinateUtils.newInstance();
    private Key mCurrentKey = null;
    private int mKeyX;
    private int mKeyY;
    private int mLastX;
    private int mLastY;
    private int mStartX;
    private long mStartTime;
    private boolean mCursorMoved = false;
    private boolean mKeyboardLayoutHasBeenChanged;
    private boolean mIsTrackingForActionDisabled;
    private MoreKeysPanel mMoreKeysPanel;
    private static final int MULTIPLIER_FOR_LONG_PRESS_TIMEOUT_IN_SLIDING_INPUT = 3;
    boolean mIsInDraggingFinger;
    boolean mIsInSlidingKeyInput;
    private int mCurrentRepeatingKeyCode = Constants.NOT_A_CODE;
    private boolean mIsAllowedDraggingFinger;
    public static void init(final TypedArray mainKeyboardViewAttr, final TimerProxy timerProxy,
            final DrawingProxy drawingProxy) {
        sParams = new PointerTrackerParams(mainKeyboardViewAttr);
        final Resources res = mainKeyboardViewAttr.getResources();
        BogusMoveEventDetector.init(res);
        sTimerProxy = timerProxy;
        sDrawingProxy = drawingProxy;
    }
    public static PointerTracker getPointerTracker(final int id) {
        final ArrayList<PointerTracker> trackers = sTrackers;
        for (int i = trackers.size(); i <= id; i++) {
            final PointerTracker tracker = new PointerTracker(i);
            trackers.add(tracker);
        }
        return trackers.get(id);
    }
    public static boolean isAnyInDraggingFinger() {
        return sPointerTrackerQueue.isAnyInDraggingFinger();
    }
    public static boolean isAnyInCursorMove() {
        return sPointerTrackerQueue.isAnyInCursorMove();
    }
    public static void cancelAllPointerTrackers() {
        sPointerTrackerQueue.cancelAllPointerTrackers();
    }
    public static void setKeyboardActionListener(final KeyboardActionListener listener) {
        sListener = listener;
    }
    public static void setKeyDetector(final KeyDetector keyDetector) {
        final Keyboard keyboard = keyDetector.getKeyboard();
        if (keyboard == null) {
            return;
        }
        final int trackersSize = sTrackers.size();
        for (int i = 0; i < trackersSize; ++i) {
            final PointerTracker tracker = sTrackers.get(i);
            tracker.setKeyDetectorInner(keyDetector);
        }
    }
    public static void setReleasedKeyGraphicsToAllKeys() {
        final int trackersSize = sTrackers.size();
        for (int i = 0; i < trackersSize; ++i) {
            final PointerTracker tracker = sTrackers.get(i);
            tracker.setReleasedKeyGraphics(tracker.getKey(), true );
        }
    }
    public static void dismissAllMoreKeysPanels() {
        final int trackersSize = sTrackers.size();
        for (int i = 0; i < trackersSize; ++i) {
            final PointerTracker tracker = sTrackers.get(i);
            tracker.dismissMoreKeysPanel();
        }
    }
    private PointerTracker(final int id) {
        mPointerId = id;
    }
    private boolean callListenerOnPressAndCheckKeyboardLayoutChange(final Key key,
            final int repeatCount) {
        final boolean ignoreModifierKey = mIsInDraggingFinger && key.isModifier();
        if (DEBUG_LISTENER) {
            Log.d(TAG, String.format("[%d] onPress    : %s%s%s", mPointerId,
                    (key == null ? "none" : Constants.printableCode(key.getCode())),
                    ignoreModifierKey ? " ignoreModifier" : "",
                    repeatCount > 0 ? " repeatCount=" + repeatCount : ""));
        }
        if (ignoreModifierKey) {
            return false;
        }
        sListener.onPressKey(key.getCode(), repeatCount, getActivePointerTrackerCount() == 1);
        final boolean keyboardLayoutHasBeenChanged = mKeyboardLayoutHasBeenChanged;
        mKeyboardLayoutHasBeenChanged = false;
        sTimerProxy.startTypingStateTimer(key);
        return keyboardLayoutHasBeenChanged;
    }
    private void callListenerOnCodeInput(final Key key, final int primaryCode, final int x,
            final int y, final boolean isKeyRepeat) {
        final boolean ignoreModifierKey = mIsInDraggingFinger && key.isModifier();
        final boolean altersCode = key.altCodeWhileTyping() && sTimerProxy.isTypingState();
        final int code = altersCode ? key.getAltCode() : primaryCode;
        if (DEBUG_LISTENER) {
            final String output = code == Constants.CODE_OUTPUT_TEXT
                    ? key.getOutputText() : Constants.printableCode(code);
            Log.d(TAG, String.format("[%d] onCodeInput: %4d %4d %s%s%s", mPointerId, x, y,
                    output, ignoreModifierKey ? " ignoreModifier" : "",
                    altersCode ? " altersCode" : ""));
        }
        if (ignoreModifierKey) {
            return;
        }
        if (code == Constants.CODE_OUTPUT_TEXT) {
            sListener.onTextInput(key.getOutputText());
        } else if (code != Constants.CODE_UNSPECIFIED) {
            sListener.onCodeInput(code,
                Constants.NOT_A_COORDINATE, Constants.NOT_A_COORDINATE, isKeyRepeat);
        }
    }
    private void callListenerOnRelease(final Key key, final int primaryCode,
            final boolean withSliding) {
        final boolean ignoreModifierKey = mIsInDraggingFinger && key.isModifier();
        if (DEBUG_LISTENER) {
            Log.d(TAG, String.format("[%d] onRelease  : %s%s%s", mPointerId,
                    Constants.printableCode(primaryCode),
                    withSliding ? " sliding" : "", ignoreModifierKey ? " ignoreModifier" : ""));
        }
        if (ignoreModifierKey) {
            return;
        }
        sListener.onReleaseKey(primaryCode, withSliding);
    }
    private void callListenerOnFinishSlidingInput() {
        if (DEBUG_LISTENER) {
            Log.d(TAG, String.format("[%d] onFinishSlidingInput", mPointerId));
        }
        sListener.onFinishSlidingInput();
    }
    private void setKeyDetectorInner(final KeyDetector keyDetector) {
        final Keyboard keyboard = keyDetector.getKeyboard();
        if (keyboard == null) {
            return;
        }
        if (keyDetector == mKeyDetector && keyboard == mKeyboard) {
            return;
        }
        mKeyDetector = keyDetector;
        mKeyboard = keyboard;
        mKeyboardLayoutHasBeenChanged = true;
        final int keyPaddedWidth = mKeyboard.mMostCommonKeyWidth
                + Math.round(mKeyboard.mHorizontalGap);
        final int keyPaddedHeight = mKeyboard.mMostCommonKeyHeight
                + Math.round(mKeyboard.mVerticalGap);
        mBogusMoveEventDetector.setKeyboardGeometry(keyPaddedWidth, keyPaddedHeight);
    }
    @Override
    public boolean isInDraggingFinger() {
        return mIsInDraggingFinger;
    }
    @Override
    public boolean isInCursorMove() {
        return mCursorMoved;
    }
    public Key getKey() {
        return mCurrentKey;
    }
    @Override
    public boolean isModifier() {
        return mCurrentKey != null && mCurrentKey.isModifier();
    }
    public Key getKeyOn(final int x, final int y) {
        return mKeyDetector.detectHitKey(x, y);
    }
    private void setReleasedKeyGraphics(final Key key, final boolean withAnimation) {
        if (key == null) {
            return;
        }
        sDrawingProxy.onKeyReleased(key, withAnimation);
        if (key.isShift()) {
            for (final Key shiftKey : mKeyboard.mShiftKeys) {
                if (shiftKey != key) {
                    sDrawingProxy.onKeyReleased(shiftKey, false );
                }
            }
        }
        if (key.altCodeWhileTyping()) {
            final int altCode = key.getAltCode();
            final Key altKey = mKeyboard.getKey(altCode);
            if (altKey != null) {
                sDrawingProxy.onKeyReleased(altKey, false );
            }
            for (final Key k : mKeyboard.mAltCodeKeysWhileTyping) {
                if (k != key && k.getAltCode() == altCode) {
                    sDrawingProxy.onKeyReleased(k, false );
                }
            }
        }
    }
    private void setPressedKeyGraphics(final Key key) {
        if (key == null) {
            return;
        }
        final boolean altersCode = key.altCodeWhileTyping() && sTimerProxy.isTypingState();
        sDrawingProxy.onKeyPressed(key, true);
        if (key.isShift()) {
            for (final Key shiftKey : mKeyboard.mShiftKeys) {
                if (shiftKey != key) {
                    sDrawingProxy.onKeyPressed(shiftKey, false );
                }
            }
        }
        if (altersCode) {
            final int altCode = key.getAltCode();
            final Key altKey = mKeyboard.getKey(altCode);
            if (altKey != null) {
                sDrawingProxy.onKeyPressed(altKey, false );
            }
            for (final Key k : mKeyboard.mAltCodeKeysWhileTyping) {
                if (k != key && k.getAltCode() == altCode) {
                    sDrawingProxy.onKeyPressed(k, false );
                }
            }
        }
    }
    public void getLastCoordinates(final int[] outCoords) {
        CoordinateUtils.set(outCoords, mLastX, mLastY);
    }
    private Key onDownKey(final int x, final int y) {
        CoordinateUtils.set(mDownCoordinates, x, y);
        mBogusMoveEventDetector.onDownKey();
        return onMoveToNewKey(onMoveKeyInternal(x, y), x, y);
    }
    private static int getDistance(final int x1, final int y1, final int x2, final int y2) {
        return (int)Math.hypot(x1 - x2, y1 - y2);
    }
    private Key onMoveKeyInternal(final int x, final int y) {
        mBogusMoveEventDetector.onMoveKey(getDistance(x, y, mLastX, mLastY));
        mLastX = x;
        mLastY = y;
        return mKeyDetector.detectHitKey(x, y);
    }
    private Key onMoveKey(final int x, final int y) {
        return onMoveKeyInternal(x, y);
    }
    private Key onMoveToNewKey(final Key newKey, final int x, final int y) {
        mCurrentKey = newKey;
        mKeyX = x;
        mKeyY = y;
        return newKey;
    }
     static int getActivePointerTrackerCount() {
        return sPointerTrackerQueue.size();
    }
    public void processMotionEvent(final MotionEvent me, final KeyDetector keyDetector) {
        final int action = me.getActionMasked();
        final long eventTime = me.getEventTime();
        if (action == MotionEvent.ACTION_MOVE) {
            final boolean shouldIgnoreOtherPointers =
                    isShowingMoreKeysPanel() && getActivePointerTrackerCount() == 1;
            final int pointerCount = me.getPointerCount();
            for (int index = 0; index < pointerCount; index++) {
                final int id = me.getPointerId(index);
                if (shouldIgnoreOtherPointers && id != mPointerId) {
                    continue;
                }
                final int x = (int)me.getX(index);
                final int y = (int)me.getY(index);
                final PointerTracker tracker = getPointerTracker(id);
                tracker.onMoveEvent(x, y, eventTime);
            }
            return;
        }
        final int index = me.getActionIndex();
        final int x = (int)me.getX(index);
        final int y = (int)me.getY(index);
        switch (action) {
        case MotionEvent.ACTION_DOWN:
        case MotionEvent.ACTION_POINTER_DOWN:
            onDownEvent(x, y, eventTime, keyDetector);
            break;
        case MotionEvent.ACTION_UP:
        case MotionEvent.ACTION_POINTER_UP:
            onUpEvent(x, y, eventTime);
            break;
        case MotionEvent.ACTION_CANCEL:
            onCancelEvent(x, y, eventTime);
            break;
        }
    }
    private void onDownEvent(final int x, final int y, final long eventTime,
            final KeyDetector keyDetector) {
        setKeyDetectorInner(keyDetector);
        if (DEBUG_EVENT) {
            printTouchEvent("onDownEvent:", x, y, eventTime);
        }
        final long deltaT = eventTime;
        if (deltaT < sParams.mTouchNoiseThresholdTime) {
            final int distance = getDistance(x, y, mLastX, mLastY);
            if (distance < sParams.mTouchNoiseThresholdDistance) {
                if (DEBUG_MODE)
                    Log.w(TAG, String.format("[%d] onDownEvent:"
                            + " ignore potential noise: time=%d distance=%d",
                            mPointerId, deltaT, distance));
                cancelTrackingForAction();
                return;
            }
        }
        final Key key = getKeyOn(x, y);
        mBogusMoveEventDetector.onActualDownEvent(x, y);
        if (key != null && key.isModifier()) {
            sPointerTrackerQueue.releaseAllPointers(eventTime);
        }
        sPointerTrackerQueue.add(this);
        onDownEventInternal(x, y);
    }
     boolean isShowingMoreKeysPanel() {
        return (mMoreKeysPanel != null);
    }
    private void dismissMoreKeysPanel() {
        if (isShowingMoreKeysPanel()) {
            mMoreKeysPanel.dismissMoreKeysPanel();
            mMoreKeysPanel = null;
        }
    }
    private void onDownEventInternal(final int x, final int y) {
        Key key = onDownKey(x, y);
        mIsAllowedDraggingFinger = sParams.mKeySelectionByDraggingFinger
                || (key != null && key.isModifier())
                || mKeyDetector.alwaysAllowsKeySelectionByDraggingFinger();
        mKeyboardLayoutHasBeenChanged = false;
        mIsTrackingForActionDisabled = false;
        resetKeySelectionByDraggingFinger();
        if (key != null) {
            if (callListenerOnPressAndCheckKeyboardLayoutChange(key, 0 )) {
                key = onDownKey(x, y);
            }
            startRepeatKey(key);
            startLongPressTimer(key);
            setPressedKeyGraphics(key);
            mStartX = x;
            mStartTime = System.currentTimeMillis();
        }
    }
    private void startKeySelectionByDraggingFinger(final Key key) {
        if (!mIsInDraggingFinger) {
            mIsInSlidingKeyInput = key.isModifier();
        }
        mIsInDraggingFinger = true;
    }
    private void resetKeySelectionByDraggingFinger() {
        mIsInDraggingFinger = false;
        mIsInSlidingKeyInput = false;
    }
    private void onMoveEvent(final int x, final int y, final long eventTime) {
        if (DEBUG_MOVE_EVENT) {
            printTouchEvent("onMoveEvent:", x, y, eventTime);
        }
        if (mIsTrackingForActionDisabled) {
            return;
        }
        if (isShowingMoreKeysPanel()) {
            final int translatedX = mMoreKeysPanel.translateX(x);
            final int translatedY = mMoreKeysPanel.translateY(y);
            mMoreKeysPanel.onMoveEvent(translatedX, translatedY, mPointerId);
            onMoveKey(x, y);
            return;
        }
        onMoveEventInternal(x, y, eventTime);
    }
    private void processDraggingFingerInToNewKey(final Key newKey, final int x, final int y) {
        Key key = newKey;
        if (callListenerOnPressAndCheckKeyboardLayoutChange(key, 0 )) {
            key = onMoveKey(x, y);
        }
        onMoveToNewKey(key, x, y);
        if (mIsTrackingForActionDisabled) {
            return;
        }
        startLongPressTimer(key);
        setPressedKeyGraphics(key);
    }
    private void processDraggingFingerOutFromOldKey(final Key oldKey) {
        setReleasedKeyGraphics(oldKey, true );
        callListenerOnRelease(oldKey, oldKey.getCode(), true );
        startKeySelectionByDraggingFinger(oldKey);
        sTimerProxy.cancelKeyTimersOf(this);
    }
    private void dragFingerFromOldKeyToNewKey(final Key key, final int x, final int y,
            final long eventTime, final Key oldKey) {
        processDraggingFingerOutFromOldKey(oldKey);
        startRepeatKey(key);
        if (mIsAllowedDraggingFinger) {
            processDraggingFingerInToNewKey(key, x, y);
        }
        else if (getActivePointerTrackerCount() > 1
                && !sPointerTrackerQueue.hasModifierKeyOlderThan(this)) {
            if (DEBUG_MODE) {
                Log.w(TAG, String.format("[%d] onMoveEvent:"
                        + " detected sliding finger while multi touching", mPointerId));
            }
            onUpEvent(x, y, eventTime);
            cancelTrackingForAction();
            setReleasedKeyGraphics(oldKey, true );
        } else {
            cancelTrackingForAction();
            setReleasedKeyGraphics(oldKey, true );
        }
    }
    private void dragFingerOutFromOldKey(final Key oldKey, final int x, final int y) {
        processDraggingFingerOutFromOldKey(oldKey);
        if (mIsAllowedDraggingFinger) {
            onMoveToNewKey(null, x, y);
        } else {
            cancelTrackingForAction();
        }
    }
    private void onMoveEventInternal(final int x, final int y, final long eventTime) {
        final Key oldKey = mCurrentKey;
        if (oldKey != null && oldKey.getCode() == Constants.CODE_SPACE && Settings.getInstance().getCurrent().mSpaceSwipeEnabled) {
            int steps = (x - mStartX) / sPointerStep;
            final int swipeIgnoreTime = Settings.getInstance().getCurrent().mKeyLongpressTimeout / MULTIPLIER_FOR_LONG_PRESS_TIMEOUT_IN_SLIDING_INPUT;
            if (steps != 0 && mStartTime + swipeIgnoreTime < System.currentTimeMillis()) {
                mCursorMoved = true;
                mStartX += steps * sPointerStep;
                sListener.onMoveCursorPointer(steps);
            }
            return;
        }
        if (oldKey != null && oldKey.getCode() == Constants.CODE_DELETE && Settings.getInstance().getCurrent().mDeleteSwipeEnabled) {
            int steps = (x - mStartX) / sPointerStep;
            if (steps != 0) {
                sTimerProxy.cancelKeyTimersOf(this);
                mCursorMoved = true;
                mStartX += steps * sPointerStep;
                sListener.onMoveDeletePointer(steps);
            }
            return;
        }
        final Key newKey = onMoveKey(x, y);
        if (newKey != null) {
            if (oldKey != null && isMajorEnoughMoveToBeOnNewKey(x, y, newKey)) {
                dragFingerFromOldKeyToNewKey(newKey, x, y, eventTime, oldKey);
            } else if (oldKey == null) {
                processDraggingFingerInToNewKey(newKey, x, y);
            }
        } else { 
            if (oldKey != null && isMajorEnoughMoveToBeOnNewKey(x, y, newKey)) {
                dragFingerOutFromOldKey(oldKey, x, y);
            }
        }
    }
    private void onUpEvent(final int x, final int y, final long eventTime) {
        if (DEBUG_EVENT) {
            printTouchEvent("onUpEvent  :", x, y, eventTime);
        }
        if (mCurrentKey != null && mCurrentKey.isModifier()) {
            sPointerTrackerQueue.releaseAllPointersExcept(this, eventTime);
        } else {
            sPointerTrackerQueue.releaseAllPointersOlderThan(this, eventTime);
        }
        onUpEventInternal(x, y);
        sPointerTrackerQueue.remove(this);
    }
    @Override
    public void onPhantomUpEvent(final long eventTime) {
        if (DEBUG_EVENT) {
            printTouchEvent("onPhntEvent:", mLastX, mLastY, eventTime);
        }
        onUpEventInternal(mLastX, mLastY);
        cancelTrackingForAction();
    }
    private void onUpEventInternal(final int x, final int y) {
        sTimerProxy.cancelKeyTimersOf(this);
        final boolean isInDraggingFinger = mIsInDraggingFinger;
        final boolean isInSlidingKeyInput = mIsInSlidingKeyInput;
        resetKeySelectionByDraggingFinger();
        final Key currentKey = mCurrentKey;
        mCurrentKey = null;
        final int currentRepeatingKeyCode = mCurrentRepeatingKeyCode;
        mCurrentRepeatingKeyCode = Constants.NOT_A_CODE;
        setReleasedKeyGraphics(currentKey, true );
        if (mCursorMoved && currentKey.getCode() == Constants.CODE_DELETE) {
            sListener.onUpWithDeletePointerActive();
        }
        if (mCursorMoved && currentKey.getCode() == Constants.CODE_SPACE) {
            sListener.onUpWithSpacePointerActive();
        }
        if (isShowingMoreKeysPanel()) {
            if (!mIsTrackingForActionDisabled) {
                final int translatedX = mMoreKeysPanel.translateX(x);
                final int translatedY = mMoreKeysPanel.translateY(y);
                mMoreKeysPanel.onUpEvent(translatedX, translatedY, mPointerId);
            }
            dismissMoreKeysPanel();
            return;
        }
        if (mCursorMoved) {
            mCursorMoved = false;
            return;
        }
        if (mIsTrackingForActionDisabled) {
            return;
        }
        if (currentKey != null && currentKey.isRepeatable()
                && (currentKey.getCode() == currentRepeatingKeyCode) && !isInDraggingFinger) {
            return;
        }
        detectAndSendKey(currentKey, mKeyX, mKeyY);
        if (isInSlidingKeyInput) {
            callListenerOnFinishSlidingInput();
        }
    }
    @Override
    public void cancelTrackingForAction() {
        if (isShowingMoreKeysPanel()) {
            return;
        }
        mIsTrackingForActionDisabled = true;
    }
    public void onLongPressed() {
        sTimerProxy.cancelLongPressTimersOf(this);
        if (isShowingMoreKeysPanel()) {
            return;
        }
        if (mCursorMoved) {
            return;
        }
        final Key key = getKey();
        if (key == null) {
            return;
        }
        if (key.hasNoPanelAutoMoreKey()) {
            cancelKeyTracking();
            final int moreKeyCode = key.getMoreKeys()[0].mCode;
            sListener.onPressKey(moreKeyCode, 0 , true );
            sListener.onCodeInput(moreKeyCode, Constants.NOT_A_COORDINATE,
                    Constants.NOT_A_COORDINATE, false );
            sListener.onReleaseKey(moreKeyCode, false );
            return;
        }
        final int code = key.getCode();
        if (code == Constants.CODE_SPACE || code == Constants.CODE_LANGUAGE_SWITCH) {
            if (sListener.onCustomRequest(Constants.CUSTOM_CODE_SHOW_INPUT_METHOD_PICKER)) {
                cancelKeyTracking();
                sListener.onReleaseKey(code, false );
                return;
            }
        }
        setReleasedKeyGraphics(key, false );
        final MoreKeysPanel moreKeysPanel = sDrawingProxy.showMoreKeysKeyboard(key, this);
        if (moreKeysPanel == null) {
            return;
        }
        final int translatedX = moreKeysPanel.translateX(mLastX);
        final int translatedY = moreKeysPanel.translateY(mLastY);
        moreKeysPanel.onDownEvent(translatedX, translatedY, mPointerId);
        mMoreKeysPanel = moreKeysPanel;
    }
    private void cancelKeyTracking() {
        resetKeySelectionByDraggingFinger();
        cancelTrackingForAction();
        setReleasedKeyGraphics(mCurrentKey, true );
        sPointerTrackerQueue.remove(this);
    }
    private void onCancelEvent(final int x, final int y, final long eventTime) {
        if (DEBUG_EVENT) {
            printTouchEvent("onCancelEvt:", x, y, eventTime);
        }
        cancelAllPointerTrackers();
        sPointerTrackerQueue.releaseAllPointers(eventTime);
        onCancelEventInternal();
    }
    private void onCancelEventInternal() {
        sTimerProxy.cancelKeyTimersOf(this);
        setReleasedKeyGraphics(mCurrentKey, true );
        resetKeySelectionByDraggingFinger();
        dismissMoreKeysPanel();
    }
    private boolean isMajorEnoughMoveToBeOnNewKey(final int x, final int y, final Key newKey) {
        final Key curKey = mCurrentKey;
        if (newKey == curKey) {
            return false;
        }
        if (curKey == null ) {
            return true;
        }
        final int keyHysteresisDistanceSquared = mKeyDetector.getKeyHysteresisDistanceSquared(
                mIsInSlidingKeyInput);
        final int distanceFromKeyEdgeSquared = curKey.squaredDistanceToHitboxEdge(x, y);
        if (distanceFromKeyEdgeSquared >= keyHysteresisDistanceSquared) {
            if (DEBUG_MODE) {
                final float distanceToEdgeRatio = (float)Math.sqrt(distanceFromKeyEdgeSquared)
                        / (mKeyboard.mMostCommonKeyWidth + mKeyboard.mHorizontalGap);
                Log.d(TAG, String.format("[%d] isMajorEnoughMoveToBeOnNewKey:"
                        +" %.2f key width from key edge", mPointerId, distanceToEdgeRatio));
            }
            return true;
        }
        if (!mIsAllowedDraggingFinger && mBogusMoveEventDetector.hasTraveledLongDistance(x, y)) {
            if (DEBUG_MODE) {
                final float keyDiagonal = (float)Math.hypot(
                        mKeyboard.mMostCommonKeyWidth + mKeyboard.mHorizontalGap,
                        mKeyboard.mMostCommonKeyHeight + mKeyboard.mVerticalGap);
                final float lengthFromDownRatio =
                        mBogusMoveEventDetector.getAccumulatedDistanceFromDownKey() / keyDiagonal;
                Log.d(TAG, String.format("[%d] isMajorEnoughMoveToBeOnNewKey:"
                        + " %.2f key diagonal from virtual down point",
                        mPointerId, lengthFromDownRatio));
            }
            return true;
        }
        return false;
    }
    private void startLongPressTimer(final Key key) {
        sTimerProxy.cancelLongPressShiftKeyTimer();
        if (key == null) return;
        if (!key.isLongPressEnabled()) return;
        if (mIsInDraggingFinger && key.getMoreKeys() == null) return;
        final int delay = getLongPressTimeout(key.getCode());
        if (delay <= 0) return;
        sTimerProxy.startLongPressTimerOf(this, delay);
    }
    private int getLongPressTimeout(final int code) {
        if (code == Constants.CODE_SHIFT) {
            return sParams.mLongPressShiftLockTimeout;
        }
        final int longpressTimeout = Settings.getInstance().getCurrent().mKeyLongpressTimeout;
        if (mIsInSlidingKeyInput) {
            return longpressTimeout * MULTIPLIER_FOR_LONG_PRESS_TIMEOUT_IN_SLIDING_INPUT;
        }
        if (code == Constants.CODE_SPACE) {
            return longpressTimeout * MULTIPLIER_FOR_LONG_PRESS_TIMEOUT_IN_SLIDING_INPUT;
        }
        return longpressTimeout;
    }
    private void detectAndSendKey(final Key key, final int x, final int y) {
        if (key == null) return;
        final int code = key.getCode();
        callListenerOnCodeInput(key, code, x, y, false );
        callListenerOnRelease(key, code, false );
    }
    private void startRepeatKey(final Key key) {
        if (key == null) return;
        if (!key.isRepeatable()) return;
        if (mIsInDraggingFinger) return;
        final int startRepeatCount = 1;
        startKeyRepeatTimer(startRepeatCount);
    }
    public void onKeyRepeat(final int code, final int repeatCount) {
        final Key key = getKey();
        if (key == null || key.getCode() != code) {
            mCurrentRepeatingKeyCode = Constants.NOT_A_CODE;
            return;
        }
        mCurrentRepeatingKeyCode = code;
        final int nextRepeatCount = repeatCount + 1;
        startKeyRepeatTimer(nextRepeatCount);
        callListenerOnPressAndCheckKeyboardLayoutChange(key, repeatCount);
        callListenerOnCodeInput(key, code, mKeyX, mKeyY, true );
    }
    private void startKeyRepeatTimer(final int repeatCount) {
        final int delay =
                (repeatCount == 1) ? sParams.mKeyRepeatStartTimeout : sParams.mKeyRepeatInterval;
        sTimerProxy.startKeyRepeatTimerOf(this, repeatCount, delay);
    }
    private void printTouchEvent(final String title, final int x, final int y,
            final long eventTime) {
        final Key key = mKeyDetector.detectHitKey(x, y);
        final String code = (key == null ? "none" : Constants.printableCode(key.getCode()));
        Log.d(TAG, String.format("[%d]%s%s %4d %4d %5d %s", mPointerId,
                (mIsTrackingForActionDisabled ? "-" : " "), title, x, y, eventTime, code));
    }
}
