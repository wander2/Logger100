package park.haneol.project.logger.util;

import android.content.Context;
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

    void showPopupMenu(View anchor, int[] titleRes, PopupMenu.OnMenuItemClickListener listener) {
        showPopupMenu(main, anchor, titleRes, listener);
    }

    void showPopupMenuDark(View anchor, int[] titleRes, PopupMenu.OnMenuItemClickListener listener) {
        Context wrapper = new ContextThemeWrapper(main, R.style.DarkPopupMenuTheme);
        showPopupMenu(wrapper, anchor, titleRes, listener);
    }

    private void showPopupMenu(Context context, View anchor, int[] titleRes, PopupMenu.OnMenuItemClickListener listener) {
        clearPopupMenu();
        popupMenu = new PopupMenu(context, anchor);
        for (int i = 0; i < titleRes.length; i++) {
            popupMenu.getMenu().add(0, i, i, titleRes[i]);
        }
        popupMenu.setOnMenuItemClickListener(listener);
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

}
