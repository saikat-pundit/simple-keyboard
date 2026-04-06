package rkr.simplekeyboard.inputmethod.latin.common;
public final class CoordinateUtils {
    private static final int INDEX_X = 0;
    private static final int INDEX_Y = 1;
    private static final int ELEMENT_SIZE = INDEX_Y + 1;
    private CoordinateUtils() {
    }
    public static int[] newInstance() {
        return new int[ELEMENT_SIZE];
    }
    public static int x(final int[] coords) {
        return coords[INDEX_X];
    }
    public static int y(final int[] coords) {
        return coords[INDEX_Y];
    }
    public static void set(final int[] coords, final int x, final int y) {
        coords[INDEX_X] = x;
        coords[INDEX_Y] = y;
    }
    public static void copy(final int[] destination, final int[] source) {
        destination[INDEX_X] = source[INDEX_X];
        destination[INDEX_Y] = source[INDEX_Y];
    }
}
