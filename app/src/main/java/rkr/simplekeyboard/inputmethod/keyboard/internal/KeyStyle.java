package rkr.simplekeyboard.inputmethod.keyboard.internal;
import android.content.res.TypedArray;
public abstract class KeyStyle {
    private final KeyboardTextsSet mTextsSet;
    public abstract String[] getStringArray(TypedArray a, int index);
    public abstract String getString(TypedArray a, int index);
    public abstract int getInt(TypedArray a, int index, int defaultValue);
    public abstract int getFlags(TypedArray a, int index);
    protected KeyStyle(final KeyboardTextsSet textsSet) {
        mTextsSet = textsSet;
    }
    protected String parseString(final TypedArray a, final int index) {
        if (a.hasValue(index)) {
            return mTextsSet.resolveTextReference(a.getString(index));
        }
        return null;
    }
    protected String[] parseStringArray(final TypedArray a, final int index) {
        if (a.hasValue(index)) {
            final String text = mTextsSet.resolveTextReference(a.getString(index));
            return MoreKeySpec.splitKeySpecs(text);
        }
        return null;
    }
}
