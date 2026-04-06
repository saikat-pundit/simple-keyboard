package rkr.simplekeyboard.inputmethod.event;
import rkr.simplekeyboard.inputmethod.latin.common.StringUtils;
public class Event {
    final public static int EVENT_TYPE_NOT_HANDLED = 0;
    final public static int EVENT_TYPE_INPUT_KEYPRESS = 1;
    final public static int EVENT_TYPE_TOGGLE = 2;
    final public static int EVENT_TYPE_MODE_KEY = 3;
    final public static int EVENT_TYPE_SOFTWARE_GENERATED_STRING = 6;
    final public static int EVENT_TYPE_CURSOR_MOVE = 7;
    final public static int NOT_A_CODE_POINT = -1;
    final public static int NOT_A_KEY_CODE = 0;
    final private static int FLAG_NONE = 0;
    final private static int FLAG_REPEAT = 0x2;
    final private static int FLAG_CONSUMED = 0x4;
    final private int mEventType; 
    final public int mCodePoint;
    final public CharSequence mText;
    final public int mKeyCode;
    final private int mFlags;
    final public Event mNextEvent;
    private Event(final int type, final CharSequence text, final int codePoint, final int keyCode,
            final int flags, final Event next) {
        mEventType = type;
        mText = text;
        mCodePoint = codePoint;
        mKeyCode = keyCode;
        mFlags = flags;
        mNextEvent = next;
    }
    public static Event createSoftwareKeypressEvent(final int codePoint, final int keyCode,
            final boolean isKeyRepeat) {
        return new Event(EVENT_TYPE_INPUT_KEYPRESS, null, codePoint, keyCode,
                isKeyRepeat ? FLAG_REPEAT : FLAG_NONE, null);
    }
        public static Event createSoftwareTextEvent(final CharSequence text, final int keyCode) {
        return new Event(EVENT_TYPE_SOFTWARE_GENERATED_STRING, text, NOT_A_CODE_POINT, keyCode,
                FLAG_NONE, null );
    }
    public boolean isFunctionalKeyEvent() {
        return NOT_A_CODE_POINT == mCodePoint;
    }
    public boolean isKeyRepeat() {
        return 0 != (FLAG_REPEAT & mFlags);
    }
    public boolean isConsumed() { return 0 != (FLAG_CONSUMED & mFlags); }
    public CharSequence getTextToCommit() {
        if (isConsumed()) {
            return ""; 
        }
        switch (mEventType) {
        case EVENT_TYPE_MODE_KEY:
        case EVENT_TYPE_NOT_HANDLED:
        case EVENT_TYPE_TOGGLE:
        case EVENT_TYPE_CURSOR_MOVE:
            return "";
        case EVENT_TYPE_INPUT_KEYPRESS:
            return StringUtils.newSingleCodePointString(mCodePoint);
        case EVENT_TYPE_SOFTWARE_GENERATED_STRING:
            return mText;
        }
        throw new RuntimeException("Unknown event type: " + mEventType);
    }
}
