package rkr.simplekeyboard.inputmethod.keyboard.internal;
import rkr.simplekeyboard.inputmethod.keyboard.Key;
import rkr.simplekeyboard.inputmethod.keyboard.PointerTracker;
public interface TimerProxy {
        void startTypingStateTimer(Key typedKey);
        boolean isTypingState();
        void startKeyRepeatTimerOf(PointerTracker tracker, int repeatCount, int delay);
        void startLongPressTimerOf(PointerTracker tracker, int delay);
        void cancelLongPressTimersOf(PointerTracker tracker);
        void cancelLongPressShiftKeyTimer();
        void cancelKeyTimersOf(PointerTracker tracker);
        void startDoubleTapShiftKeyTimer();
        void cancelDoubleTapShiftKeyTimer();
        boolean isInDoubleTapShiftKeyTimeout();
}
