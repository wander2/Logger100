package park.haneol.project.logger.util;

import android.content.Context;
import android.view.MenuItem;
import android.view.View;

import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;

import park.haneol.project.logger.R;
import park.haneol.project.logger.component.MainActivity;

class PopupMenuManager {

    private MainActivity main;
    private PopupMenu popupMenu;

    PopupMenuManager(MainActivity main) {
        this.main = main;
    }

    void showPopupMenu(View anchor, final MenuFunction... menuFunctions) {
        showPopupMenu(main, anchor, menuFunctions);
    }

    void showPopupMenuDark(View anchor, final MenuFunction... menuFunctions) {
        Context wrapper = new ContextThemeWrapper(main, R.style.DarkPopupMenuTheme);
        showPopupMenu(wrapper, anchor, menuFunctions);
    }

    private void showPopupMenu(Context context, View anchor, final MenuFunction... menuFunctions) {
        clearPopupMenu();
        popupMenu = new PopupMenu(context, anchor);
        for (int i = 0; i < menuFunctions.length; i++) {
            popupMenu.getMenu().add(0, i, i, menuFunctions[i].titleRes);
        }
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                for (int i = 0; i < menuFunctions.length; i++) {
                    if (i == item.getItemId()) {
                        menuFunctions[i].function.run();
                    }
                }
                return false;
            }
        });
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {
                popupMenu = null;
            }
        });
        popupMenu.show();
    }

    private void clearPopupMenu() {
        if (popupMenu != null) {
            popupMenu.dismiss();
            popupMenu = null;
        }
    }

    static class MenuFunction {
        int titleRes;
        Runnable function;
    }

}
