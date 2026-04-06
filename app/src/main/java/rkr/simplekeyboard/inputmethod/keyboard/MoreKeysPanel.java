package rkr.simplekeyboard.inputmethod.keyboard;
import android.view.View;
import android.view.ViewGroup;
public interface MoreKeysPanel {
    interface Controller {
                void onShowMoreKeysPanel(final MoreKeysPanel panel);
                void onDismissMoreKeysPanel();
                void onCancelMoreKeysPanel();
    }
    Controller EMPTY_CONTROLLER = new Controller() {
        @Override
        public void onShowMoreKeysPanel(final MoreKeysPanel panel) {}
        @Override
        public void onDismissMoreKeysPanel() {}
        @Override
        public void onCancelMoreKeysPanel() {}
    };
    void showMoreKeysPanel(View parentView, Controller controller, int pointX,
                           int pointY, KeyboardActionListener listener);
        void dismissMoreKeysPanel();
        void onMoveEvent(final int x, final int y, final int pointerId);
        void onDownEvent(final int x, final int y, final int pointerId);
        void onUpEvent(final int x, final int y, final int pointerId);
        int translateX(int x);
        int translateY(int y);
        void showInParent(ViewGroup parentView);
        void removeFromParent();
        boolean isShowingInParent();
}
