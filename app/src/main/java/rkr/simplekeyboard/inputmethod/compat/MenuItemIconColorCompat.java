package rkr.simplekeyboard.inputmethod.compat;

import android.app.ActionBar;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.util.ArrayList;

public class MenuItemIconColorCompat {
    /**
     * Set a menu item's icon to matching text color.
     * @param menuItem the menu item that should change colors.
     * @param actionBar target ActionBar.
     */
    public static void matchMenuIconColor(final View view, final MenuItem menuItem, final ActionBar actionBar) {
        ArrayList<View> views = new ArrayList<>();

        view.getRootView().findViewsWithText(views, actionBar.getTitle(), View.FIND_VIEWS_WITH_TEXT);
        if (views.size() == 1 && views.get(0) instanceof TextView) {
            int color = ((TextView) views.get(0)).getCurrentTextColor();
            setIconColor(menuItem, color);
        }
    }

    /**
     * Set a menu item's icon to specific color.
     * @param menuItem the menu item that should change colors.
     * @param color the color that the icon should be changed to.
     */
    private static void setIconColor(final MenuItem menuItem, final int color) {
        if (menuItem != null) {
            Drawable drawable = menuItem.getIcon();
            if (drawable != null) {
                drawable.mutate();
                drawable.setColorFilter(color, PorterDuff.Mode.SRC_ATOP);
            }
        }
    }
}
