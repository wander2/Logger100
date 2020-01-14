package park.haneol.project.logger.util;

import android.app.DatePickerDialog;
import android.app.PendingIntent;
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
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.core.content.FileProvider;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import park.haneol.project.logger.R;
import park.haneol.project.logger.component.HiddenActivity;
import park.haneol.project.logger.component.MainActivity;
import park.haneol.project.logger.component.ShortcutActivity;
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
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        int[] titleRes = {
                item.getFlag() == 1 ? R.string.remove_highlight : R.string.highlight,
                R.string.edit,
                R.string.remove
        };
        popupMenuManager.showPopupMenu(anchor, titleRes, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case 0:
                        onClickHighlight(position);
                        return true;
                    case 1:
                        onClickEdit(position);
                        return true;
                    case 2:
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
        if (main instanceof HiddenActivity) {
            int[] titleRes = {
                    R.string.copy,
                    R.string.move_hidden_return

            };
            popupMenuManager.showPopupMenuDark(anchor, titleRes, new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem) {
                    switch (menuItem.getItemId()) {
                        case 0:
                            copy(item.getText());
                            return true;
                        case 1:
                            onClickHideReturn(position);
                            return true;
                    }
                    return false;
                }
            });
        } else {
            int[] titleRes = {
                    R.string.copy,
                    R.string.share,
                    R.string.move_hidden

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
                            onClickHide(position);
                            return true;
                    }
                    return false;
                }
            });
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onLongClickDateItem(final int position, View anchor) {
        final DateItem item = (DateItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        int[] titleRes = {
                R.string.copy_all,
                R.string.share_all,
                R.string.remove_all
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
                R.string.backup,
                R.string.shortcut,
                (main instanceof HiddenActivity) ? R.string.change_password : R.string.hidden
        };
        popupMenuManager.showPopupMenu(anchor, titleRes, new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case 0:
                        onClickSetting();
                        return true;
                    case 1:
                        exportFullBackupFile();
                        return true;
                    case 2:
                        onClickCreateShortcut();
                        return true;
                    case 3:
                        if (main instanceof HiddenActivity) {
                            onClickChangePassword();
                        } else {
                            onClickHidden();
                        }
                        return true;
                }
                return false;
            }
        });
    }

    public void onClickTime(int position) {
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }

        // 변수
        int dayMinutes = TimeUtil.getLocalDayMinutes(item.getTime());
        final int[] date = TimeUtil.getEach(item.getTime()); // 날짜 정보 저장 변수
        final int[] hm = {dayMinutes / 60, dayMinutes % 60}; // 시간 정보 저장 변수

        // 뷰
        final View contentView = LayoutInflater.from(main).inflate(R.layout.time_change_layout, main.mRootLayout, false);
        final Button dateChangeButton = contentView.findViewById(R.id.button_change_date);
        final TimePicker timePicker = contentView.findViewById(R.id.picker_change_time);

        // 날짜 변경 버튼
        // 텍스트 초기화
        dateChangeButton.setText(TimeUtil.getDateString(item.getTime()));
        // 버튼 클릭시 나타날 것
        final DatePickerDialog datePickerDialog = new DatePickerDialog(main,
                new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        // 입력정보 -> 새 변수
                        int days = TimeUtil.getDays(year, month + 1, dayOfMonth);
                        int[] afterDate = TimeUtil.getEachFromDays(days);

                        // 변수에 저장
                        System.arraycopy(afterDate, 0, date, 0, 4);

                        // 버튼 텍스트 변경
                        dateChangeButton.setText(TimeUtil.getDateStringFromDays(days));
                    }
                },
                date[0],date[1] - 1, date[2]);
        // 최소 1970. 1. 2. (UTC 변경으로 인한 오류 방지) / 최대 현재시간
        datePickerDialog.getDatePicker().setMinDate(86400000);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        // 클릭 이벤트 적용
        dateChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePickerDialog.show();
            }
        });

        // 시간 선택기
        // 24시간 설정
        timePicker.setIs24HourView(true);
        // 초기화
        timePicker.setCurrentHour(hm[0]);
        timePicker.setCurrentMinute(hm[1]);
        // 변경 이벤트
        timePicker.setOnTimeChangedListener(new TimePicker.OnTimeChangedListener() {
            @Override
            public void onTimeChanged(TimePicker view, int hourOfDay, int minute) {
                // 시간 받아옴
                hm[0] = hourOfDay;
                hm[1] = minute;

                // 시간이 현재시간보다 클 경우 현재시간으로 설정됨
                int newTime = TimeUtil.toSystemTime(date, hm);
                int currentTime = TimeUtil.getCurrentTime();
                if (newTime > currentTime) {
                    int currentDayMinutes = TimeUtil.getLocalDayMinutes(currentTime);
                    hm[0] = currentDayMinutes / 60;
                    hm[1] = currentDayMinutes % 60;
                    view.setCurrentHour(hm[0]);
                    view.setCurrentMinute(hm[1]);
                }
            }
        });

        // 다이얼로그
        new AlertDialog.Builder(main)
                .setTitle(R.string.change_time)
                .setView(contentView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        int afterTime = TimeUtil.toSystemTime(date, hm);
                        int[] betweenIds = main.mAdapter.timeInsertFindBetween(afterTime);
                        int afterId = betweenIds[0] + 1;
                        int pushUntilId = main.mAdapter.timeInsertFindPushUntil(betweenIds);
                        main.mDatabase.changeItemTime(item.getId(), afterTime, afterId, pushUntilId);
                        main.mAdapter.setItemList(main.mDatabase.load());
                        main.mAdapter.update(main.isSearchMode);
                        main.mRecView.scrollToItemPosition(main.mAdapter.getPositionById(afterId));
                        undoItem.clear();
                    }
                })
                .show();
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
                        UIUtil.hideSoftInput(editDialogView);
                        UIUtil.hideSoftInput(main.mInputText);
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
                        UIUtil.hideSoftInput(editDialogView);
                        UIUtil.hideSoftInput(main.mInputText);
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

        // 타임존
        final Spinner spinner = contentView.findViewById(R.id.setting_timezone_spinner);
        ArrayAdapter<CharSequence> spinnerAdapter = new ArrayAdapter<>(main, android.R.layout.simple_spinner_item, UTC_STRING);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
        spinner.setSelection(UTC_INT.indexOf(PrefUtil.timeZoneOffset));

        // 타임존 버튼
        Button resetTimezoneButton = contentView.findViewById(R.id.setting_timezone_reset);
        resetTimezoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinner.setSelection(UTC_INT.indexOf(PrefUtil.getDefaultTimeOffset()));
            }
        });

        // 날짜 형식 버튼
        Button dateFormatChangeButton = contentView.findViewById(R.id.setting_button_date_format_change);
        dateFormatChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onClickDateFormatChange();
            }
        });

        // 키패드 예측
        if (Build.VERSION.SDK_INT >= 19) {
            CheckBox keypadPrediction = contentView.findViewById(R.id.setting_allow_keypad_prediction);
            keypadPrediction.setChecked(PrefUtil.onStartKeypad);
        }

        // 화면 보안
        final CheckBox screenSecure = contentView.findViewById(R.id.setting_secure);
        screenSecure.setChecked(PrefUtil.getIsScreenSecure(main));

        // 라벨 구분자
        final EditText editTextLabelLeft = contentView.findViewById(R.id.setting_label_separator_left);
        editTextLabelLeft.setText(PrefUtil.getLabelSeparatorLeft(main));
        final EditText editTextLabelRight = contentView.findViewById(R.id.setting_label_separator_right);
        editTextLabelRight.setText(PrefUtil.getLabelSeparatorRight(main));

        // 다이얼로그
        AlertDialog dialog = new AlertDialog.Builder(main)
                .setView(contentView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 타임존
                        int offset = UTC_INT.get(spinner.getSelectedItemPosition());
                        if (offset != PrefUtil.timeZoneOffset) {
                            PrefUtil.setTimeOffset(main, offset);
                            main.mAdapter.setItemList(main.mDatabase.load());
                            main.mAdapter.update(main.isSearchMode);
                            main.mRecView.scrollDown();
                            undoItem.clear();
                        }

                        // 키패드 예측
                        if (Build.VERSION.SDK_INT >= 19) {
                            CheckBox checkBox = contentView.findViewById(R.id.setting_allow_keypad_prediction);
                            PrefUtil.setOnStartKeypad(main, checkBox.isChecked());
                        }

                        // 화면 보안
                        PrefUtil.setScreenSecure(main, screenSecure.isChecked());
                        if (screenSecure.isChecked()) {
                            main.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                        } else {
                            main.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                        }

                        // 라벨 구분자
                        PrefUtil.setLabelSeparatorLeft(main, editTextLabelLeft.getText().toString());
                        PrefUtil.setLabelSeparatorRight(main, editTextLabelRight.getText().toString());
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

    private void onClickDateFormatChange() {
        final View contentView = LayoutInflater.from(main).inflate(R.layout.setting_layout_date_format, main.mRootLayout, false);

        // 날짜 형식
        final EditText editText = contentView.findViewById(R.id.setting_date_format);
        editText.setText(PrefUtil.dateFormat);

        // 다이얼로그
        AlertDialog dialog = new AlertDialog.Builder(main)
                .setView(contentView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 날짜 형식
                        if (!editText.getText().toString().equals(PrefUtil.dateFormat)) {
                            PrefUtil.setDateFormat(main, editText.getText().toString());
                            main.mAdapter.notifyDataSetChanged();
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

    private void onClickCreateShortcut() {
        // Make Content View
        final EditText editText = new EditText(main);
        editText.setHint(R.string.hint_label);
        editText.requestFocus();

        // Make Dialog
        AlertDialog dialog = new AlertDialog.Builder(main)
                .setTitle(R.string.shortcut)
                .setView(editText)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        UIUtil.hideSoftInput(editText);
                        UIUtil.hideSoftInput(main.mInputText);
                    }
                })
                .setPositiveButton(R.string.create, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 확인시 작동
                        createShortcut(editText.getText().toString());
                        UIUtil.hideSoftInput(editText);
                        UIUtil.hideSoftInput(main.mInputText);
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        UIUtil.isPopupEditing = false;
                    }
                })
                .create();

        // 키패드 올리기
        if (dialog.getWindow() != null) {
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }

        dialog.show();
        UIUtil.isPopupEditing = true;
    }

    private void createShortcut(String label) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(main)) {
            String shortLabel = label.length() >= 10 ? label.substring(0, 10) : label; // 10자
            String longLabel = label.length() >= 25 ? label.substring(0, 25) : label; // 25자
            Intent intent = new Intent(main, ShortcutActivity.class);
            intent.setAction(Intent.ACTION_DEFAULT);
            intent.putExtra(ShortcutActivity.EXTRA_SHORTCUT_LABEL, label);
            intent.addFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
            if (label.length() == 0) {
                shortLabel = main.getString(R.string.shortcut_name);
                longLabel = main.getString(R.string.shortcut_name);
            }
            ShortcutInfoCompat pinShortcutInfo = new ShortcutInfoCompat.Builder(main, "logger_shortcut_"+label)
                    .setShortLabel(shortLabel)
                    .setLongLabel(longLabel)
                    .setIcon(IconCompat.createWithResource(main, R.mipmap.ic_shortcut))
                    .setIntent(intent)
                    .build();
            Intent pinnedShortcutCallbackIntent = ShortcutManagerCompat.createShortcutResultIntent(main, pinShortcutInfo);
            PendingIntent successCallback = PendingIntent.getBroadcast(main, 0, pinnedShortcutCallbackIntent, 0);
            ShortcutManagerCompat.requestPinShortcut(main, pinShortcutInfo, successCallback.getIntentSender());
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private void onClickHidden() {
        main.startActivity(new Intent(main, HiddenActivity.class));
        main.finish();
    }

    private void onClickChangePassword() {
        if (HiddenActivity.mode != 2) {
            Toast.makeText(main, R.string.msg_password_first, Toast.LENGTH_SHORT).show();
        } else {
            // Make Content View
            final EditText editText = new EditText(main);
            editText.requestFocus();

            // Make Dialog
            final AlertDialog dialog = new AlertDialog.Builder(main)
                    .setTitle(R.string.change_password)
                    .setView(editText)
                    .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            UIUtil.hideSoftInput(editText);
                            UIUtil.hideSoftInput(main.mInputText);
                        }
                    })
                    .setPositiveButton(R.string.confirm, null)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            UIUtil.isPopupEditing = false;
                            UIUtil.hideSoftInput(editText);
                            UIUtil.hideSoftInput(main.mInputText);
                        }
                    })
                    .create();
            dialog.setCanceledOnTouchOutside(false);

            // 키패드 올리기
            if (dialog.getWindow() != null) {
                dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            }
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (editText.getText() != null) {
                        String afterPassword = editText.getText().toString();
                        onClickPasswordChangeConfirm(dialog, afterPassword);
                    }
                }
            });

            UIUtil.isPopupEditing = true;
        }
    }

    private void onClickPasswordChangeConfirm(final AlertDialog baseDialog, final String afterPassword) {
        AlertDialog dialog = new AlertDialog.Builder(main)
                .setTitle(R.string.change_password)
                .setMessage(main.getString(R.string.msg_password_changing, afterPassword))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PrefUtil.setHiddenPassword(main, afterPassword);
                        baseDialog.dismiss();
                        Toast.makeText(main, R.string.msg_password_changed, Toast.LENGTH_SHORT).show();
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

    private void onClickHide(int position) {
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        main.mAdapter.removeItem(position);
        main.mDatabase.delete(item.getId());
        Database databaseHidden = new Database(main, Database.DATABASE_NAME_HIDDEN);
        if (databaseHidden.existId(item.getId())) {
            databaseHidden.insert(item.getText());
        } else {
            databaseHidden.insertOfItem(item);
        }
    }

    private void onClickHideReturn(int position) {
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        main.mAdapter.removeItem(position);
        main.mDatabase.delete(item.getId());
        Database databaseDefault = new Database(main, Database.DATABASE_NAME);
        if (databaseDefault.existId(item.getId())) {
            databaseDefault.insert(item.getText());
        } else {
            databaseDefault.insertOfItem(item);
        }
    }









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

    private void exportFullBackupFile() {
        final String path = main.getString(R.string.app_name) + " (" + TimeUtil.getDefaultDateFormat(TimeUtil.getCurrentTime()) + ").txt";
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
