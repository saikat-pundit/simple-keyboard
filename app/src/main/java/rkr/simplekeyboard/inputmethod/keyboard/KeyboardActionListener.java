package rkr.simplekeyboard.inputmethod.keyboard;
import rkr.simplekeyboard.inputmethod.latin.common.Constants;
public interface KeyboardActionListener {
        void onPressKey(int primaryCode, int repeatCount, boolean isSinglePointer);
        void onReleaseKey(int primaryCode, boolean withSliding);
    void onCodeInput(int primaryCode, int x, int y, boolean isKeyRepeat);
        void onTextInput(final String rawText);
        void onFinishSlidingInput();
        boolean onCustomRequest(int requestCode);
    void onMoveCursorPointer(int steps);
    void onMoveDeletePointer(int steps);
    void onUpWithDeletePointerActive();
    void onUpWithSpacePointerActive();
    KeyboardActionListener EMPTY_LISTENER = new Adapter();
    class Adapter implements KeyboardActionListener {
        @Override
        public void onPressKey(int primaryCode, int repeatCount, boolean isSinglePointer) {}
        @Override
        public void onReleaseKey(int primaryCode, boolean withSliding) {}
        @Override
        public void onCodeInput(int primaryCode, int x, int y, boolean isKeyRepeat) {}
        @Override
        public void onTextInput(String text) {}
        @Override
        public void onFinishSlidingInput() {}
        @Override
        public boolean onCustomRequest(int requestCode) {
            return false;
        }
        @Override
        public void onMoveCursorPointer(int steps) {}
        @Override
        public void onMoveDeletePointer(int steps) {}
        @Override
        public void onUpWithDeletePointerActive() {}
        @Override
        public void onUpWithSpacePointerActive() {}
    }
}
