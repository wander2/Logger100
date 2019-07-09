package park.haneol.project.logger.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.view.ContextThemeWrapper;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import park.haneol.project.logger.R;
import park.haneol.project.logger.item.BaseItem;
import park.haneol.project.logger.item.DateItem;
import park.haneol.project.logger.item.LogItem;
import park.haneol.project.logger.recyclerview.DataAdapter;
import park.haneol.project.logger.recyclerview.RecView;
import park.haneol.project.logger.view.RootLayout;

public class ActionManager {

    private Context context;

    private RootLayout rootLayout;
    private RecView recView;
    private EditText editText;
    private ImageButton undoButton;

    private DataAdapter adapter;
    private Database database;

    private PopupMenu popupMenu;

    private boolean undoDateRemoved;
    private int undoPosition;
    private LogItem undoItem;

    private ThemeAnimation themeAnimation;

    private static List<Integer> UTC_INT = Arrays.asList(
            -720, -660, -600, -570, -540, -480, -420, -360,
            -300, -240, -210, -180, -150, -120, -60, 0,
            +60, +120, +180, +210, +240, +270, +300, +330,
            +345, +360, +390, +420, +480, +525, +540, +570,
            +600, +630, +660, +690, +720, +765, +780, +825,
            +840);

    private static CharSequence[] UTC_STRING = {"UTC -12",
            "UTC -11", "UTC -10", "UTC -9:30", "UTC -9",
            "UTC -8", "UTC -7", "UTC -6", "UTC -5",
            "UTC -4", "UTC -3:30", "UTC -3", "UTC -2:30",
            "UTC -2", "UTC -1", "UTC 0", "UTC +1",
            "UTC +2", "UTC +3", "UTC +3:30", "UTC +4",
            "UTC +4:30", "UTC +5", "UTC +5:30", "UTC +5:45",
            "UTC +6", "UTC +6:30", "UTC +7", "UTC +8",
            "UTC +8:45", "UTC +9", "UTC +9:30", "UTC +10",
            "UTC +10:30", "UTC +11", "UTC +11:30", "UTC +12",
            "UTC +12:45", "UTC +13", "UTC +13:45", "UTC +14"};

    public ActionManager(RootLayout rootLayout, Database database) {
        this.context = rootLayout.getContext();
        this.rootLayout = rootLayout;
        this.recView = rootLayout.findViewById(R.id.rec_view);
        this.editText = rootLayout.findViewById(R.id.edit_text);
        ImageButton menuButton = rootLayout.findViewById(R.id.menu_button);
        ImageButton themeButton = rootLayout.findViewById(R.id.theme_button);
        this.undoButton = rootLayout.findViewById(R.id.undo_button);
        this.adapter = (DataAdapter) recView.getAdapter();
        this.database = database;
        this.themeAnimation = new ThemeAnimation(rootLayout, recView, editText, menuButton, themeButton, undoButton);
    }





