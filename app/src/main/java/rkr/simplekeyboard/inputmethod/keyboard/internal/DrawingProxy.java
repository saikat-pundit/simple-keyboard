package rkr.simplekeyboard.inputmethod.keyboard.internal;
import rkr.simplekeyboard.inputmethod.keyboard.Key;
import rkr.simplekeyboard.inputmethod.keyboard.MoreKeysPanel;
import rkr.simplekeyboard.inputmethod.keyboard.PointerTracker;
public interface DrawingProxy {
        void onKeyPressed(Key key, boolean withPreview);
        void onKeyReleased(Key key, boolean withAnimation);
        MoreKeysPanel showMoreKeysKeyboard(Key key, PointerTracker tracker);
        void startWhileTypingAnimation(int fadeInOrOut);
    int FADE_IN = 0;
    int FADE_OUT = 1;
}
