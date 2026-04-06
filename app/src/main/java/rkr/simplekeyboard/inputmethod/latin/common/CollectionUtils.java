package rkr.simplekeyboard.inputmethod.latin.common;
import java.util.ArrayList;
public final class CollectionUtils {
    private CollectionUtils() {
    }
        public static <E> ArrayList<E> arrayAsList(final E[] array, final int start,
            final int end) {
        if (start < 0 || start > end || end > array.length) {
            throw new IllegalArgumentException("Invalid start: " + start + " end: " + end
                    + " with array.length: " + array.length);
        }
        final ArrayList<E> list = new ArrayList<>(end - start);
        for (int i = start; i < end; i++) {
            list.add(array[i]);
        }
        return list;
    }
}