    public void onClickLogItem(final int position, View anchor) {
        final LogItem item = (LogItem) adapter.getItemAt(position);
        if (item == null) {
            return;
        }
        final int ID_EDIT = 1;
        final int ID_REMOVE = 2;
        clearPopupMenu();
        popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenu().add(0, ID_EDIT, ID_EDIT, R.string.edit);
        popupMenu.getMenu().add(0, ID_REMOVE, ID_REMOVE, R.string.remove);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case ID_EDIT:
                        onClickEdit(item, position);
                        return true;
                    case ID_REMOVE:
                        onClickRemove(item, position);
                        return true;
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

    private void onClickEdit(final LogItem item, final int position) {
        final boolean keypadShown = UIUtil.keypadShown;

        // Make Content View
        final EditText popupEditText = new EditText(context);
        popupEditText.setText(item.getText());
        popupEditText.setSelection(item.getText().length());
        popupEditText.requestFocus();
        popupEditText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Make Dialog
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.edit)
                .setView(popupEditText)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!keypadShown) {
                            UIUtil.hideSoftInput(popupEditText);
                        }
                    }
                })
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 작동 조건
                        String text = popupEditText.getText().toString();
                        if (text.length() > 0) {
                            onEditConfirm(item, position, text);
                        }
                        if (!keypadShown) {
                            UIUtil.hideSoftInput(popupEditText);
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        UIUtil.isPopupEditing = false;
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);

        // 키패드 올리기
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        dialog.show();
        UIUtil.isPopupEditing = true;
    }

    private void onClickRemove(LogItem item, int position) {
        undoDateRemoved = adapter.removeItem(position);
        undoPosition = position;
        undoItem = item;
        database.delete(item.getId());
        undoButton.setVisibility(View.VISIBLE);
    }

    private void onEditConfirm(LogItem item, final int position, String text) {
        item.setText(text);
        adapter.notifyItemChanged(position);
        database.updateText(item.getId(), text);
        recView.startBlinkAnimation(position);

        // 수정 후 화면 밖으로 나가는 문제 해결
        recView.postDelayed(new Runnable() {
            @Override
            public void run() {
                RecyclerView.ViewHolder holder = recView.findViewHolderForAdapterPosition(position);
                if (holder != null) {
                    int top = holder.itemView.getTop();
                    int bot = holder.itemView.getBottom();
                    int size = holder.itemView.getHeight();
                    int height = recView.getHeight();
                    LinearLayoutManager layoutManager = (LinearLayoutManager) recView.getLayoutManager();
                    if (size > height) {
                        if (top < 0 && bot < height) {
                            if (layoutManager != null) {
                                layoutManager.scrollToPositionWithOffset(position, height-size);
                            }
                        } else if (top > 0 && bot > height) {
                            recView.scrollToPosition(position);
                        }
                    } else {
                        if (top < 0 && bot < height) {
                            recView.scrollToPosition(position);
                        } else if (top > 0 && bot > height) {
                            if (layoutManager != null) {
                                layoutManager.scrollToPositionWithOffset(position, height-size);
                            }
                        }
                    }
                }
            }
        }, 100);
    }









    public void onLongClickLogItem(final int position, View anchor) {
        final LogItem item = (LogItem) adapter.getItemAt(position);
        if (item == null) {
            return;
        }
        final int ID_HIGHLIGHT = 1;
        final int ID_COPY = 2;
        final int ID_SHARE = 3;
        clearPopupMenu();
        Context wrapper = new ContextThemeWrapper(context, R.style.DarkPopupMenuTheme);
        popupMenu = new PopupMenu(wrapper, anchor);
        popupMenu.getMenu().add(0, ID_COPY, ID_COPY, R.string.copy);
        popupMenu.getMenu().add(0, ID_SHARE, ID_SHARE, R.string.share);
        popupMenu.getMenu().add(0, ID_HIGHLIGHT, ID_HIGHLIGHT, item.getFlag() == 1 ? R.string.remove_highlight : R.string.highlight);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case ID_COPY:
                        onClickCopy(item.getText());
                        return true;
                    case ID_SHARE:
                        onClickShare(item.getText());
                        return true;
                    case ID_HIGHLIGHT:
                        onClickHighlight(item, position);
                        return true;
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

    private void onClickCopy(String text) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("content", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, context.getString(R.string.copy_message), Toast.LENGTH_SHORT).show();
        }
    }

    private void onClickShare(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        context.startActivity(Intent.createChooser(intent, "공유하기"));
    }

    private void onClickHighlight(LogItem item, int position) {
        item.setFlag(item.getFlag() == 1 ? 0 : 1);
        adapter.notifyItemChanged(position);
        database.updateFlag(item.getId(), item.getFlag());
    }











    public void onLongClickDateItem(final int position, View anchor) {
        final DateItem item = (DateItem) adapter.getItemAt(position);
        if (item == null) {
            return;
        }
        final int ID_REMOVE = 1;
        final int ID_COPY = 2;
        final int ID_SHARE = 3;
        clearPopupMenu();
        Context wrapper = new ContextThemeWrapper(context, R.style.DarkPopupMenuTheme);
        popupMenu = new PopupMenu(wrapper, anchor);
        popupMenu.getMenu().add(0, ID_REMOVE, ID_REMOVE, R.string.remove);
        popupMenu.getMenu().add(0, ID_COPY, ID_COPY, R.string.copy);
        popupMenu.getMenu().add(0, ID_SHARE, ID_SHARE, R.string.share);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case ID_REMOVE:
                        onClickDateRemove(position);
                        return true;
                    case ID_COPY:
                        onClickCopy(getDateSpan(item, position + 1));
                        return true;
                    case ID_SHARE:
                        onClickShare(getDateSpan(item, position + 1));
                        return true;
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

    private void onClickDateRemove(final int position) {
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.remove)
                .setMessage(context.getString(R.string.remove_date_message))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<Integer> idList = adapter.removeDate(position);
                        database.deleteSpan(idList);
                        clearUndoButton();
                    }
                })
                .create();
        dialog.show();
    }

    private String getDateSpan(DateItem dateItem, int position) {
        StringBuilder sb = new StringBuilder();
        sb.append(dateItem.getDateString());
        BaseItem item;
        while (true) {
            item = adapter.getItemAt(position);
            if (!(item instanceof LogItem)) {
                break;
            }
            sb.append("\r\n");
            sb.append(((LogItem) item).getTimeString());
            sb.append(((LogItem) item).getText().replace("\n", "\r\n      "));
            position++;
        }
        return sb.toString();
    }














    public void onClickSaveButton() {
        LogItem item = null;
        String text = editText.getText().toString();
        if (text.length() > 0) {
            item = database.insert(text);
            adapter.addItem(item);
            editText.getText().clear();
        }
        recView.scrollDown();
        if (undoItem != null && item != null) {
            if (undoItem.getId() == item.getId()) {
                clearUndoButton();
            }
        }
    }

    public void onClickMenu(View anchor) {
        clearPopupMenu();
        popupMenu = new PopupMenu(context, anchor);
        popupMenu.getMenu().add(0, 0, 0, R.string.setting);
        popupMenu.getMenu().add(0, 1, 1, R.string.backup);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case 0:
                        onClickSetting();
                        return true;
                    case 1:
                        onClickBackup();
                        return true;
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

    private void onClickSetting() {
        final View contentView = LayoutInflater.from(context).inflate(R.layout.setting_layout, rootLayout, false);

        final Spinner spinner = contentView.findViewById(R.id.setting_timezone_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<>(context, android.R.layout.simple_spinner_item, UTC_STRING);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(UTC_INT.indexOf(PrefUtil.timeZoneOffset));

        if (Build.VERSION.SDK_INT >= 19) {
            CheckBox checkBox = contentView.findViewById(R.id.setting_allow_keypad_prediction);
            checkBox.setChecked(PrefUtil.onStartKeypad);
        }

        Button resetTimezoneButton = contentView.findViewById(R.id.setting_timezone_reset);
        resetTimezoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setSelection(UTC_INT.indexOf(PrefUtil.getDefaultTimeOffset()));
            }
        });

        final EditText editText = contentView.findViewById(R.id.setting_date_format);
        editText.setText(PrefUtil.dateFormat);
        editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(contentView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= 19) {
                            CheckBox checkBox = contentView.findViewById(R.id.setting_allow_keypad_prediction);
                            PrefUtil.setOnStartKeypad(context, checkBox.isChecked());
                        }

                        if (!editText.getText().toString().equals(PrefUtil.dateFormat)) {
                            PrefUtil.setDateFormat(context, editText.getText().toString());
                            adapter.notifyDataSetChanged();
                        }

                        int offset = UTC_INT.get(spinner.getSelectedItemPosition());
                        if (offset != PrefUtil.timeZoneOffset) {
                            PrefUtil.setTimeOffset(context, offset);
                            adapter.setItemList(database.load());
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        UIUtil.isPopupEditing = false;
                    }
                })
                .create();
        dialog.show();
        UIUtil.isPopupEditing = true;
    }

    private void onClickBackup() {
        final String path = context.getString(R.string.app_name) + " (" + TimeUtil.getDefaultDateFormat(TimeUtil.getCurrentTime()) + ").txt";
        AlertDialog dialog = new AlertDialog.Builder(context)
                .setTitle(R.string.backup)
                .setMessage(context.getString(R.string.backup_message))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        exportFullBackupFile(path);
                    }
                })
                .create();
        dialog.show();
    }

    public void onClickTheme() {
        PrefUtil.toggleThemeColorNumber(context);
        ColorUtil.themeToggled(context);
        themeAnimation.interStart = ColorUtil.currentInter;
        rootLayout.startAnimation(themeAnimation);
    }

    public void onClickUndo() {
        clearUndoButton();
        if (undoItem != null) {
            adapter.addItemAt(undoItem, undoPosition, undoDateRemoved);
            database.insertOfItem(undoItem);
            recView.scrollToItemPosition(undoPosition);
            undoItem = null;
            undoPosition = -1;
            undoDateRemoved = false;
        }
    }







    private void clearPopupMenu() {
        if (popupMenu != null) {
            popupMenu.dismiss();
            popupMenu = null;
        }
    }

    private void clearUndoButton() {
        undoButton.setVisibility(View.GONE);
    }

    private void exportFullBackupFile(String path) {
        File file = new File(context.getCacheDir(), path);
        try {
            FileWriter writer = new FileWriter(file);
            writer.append(TimeUtil.getUTC());
            for (int position = 1; position < adapter.getItemCount(); position++) {
                BaseItem item = adapter.getItemAt(position);
                if (item instanceof DateItem) {
                    writer.append("\r\n     ");
                    writer.append(((DateItem) item).getDateFormat());
                    writer.append("\r\n");
                    writer.flush();
                } else if (item instanceof LogItem) {
                    writer.append(((LogItem) item).getTimeString());
                    writer.append(((LogItem) item).getText().replace("\n", "\r\n      "));
                    writer.append("\r\n");
                    writer.flush();
                }
            }
            writer.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        Uri uri = FileProvider.getUriForFile(context, context.getPackageName()+".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/txt");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        context.startActivity(Intent.createChooser(intent, context.getString(R.string.backup)));
    }






    private class ThemeAnimation extends Animation implements Animation.AnimationListener {

        private RootLayout rootLayout;
        private EditText editText;
        private RecView recView;
        private ImageButton menuButton;
        private ImageButton themeButton;
        private ImageButton undoButton;

        private float interStart;

        ThemeAnimation(RootLayout rootLayout, RecView recView, EditText editText,
                       ImageButton menuButton, ImageButton themeButton, ImageButton undoButton) {
            this.rootLayout = rootLayout;
            this.recView = recView;
            this.editText = editText;
            this.menuButton = menuButton;
            this.themeButton = themeButton;
            this.undoButton = undoButton;
            setDuration(200);
            setInterpolator(new LinearInterpolator());
            setAnimationListener(this);
        }

        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        protected void applyTransformation(float t, Transformation transformation) {
            ColorUtil.applyInter(getInter(t));
            ColorUtil.applyColor(rootLayout, recView, editText, menuButton, themeButton, undoButton);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            adapter.notifyDataSetChanged();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}

        private float getInter(float t) {
            return interStart + (t * ((float) PrefUtil.themeNumber - interStart));
        }
    }
}
