package rkr.simplekeyboard.inputmethod.keyboard.internal;
import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.widget.RelativeLayout;
import rkr.simplekeyboard.inputmethod.latin.common.CoordinateUtils;
public final class DrawingPreviewPlacerView extends RelativeLayout {
    private final int[] mKeyboardViewOrigin = CoordinateUtils.newInstance();
    public DrawingPreviewPlacerView(final Context context, final AttributeSet attrs) {
        super(context, attrs);
        setWillNotDraw(false);
    }
    public void setKeyboardViewGeometry(final int[] originCoords) {
        CoordinateUtils.copy(mKeyboardViewOrigin, originCoords);
    }
    @Override
    public void onDraw(final Canvas canvas) {
        super.onDraw(canvas);
        final int originX = CoordinateUtils.x(mKeyboardViewOrigin);
        final int originY = CoordinateUtils.y(mKeyboardViewOrigin);
        canvas.translate(originX, originY);
        canvas.translate(-originX, -originY);
    }
}
