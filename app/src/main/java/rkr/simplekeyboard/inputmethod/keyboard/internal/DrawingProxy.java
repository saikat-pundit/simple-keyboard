package rkr.simplekeyboard.inputmethod.keyboard.internal;

import rkr.simplekeyboard.inputmethod.keyboard.Key;
import rkr.simplekeyboard.inputmethod.keyboard.MoreKeysPanel;
import rkr.simplekeyboard.inputmethod.keyboard.PointerTracker;

public interface DrawingProxy {
    /**
     * Called when a key is being pressed.
     * @param key the {@link Key} that is being pressed.
     * @param withPreview true if key popup preview should be displayed.
     */
    void onKeyPressed(Key key, boolean withPreview);

    /**
     * Called when a key is being released.
     * @param key the {@link Key} that is being released.
     * @param withAnimation when true, key popup preview should be dismissed with animation.
     */
    void onKeyReleased(Key key, boolean withAnimation);

    /**
     * Start showing more keys keyboard of a key that is being long pressed.
     * @param key the {@link Key} that is being long pressed and showing more keys keyboard.
     * @param tracker the {@link PointerTracker} that detects this long pressing.
     * @return {@link MoreKeysPanel} that is being shown. null if there is no need to show more keys
     *     keyboard.
     */
    MoreKeysPanel showMoreKeysKeyboard(Key key, PointerTracker tracker);

    /**
     * Start a while-typing-animation.
     * @param fadeInOrOut {@link #FADE_IN} starts while-typing-fade-in animation.
     * {@link #FADE_OUT} starts while-typing-fade-out animation.
     */
    void startWhileTypingAnimation(int fadeInOrOut);
    int FADE_IN = 0;
    int FADE_OUT = 1;
}
