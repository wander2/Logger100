package park.haneol.project.logger.util;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.LinearInterpolator;
import android.view.animation.Transformation;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
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
import park.haneol.project.logger.component.MainActivity;
import park.haneol.project.logger.item.BaseItem;
import park.haneol.project.logger.item.DateItem;
import park.haneol.project.logger.item.LogItem;

public class ActionManager {

    public EditText editDialogView;
    public int editPosition = -1;

    private MainActivity main;
    private PopupMenuManager popupMenuManager;

    private UndoItem undoItem = new UndoItem();
    private ThemeAnimation themeAnimation = new ThemeAnimation();

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

    public ActionManager(MainActivity main) {
        this.main = main;
        this.popupMenuManager = new PopupMenuManager(main);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClickLogItem(final int position, View anchor) {
        int[] titleRes = {
                R.string.edit,
                R.string.remove
        };
        popupMenuManager.showPopupMenu(anchor, titleRes, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case 0:
                        onClickEdit(position);
                        return true;
                    case 1:
                        onClickRemove(position);
                        return true;
                }
                return false;
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onLongClickLogItem(final int position, View anchor) {
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        int[] titleRes = {
                R.string.copy,
                R.string.share,
                item.getFlag() == 1 ? R.string.remove_highlight : R.string.highlight
        };
        popupMenuManager.showPopupMenuDark(anchor, titleRes, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case 0:
                        copy(item.getText());
                        return true;
                    case 1:
                        share(item.getText());
                        return true;
                    case 2:
                        onClickHighlight(position);
                        return true;
                }
                return false;
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onLongClickDateItem(final int position, View anchor) {
        final DateItem item = (DateItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        int[] titleRes = {
                R.string.copy,
                R.string.share,
                R.string.remove
        };
        popupMenuManager.showPopupMenuDark(anchor, titleRes, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case 0:
                        copy(getDateSpan(item, position + 1));
                        return true;
                    case 1:
                        share(getDateSpan(item, position + 1));
                        return true;
                    case 2:
                        onClickDateRemove(position);
                        return true;
                }
                return false;
            }
        });
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClickMenu(View anchor) {
        int[] titleRes = {
                R.string.setting,
                R.string.backup
        };
        popupMenuManager.showPopupMenu(anchor, titleRes, new PopupMenu.OnMenuItemClickListener() {
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
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////









    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClickEdit(final int position) {
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        final boolean keypadShown = UIUtil.keypadShown;

        // Make Content View
        editDialogView = new EditText(main);
        editDialogView.setText(item.getText());
        editDialogView.setSelection(item.getText().length());
        editDialogView.requestFocus();
        //contentView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // Make Dialog
        AlertDialog dialog = new AlertDialog.Builder(main)
                .setTitle(R.string.edit)
                .setView(editDialogView)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!keypadShown) {
                            UIUtil.hideSoftInput(editDialogView);
                            UIUtil.hideSoftInput(main.mInputText);
                        }
                    }
                })
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 작동 조건
                        String text = editDialogView.getText().toString();
                        if (text.length() > 0) {
                            onEditConfirm(item, position, text);
                        }
                        if (!keypadShown) {
                            UIUtil.hideSoftInput(editDialogView);
                            UIUtil.hideSoftInput(main.mInputText);
                        }
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        UIUtil.isPopupEditing = false;
                        editDialogView = null;
                        editPosition = -1;
                    }
                })
                .create();
        dialog.setCanceledOnTouchOutside(false);

        // 키패드 올리기
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        dialog.show();
        editPosition = position;
        UIUtil.isPopupEditing = true;
    }

    private void onEditConfirm(LogItem item, final int position, String text) {
        item.setText(text);
        main.mAdapter.notifyItemChanged(position);
        main.mDatabase.updateText(item.getId(), text);
        main.mRecView.startBlinkAnimation(position, true);

        // 수정 후 화면 밖으로 나가는 문제 해결
        main.mRecView.postDelayed(new Runnable() {
            @Override
            public void run() {
                RecyclerView.ViewHolder holder = main.mRecView.findViewHolderForAdapterPosition(position);
                if (holder != null) {
                    int top = holder.itemView.getTop();
                    int bot = holder.itemView.getBottom();
                    int size = holder.itemView.getHeight();
                    int height = main.mRecView.getHeight();
                    LinearLayoutManager layoutManager = (LinearLayoutManager) main.mRecView.getLayoutManager();
                    if (layoutManager != null) {
                        if (size > height) {
                            if (top < 0 && bot < height) {
                                layoutManager.scrollToPositionWithOffset(position, height/2-size);
                            } else if (top > 0 && bot > height) {
                                layoutManager.scrollToPositionWithOffset(position, height/2);
                            }
                        } else {
                            if (top < 0 || bot > height) {
                                layoutManager.scrollToPositionWithOffset(position, (height-size)/2);
                            }
                        }
                    }
                }
            }
        }, 100);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void onClickSetting() {
        final View contentView = LayoutInflater.from(main).inflate(R.layout.setting_layout, main.mRootLayout, false);

        final Spinner spinner = contentView.findViewById(R.id.setting_timezone_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<>(main, android.R.layout.simple_spinner_item, UTC_STRING);
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
        //editText.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        AlertDialog dialog = new AlertDialog.Builder(main)
                .setView(contentView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (Build.VERSION.SDK_INT >= 19) {
                            CheckBox checkBox = contentView.findViewById(R.id.setting_allow_keypad_prediction);
                            PrefUtil.setOnStartKeypad(main, checkBox.isChecked());
                        }

                        if (!editText.getText().toString().equals(PrefUtil.dateFormat)) {
                            PrefUtil.setDateFormat(main, editText.getText().toString());
                            main.mAdapter.notifyDataSetChanged();
                        }

                        int offset = UTC_INT.get(spinner.getSelectedItemPosition());
                        if (offset != PrefUtil.timeZoneOffset) {
                            PrefUtil.setTimeOffset(main, offset);
                            main.mAdapter.setItemList(main.mDatabase.load());
                            main.mAdapter.update(main.isSearchMode);
                            main.mRecView.scrollDown();
                            undoItem.clear();
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void onClickBackup() {
        final String path = main.getString(R.string.app_name) + " (" + TimeUtil.getDefaultDateFormat(TimeUtil.getCurrentTime()) + ").txt";
        AlertDialog dialog = new AlertDialog.Builder(main)
                .setTitle(R.string.backup)
                .setMessage(main.getString(R.string.backup_message))
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

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////















    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void onClickRemove(int position) {
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        main.mAdapter.removeItem(position);
        undoItem.set(item);
        main.mDatabase.delete(item.getId());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void onClickHighlight(int position) {
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        item.setFlag(item.getFlag() == 1 ? 0 : 1);
        main.mAdapter.notifyItemChanged(position);
        main.mDatabase.updateFlag(item.getId(), item.getFlag());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void onClickDateRemove(final int position) {
        final ArrayList<Integer> idList = main.mAdapter.getIdsInDate(position);
        AlertDialog dialog = new AlertDialog.Builder(main)
                .setTitle(R.string.remove)
                .setMessage(main.getString(R.string.remove_date_message, idList.size()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        main.mAdapter.removeItems(idList);
                        main.mDatabase.deleteItems(idList);
                        undoItem.clear();
                    }
                })
                .create();
        dialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClickSaveButton() {
        LogItem item = null;
        Editable text = main.mInputText.getText();
        if (text != null) {
            String string = text.toString();
            if (string.length() > 0) {
                item = main.mDatabase.insert(string);
                main.mAdapter.addItem(item);
                main.mInputText.getText().clear();
            }
            main.mRecView.scrollDown();
            if (undoItem.exist() && item != null) {
                if (undoItem.item.getId() == item.getId()) {
                    undoItem.clear();
                }
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClickUndo() {
        if (undoItem.exist()) {
            int position = main.mAdapter.restoreItem(undoItem.item);
            main.mDatabase.insertOfItem(undoItem.item);
            main.mRecView.scrollToItemPosition(position);
            undoItem.clear();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////










    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private String getDateSpan(DateItem dateItem, int position) {
        StringBuilder sb = new StringBuilder();
        sb.append(dateItem.getDateString());
        BaseItem item;
        while (true) {
            item = main.mAdapter.getItemAt(position);
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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClickTheme() {
        PrefUtil.toggleThemeColorNumber(main);
        ColorUtil.themeToggled(main);
        themeAnimation.interStart = ColorUtil.currentInter;
        main.mRootLayout.startAnimation(themeAnimation);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void exportFullBackupFile(String path) {
        File file = new File(main.getCacheDir(), path);
        try {
            FileWriter writer = new FileWriter(file);
            writer.append(TimeUtil.getUTC());
            for (int position = 1; position < main.mAdapter.getItemCount(); position++) {
                BaseItem item = main.mAdapter.getItemAt(position);
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
        Uri uri = FileProvider.getUriForFile(main, main.getPackageName()+".provider", file);
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/txt");
        intent.putExtra(Intent.EXTRA_STREAM, uri);
        main.startActivity(Intent.createChooser(intent, main.getString(R.string.backup)));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////














    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void copy(String text) {
        ClipboardManager clipboard = (ClipboardManager) main.getSystemService(Context.CLIPBOARD_SERVICE);
        if (clipboard != null) {
            ClipData clip = ClipData.newPlainText("content", text);
            clipboard.setPrimaryClip(clip);
            Toast.makeText(main, main.getString(R.string.copy_message), Toast.LENGTH_SHORT).show();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void share(String text) {
        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, text);
        main.startActivity(Intent.createChooser(intent, "공유하기"));
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
















    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class ThemeAnimation extends Animation implements Animation.AnimationListener {

        private float interStart;

        ThemeAnimation() {
            setDuration(200);
            setInterpolator(new LinearInterpolator());
            setAnimationListener(this);
        }

        @Override
        public void onAnimationStart(Animation animation) {}

        @Override
        protected void applyTransformation(float t, Transformation transformation) {
            ColorUtil.applyInter(getInter(t));
            ColorUtil.applyColor(main);
        }

        @Override
        public void onAnimationEnd(Animation animation) {
            main.mAdapter.notifyDataSetChanged();
        }

        @Override
        public void onAnimationRepeat(Animation animation) {}

        private float getInter(float t) {
            return interStart + (t * ((float) PrefUtil.themeNumber - interStart));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class UndoItem {
        LogItem item = null;
        void set(LogItem item) {
            this.item = item;
            main.mUndoButton.setVisibility(View.VISIBLE);
        }
        void clear() {
            main.mUndoButton.setVisibility(View.GONE);
            this.item = null;
        }
        boolean exist() {
            return item != null;
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////////////////////////////////////////////

}
