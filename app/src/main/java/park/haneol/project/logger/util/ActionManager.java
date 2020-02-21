package park.haneol.project.logger.util;

import android.app.PendingIntent;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.text.Editable;
import android.text.InputType;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
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
import park.haneol.project.logger.recyclerview.DataAdapter;

public class ActionManager {

    // 수정 기능에 사용됨
    public EditText editDialogView;
    public int editPosition = -1;

    // 필요 변수
    private MainActivity main;
    private PopupMenuManager popupMenuManager;

    // 사용 변수
    private UndoItem undoItem = new UndoItem();
    private ThemeAnimation themeAnimation = new ThemeAnimation();

    // 생성자
    public ActionManager(MainActivity main) {
        // 필요 변수
        this.main = main;
        this.popupMenuManager = new PopupMenuManager(main);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 아이템 클릭해서 메뉴 열리는 함수
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClickLogItem(final int position, View anchor) {
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        final String link = getLink(item.getText());
        if (link != null) {
            popupMenuManager.showPopupMenu(anchor,
                    new OpenLink(link),
                    new ToggleHighlight(position, item),
                    new ChangeItemText(position, item),
                    new RemoveItem(position, item)
            );
        } else {
            popupMenuManager.showPopupMenu(anchor,
                    new ToggleHighlight(position, item),
                    new ChangeItemText(position, item),
                    new RemoveItem(position, item)
            );
        }
    }

    private String getLink(String text) {
        String[] split = text.split("\n");
        for (String line: split) {
            line = line.trim();
            if (line.startsWith("http://") || line.startsWith("https://")) {
                return line;
            }
        }
        return null;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onLongClickLogItem(final int position, View anchor) {
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        if (main instanceof HiddenActivity) {
            popupMenuManager.showPopupMenuDark(anchor,
                    new CopyText(item.getText()),
                    new ShareText(item.getText()),
                    new RevealItem(position, item)
            );
        } else {
            popupMenuManager.showPopupMenuDark(anchor,
                    new CopyText(item.getText()),
                    new ShareText(item.getText()),
                    new HideItem(position, item)
            );
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onLongClickDateItem(final int position, View anchor) {
        final DateItem item = (DateItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        popupMenuManager.showPopupMenuDark(anchor,
                new CopyTextInDate(position, item),
                new ShareTextInDate(position, item),
                new RemoveItemsInDate(position)
        );
    }

    private String getTextInDate(int position, DateItem dateItem) {
        StringBuilder sb = new StringBuilder();
        sb.append(dateItem.getDateString());
        BaseItem item;
        while (true) {
            position += 1;
            item = main.mAdapter.getItemAt(position);
            if (!(item instanceof LogItem)) {
                break;
            }
            sb.append("\r\n");
            sb.append(((LogItem) item).getTimeString());
            sb.append(((LogItem) item).getText().replace("\n", "\r\n      "));
        }
        return sb.toString();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 메뉴 버튼 클릭
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClickMenu(View anchor) {
        if (main instanceof HiddenActivity) {
            popupMenuManager.showPopupMenuDark(anchor,
                    new OpenSetting(),
                    new ExportBackupFile(),
                    new CreateShortcut(),
                    new ChangePassword()
            );
        } else {
            popupMenuManager.showPopupMenu(anchor,
                    new OpenSetting(),
                    new ExportBackupFile(),
                    new CreateShortcut(),
                    new StartHiddenActivity()
            );
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////



















































    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 아이템 시간 클릭
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClickTime(final int position) {
        final LogItem item = (LogItem) main.mAdapter.getItemAt(position);
        if (item == null) {
            return;
        }
        showChangeItemTimeDialog(position, item);
    }

    private void showChangeItemTimeDialog(final int position, final LogItem item) {
        // 변수
        int dayMinutes = TimeUtil.getLocalDayMinutes(item.getTime());
        final int[] date = TimeUtil.getEach(item.getTime()); // 날짜 정보 저장 변수
        final int[] hm = {dayMinutes / 60, dayMinutes % 60}; // 시간 정보 저장 변수

        // 뷰
        final View contentView = LayoutInflater.from(main).inflate(R.layout.time_change_layout, main.mRootLayout, false);
        final DatePicker datePicker = contentView.findViewById(R.id.picker_change_date);
        final TimePicker timePicker = contentView.findViewById(R.id.picker_change_time);

        // 다이얼로그
        final AlertDialog dialog = new AlertDialog.Builder(main)
                .setTitle(TimeUtil.getDefaultDateFormat(item.getTime()) + " " + TimeUtil.getTimeString(item.getTime()))
                .setView(contentView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 시간
                        int toTime = TimeUtil.toSystemTime(date, hm);

                        // 현재 시간 넘으면 현재 시간으로
                        int currentTime = TimeUtil.getCurrentTime();
                        if (toTime > currentTime) {
                            toTime = currentTime;
                        }

                        changeItemTime(position, item, toTime);
                    }
                }).create();

        // 날짜 선택기
        datePicker.init(date[0], date[1] - 1, date[2], new DatePicker.OnDateChangedListener() {
            @Override
            public void onDateChanged(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                // 입력정보 -> 새 변수
                int days = TimeUtil.getDays(year, monthOfYear + 1, dayOfMonth);
                int[] afterDate = TimeUtil.getEachFromDays(days);

                // 변수에 저장
                System.arraycopy(afterDate, 0, date, 0, 4);

                // 최대 시간 넘는지
                int newTime = TimeUtil.toSystemTime(date, hm);
                int currentTime = TimeUtil.getCurrentTime();
                if (newTime > currentTime) {
                    // 타이틀에 최신 날짜로 표시
                    dialog.setTitle(blueString(TimeUtil.getDefaultDateFormat(currentTime) + " " + TimeUtil.getTimeString(currentTime)));
                } else {
                    // 다이얼로그 제목에 반영
                    dialog.setTitle(TimeUtil.getDefaultDateFormatFromEach(date) + " " + TimeUtil.getTimeString(hm[0], hm[1]));
                }
            }
        });
        // 최소 1970. 1. 2. (UTC 변경으로 인한 오류 방지) / 최대 현재시간
        datePicker.setMinDate(86400000);

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
                // 변수에 저장
                hm[0] = hourOfDay;
                hm[1] = minute;

                // 최대 시간 넘는지
                int newTime = TimeUtil.toSystemTime(date, hm);
                int currentTime = TimeUtil.getCurrentTime();
                if (newTime > currentTime) {
                    // 타이틀에 최신 날짜로 표시
                    dialog.setTitle(blueString(TimeUtil.getDefaultDateFormat(currentTime) + " " + TimeUtil.getTimeString(currentTime)));
                } else {
                    // 다이얼로그 제목에 반영
                    dialog.setTitle(TimeUtil.getDefaultDateFormatFromEach(date) + " " + TimeUtil.getTimeString(hm[0], hm[1]));
                }
            }
        });

        // 띄우기
        dialog.show();
    }

    private void changeItemTime(int position, LogItem item, int toTime) {
        int originalId = item.getId();
        int originalTime = item.getTime();

        // 지우고 삽입위치 탐색
        main.mAdapter.removeItem(position);
        int[] betweenIds = main.mAdapter.timeInsertFindBetween(toTime);

        // 삽입위치, 밀려나는 위치
        int toId = betweenIds[0] + 1;
        int pushUntilId = main.mAdapter.timeInsertFindPushUntil(betweenIds);

        // 데이터베이스
        if (pushUntilId == -1) {
            main.mDatabase.updateId(item.getId(), toId);
            main.mDatabase.updateTime(toId, toTime);
        } else {
            int pushCount = pushUntilId - toId;
            boolean res = main.mDatabase.pushId(item.getId(), toId, toId, pushCount);
            if (res) {
                main.mDatabase.updateTime(toId, toTime);
            }
        }

        // 리로드
        main.mAdapter.setItemList(main.mDatabase.load());
        main.mAdapter.update(main.isSearchMode);

        // 해당 위치로 스크롤
        main.mRecView.scrollToItemPosition(main.mAdapter.getPositionById(toId));

        // 되돌리기 TODO
        // 시간 변경 이전으로 되돌림
        // 되돌리기 이후 다시 해당 시간으로 변경하는 모드로 변경
        //undoItem.setMode(UNDO_MODE_RESTORE_TIME_CHANGE);
        //undoItem.setItem(item);
        //undoItem.setId(originalId);
        //undoItem.setTime(originalTime);
        //undoItem.setPosition(position);
        undoItem.clear();
    }

    private Spannable blueString(String string) {
        SpannableString spannable = new SpannableString(string);
        spannable.setSpan(new ForegroundColorSpan(Color.BLUE), 0, string.length(), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
        return spannable;
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////






















































    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 기본 버튼
    ////////////////////////////////////////////////////////////////////////////////////////////////

    public void onClickSaveButton() {
        LogItem item = null;
        Editable text = main.mInputText.getText();
        if (text != null) {
            String string = text.toString();
            if (string.length() > 0) {
                item = main.mDatabase.append(string, TimeUtil.getSaveTime());
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

    public void onClickTheme() {
        PrefUtil.toggleThemeColorNumber(main);
        ColorUtil.themeToggled(main);
        themeAnimation.interStart = ColorUtil.currentInter;
        main.mRootLayout.startAnimation(themeAnimation);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

















































    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 되돌리기
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class UndoItem {
        LogItem item = null;

        void set(LogItem item) {
            this.item = item;
            main.mUndoButton.setVisibility(View.VISIBLE);
        }

        // TODO : visible 관련 설정 필요없으면 제거
        void clear() {
            main.mUndoButton.setVisibility(View.GONE);
            this.item = null;
        }

        boolean exist() {
            return item != null;
        }
    }

    // TODO
    public void onClickUndo() {
        if (undoItem.exist()) {
            int position = main.mAdapter.restoreItem(undoItem.item);
            main.mDatabase.insertItem(undoItem.item);
            main.mRecView.scrollToItemPosition(position);

            // todo
            undoItem.clear();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////














































    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 아이템 종속 함수
    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class OpenLink extends PopupMenuManager.MenuFunction {
        OpenLink(final String link) {
            titleRes = R.string.open_link;
            function = new Runnable() {
                @Override
                public void run() {
                    openLink(link);
                }
            };
        }
    }

    private void openLink(String link) {
        if (link != null) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
            main.startActivity(intent);
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class RemoveItem extends PopupMenuManager.MenuFunction {
        RemoveItem(final int position, final LogItem item) {
            titleRes = R.string.remove;
            function = new Runnable() {
                @Override
                public void run() {
                    removeItem(position, item);
                }
            };
        }
    }

    private void removeItem(int position, LogItem item) {
        main.mAdapter.removeItem(position);
        undoItem.set(item);
        main.mDatabase.delete(item.getId());

        // 되돌리기 TODO
        // 제거된 아이템을 되돌림, 점유시 push
        // 되돌리기 후 다시 제거하는 모드로 변경
        //undoItem.setMode(UNDO_MODE_RESTORE_REMOVE);
        //undoItem.setItem(item);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class ToggleHighlight extends PopupMenuManager.MenuFunction {
        ToggleHighlight(final int position, final LogItem item) {
            titleRes = item.getFlag() == 1 ? R.string.remove_highlight : R.string.highlight;
            function = new Runnable() {
                @Override
                public void run() {
                    toggleHighlight(position, item);
                }
            };
        }
    }

    private void toggleHighlight(int position, LogItem item) {
        item.toggleHighlight();
        main.mAdapter.notifyItemChanged(position);
        main.mDatabase.updateFlag(item.getId(), item.getFlag());
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class ChangeItemText extends PopupMenuManager.MenuFunction {
        ChangeItemText(final int position, final LogItem item) {
            titleRes = R.string.edit;
            function = new Runnable() {
                @Override
                public void run() {
                    showChangeItemTextDialog(position, item);
                }
            };
        }
    }

    public void showChangeItemTextDialog(final int position, final LogItem item) {
        final boolean wasKeypadShown = UIUtil.keypadShown;

        // Make Content View
        editDialogView = new EditText(main);
        editDialogView.setSingleLine(false);
        editDialogView.setInputType(editDialogView.getInputType() | InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editDialogView.setText(item.getText());
        editDialogView.setSelection(item.getText().length());
        editDialogView.requestFocus();
        //contentView.setImeOptions(EditorInfo.IME_FLAG_NO_EXTRACT_UI);

        // 변경 감지시 시간 기록
        if (PrefUtil.settingSavingTime == 1) {
            PrefUtil.timePreserved = TimeUtil.getCurrentTime();
        }

        // Make Dialog
        AlertDialog dialog = new AlertDialog.Builder(main)
                .setTitle(R.string.edit)
                .setView(editDialogView)
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (!wasKeypadShown) {
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
                            onConfirmChangeItemText(item, position, text);
                        }
                        if (!wasKeypadShown) {
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

    private void onConfirmChangeItemText(LogItem item, final int position, String text) {
        if (PrefUtil.settingEditTime) {
            changeItemTextAndUpdate(position, item, text);
        } else {
            changeItemText(position, item, text);
        }

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

    private void changeItemTextAndUpdate(int position, LogItem item, String text) {
        int originalId = item.getId();
        String originalText = item.getText();
        int originalTime = item.getTime();

        // 원래 아이템 제거
        main.mAdapter.removeItem(position);

        // 데이터베이스
        int newId = main.mAdapter.getNewId();
        if (main.mDatabase.existId(newId)) {
            newId += 1;
        }
        int time = TimeUtil.getSaveTime();
        main.mDatabase.updateId(originalId, newId);
        main.mDatabase.updateTime(newId, time);
        main.mDatabase.updateText(newId, text);

        // 맨 아래에 추가
        item.setId(newId);
        item.setTime(time);
        item.setText(text);
        main.mAdapter.addItem(item);

        // 스크롤 다운
        main.mRecView.postDelayed(new Runnable() {
            @Override
            public void run() {
                main.mRecView.scrollToItemPosition(main.mAdapter.getItemCount() - 1, true);
            }
        }, 100);

        // 되돌리기 TODO
        // 수정해서 밑으로 내려간 항목을 원래 위치로 되돌림 text + id, time, position
        // 변경된 텍스트도 되돌림
        // 되돌리기 후 다시 내리고 텍스트도 변경하는 모드로 변경
        //undoItem.setMode(UNDO_MODE_RESTORE_EDIT_UPDATE);
        //undoItem.setItem(item);
        //undoItem.setText(originalText);
        //undoItem.setId(originalId);
        //undoItem.setTime(originalTime);
        //undoItem.setPosition(position);
        undoItem.clear();
    }

    private void changeItemText(int position, LogItem item, String text) {
        String originalText = item.getText();

        // 메모리 설정
        item.setText(text);

        // 메모리 설정 반영
        main.mAdapter.notifyItemChanged(position);

        // 데이터베이스 저장
        main.mDatabase.updateText(item.getId(), text);

        // 스크롤 효과
        main.mRecView.startBlinkAnimation(position, true);

        // 되돌리기 TODO
        // 수정 전 텍스트로 변경
        // 되돌리기 후 수정 후 텍스트로 다시 변경하는 모드로 변경
        //undoItem.setMode(UNDO_MODE_RESTORE_EDIT);
        //undoItem.setItem(item);
        //undoItem.setText(originalText);
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class CopyText extends PopupMenuManager.MenuFunction {
        CopyText(final String text) {
            titleRes = R.string.copy;
            function = new Runnable() {
                @Override
                public void run() {
                    copyText(text);
                }
            };
        }
    }

    private void copyText(String text) {
        if (text != null) {
            ClipboardManager clipboard = (ClipboardManager) main.getSystemService(Context.CLIPBOARD_SERVICE);
            if (clipboard != null) {
                ClipData clip = ClipData.newPlainText("content", text);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(main, R.string.copy_message, Toast.LENGTH_SHORT).show();
            }
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class ShareText extends PopupMenuManager.MenuFunction {
        ShareText(final String text) {
            titleRes = R.string.share;
            function = new Runnable() {
                @Override
                public void run() {
                    shareText(text);
                }
            };
        }
    }

    private void shareText(String text) {
        if (text != null) {
            Intent intent = new Intent(Intent.ACTION_SEND);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_TEXT, text);
            main.startActivity(Intent.createChooser(intent, main.getString(R.string.share)));
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class HideItem extends PopupMenuManager.MenuFunction {
        HideItem(final int position, final LogItem item) {
            titleRes = R.string.hide;
            function = new Runnable() {
                @Override
                public void run() {
                    hideItem(position, item);
                }
            };
        }
    }

    private void hideItem(int position, LogItem item) {
        int id = item.getId();

        // 기본에서 지움
        main.mAdapter.removeItem(position);
        main.mDatabase.delete(item.getId());

        // 숨김 데이터베이스 오픈
        Database databaseHidden = new Database(main, Database.DATABASE_NAME_HIDDEN);
        DataAdapter adapterHidden = new DataAdapter(main);
        adapterHidden.setItemList(databaseHidden.load());

        // 숨김에서 삽입위치 탐색
        int toTime = item.getTime();
        int[] betweenIds = adapterHidden.timeInsertFindBetween(toTime);

        // 삽입위치, 밀려나는 위치
        int toId = betweenIds[0] + 1;
        int pushUntilId = adapterHidden.timeInsertFindPushUntil(betweenIds);

        // 데이터베이스
        if (pushUntilId == -1) {
            item.setId(toId);
            databaseHidden.insertItem(item);
        } else {
            int pushCount = pushUntilId - toId;
            boolean res = databaseHidden.pushId(toId, pushCount);
            if (res) {
                item.setId(toId);
                databaseHidden.insertItem(item);
            }
        }

        // 되돌리기 TODO
        // item: hidden 으로 이동된 아이템
        // position: 이동 전 position
        // id: 이동 전 id
        // => 이동된 아이템을 hidden 데이터베이스에서 제거 후 default 데이터베이스로 이동하고 원래 id 로 변경해 position 에 삽입한다.
        // 점유시 push
        // 되돌리기 후 다시 보내는 모드로 변경
        //undoItem.setMode(UNDO_MODE_RESTORE_HIDE);
        //undoItem.setPosition(position);
        //undoItem.setItem(item);
        //undoItem.setId(id);
        undoItem.clear();

        // 데이터베이스 닫기
        databaseHidden.close();
    }

    /////////////////////////////////////////////////////////////////////////

    private class RevealItem extends PopupMenuManager.MenuFunction {
        RevealItem(final int position, final LogItem item) {
            titleRes = R.string.reveal;
            function = new Runnable() {
                @Override
                public void run() {
                    revealItem(position, item);
                }
            };
        }
    }

    private void revealItem(int position, LogItem item) {
        int id = item.getId();

        // 기본에서 지움
        main.mAdapter.removeItem(position);
        main.mDatabase.delete(item.getId());

        // 숨김 데이터베이스 오픈
        Database databaseDefault = new Database(main, Database.DATABASE_NAME);
        DataAdapter adapterDefault = new DataAdapter(main);
        adapterDefault.setItemList(databaseDefault.load());

        // 숨김에서 삽입위치 탐색
        int toTime = item.getTime();
        int[] betweenIds = adapterDefault.timeInsertFindBetween(toTime);

        // 삽입위치, 밀려나는 위치
        int toId = betweenIds[0] + 1;
        int pushUntilId = adapterDefault.timeInsertFindPushUntil(betweenIds);

        // 데이터베이스
        if (pushUntilId == -1) {
            item.setId(toId);
            databaseDefault.insertItem(item);
        } else {
            int pushCount = pushUntilId - toId;
            boolean res = databaseDefault.pushId(toId, pushCount);
            if (res) {
                item.setId(toId);
                databaseDefault.insertItem(item);
            }
        }

        // 되돌리기 TODO
        // item: default 로 이동된 아이템
        // position: 이동 전 position
        // id: 이동 전 id
        // => 이동된 아이템을 default 데이터베이스에서 제거 후 hidden 데이터베이스로 이동하고 원래 id 로 변경해 position 에 삽입한다.
        // 점유시 push
        // 되돌리기 후 다시 보내는 모드로 변경
        //undoItem.setMode(UNDO_MODE_RESTORE_REVEAL);
        //undoItem.setPosition(position);
        //undoItem.setItem(item);
        //undoItem.setId(id);
        undoItem.clear();

        // 데이터베이스 닫기
        databaseDefault.close();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class CopyTextInDate extends PopupMenuManager.MenuFunction {
        CopyTextInDate(final int position, final DateItem item) {
            titleRes = R.string.copy_all;
            function = new Runnable() {
                @Override
                public void run() {
                    copyText(getTextInDate(position, item));
                }
            };
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class ShareTextInDate extends PopupMenuManager.MenuFunction {
        ShareTextInDate(final int position, final DateItem item) {
            titleRes = R.string.share_all;
            function = new Runnable() {
                @Override
                public void run() {
                    shareText(getTextInDate(position, item));
                }
            };
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class RemoveItemsInDate extends PopupMenuManager.MenuFunction {
        RemoveItemsInDate(final int position) {
            titleRes = R.string.remove_all;
            function = new Runnable() {
                @Override
                public void run() {
                    removeItemsInDate(position);
                }
            };
        }
    }

    private void removeItemsInDate(final int position) {
        final ArrayList<Integer> idList = main.mAdapter.getIdsInDate(position);
        AlertDialog dialog = new AlertDialog.Builder(main)
                .setTitle(R.string.remove)
                .setMessage(main.getString(R.string.remove_date_message, idList.size()))
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        ArrayList<LogItem> items = main.mAdapter.removeItems(idList);
                        main.mDatabase.delete(idList);

                        // 되돌리기 TODO
                        // 제거한 아이템들을 복원함
                        // 복원 중 id가 겹칠 경우 push
                        // 되돌리기 후 다시 제거하는 모드로 변경
                        //undoItem.setMode(UNDO_MODE_RESTORE_REMOVE_ITEMS);
                        //undoItem.setItems(items);
                    }
                })
                .create();
        dialog.show();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

















































    ////////////////////////////////////////////////////////////////////////////////////////////////
    // 메뉴 관련 함수
    ////////////////////////////////////////////////////////////////////////////////////////////////

    // 시간대: 수
    private static List<Integer> UTC_INT = Arrays.asList(
            -720, -660, -600, -570, -540, -480, -420, -360,
            -300, -240, -210, -180, -150, -120, -60, 0,
            +60, +120, +180, +210, +240, +270, +300, +330,
            +345, +360, +390, +420, +480, +525, +540, +570,
            +600, +630, +660, +690, +720, +765, +780, +825,
            +840);

    // 시간대: 텍스트
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

    private class OpenSetting extends PopupMenuManager.MenuFunction {
        OpenSetting() {
            titleRes = R.string.setting;
            function = new Runnable() {
                @Override
                public void run() {
                    showSettingDialog();
                }
            };
        }
    }

    private void showSettingDialog() {
        final View contentView = LayoutInflater.from(main).inflate(R.layout.setting_layout, main.mRootLayout, false);

        // 타임존
        final Spinner spinnerTimezone = contentView.findViewById(R.id.setting_timezone_spinner);
        ArrayAdapter<CharSequence> adapterTimezone = new ArrayAdapter<>(main, android.R.layout.simple_spinner_item, UTC_STRING);
        adapterTimezone.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerTimezone.setAdapter(adapterTimezone);
        spinnerTimezone.setSelection(UTC_INT.indexOf(PrefUtil.timeZoneOffset));

        // 타임존 버튼
        Button resetTimezoneButton = contentView.findViewById(R.id.setting_timezone_reset);
        resetTimezoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                spinnerTimezone.setSelection(UTC_INT.indexOf(PrefUtil.getDefaultTimeOffset()));
            }
        });

        // 저장 시점
        final Spinner spinnerSaveTime = contentView.findViewById(R.id.setting_saving_time_spinner);
        String[] savingTimeList = main.getResources().getStringArray(R.array.saving_time_list);
        ArrayAdapter<String> adapterSaveTime = new ArrayAdapter<>(main, android.R.layout.simple_spinner_item, savingTimeList);
        adapterSaveTime.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerSaveTime.setAdapter(adapterSaveTime);
        spinnerSaveTime.setSelection(PrefUtil.settingSavingTime);

        // 날짜 형식 버튼
        Button dateFormatChangeButton = contentView.findViewById(R.id.setting_button_date_format_change);
        dateFormatChangeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showDateFormatChangeDialog();
            }
        });

        // 바로가기 설정 버튼
        Button shortcutSettingButton = contentView.findViewById(R.id.setting_button_shortcut_setting);
        shortcutSettingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showShortcutSettingDialog();
            }
        });

        // 수정시 시간 갱신
        final CheckBox checkBoxEditTime = contentView.findViewById(R.id.setting_edit_time);
        checkBoxEditTime.setChecked(PrefUtil.settingEditTime);

        // 화면 보안
        final CheckBox screenSecure = contentView.findViewById(R.id.setting_secure);
        screenSecure.setChecked(PrefUtil.getScreenSecure(main));

        // 키패드 예측
        if (Build.VERSION.SDK_INT >= 19) {
            CheckBox keypadPrediction = contentView.findViewById(R.id.setting_allow_keypad_prediction);
            keypadPrediction.setChecked(PrefUtil.onStartKeypad);
        }

        // 다이얼로그
        AlertDialog dialog = new AlertDialog.Builder(main)
                .setView(contentView)
                .setNegativeButton(R.string.cancel, null)
                .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // 타임존
                        int offset = UTC_INT.get(spinnerTimezone.getSelectedItemPosition());
                        if (offset != PrefUtil.timeZoneOffset) {
                            PrefUtil.setTimeOffset(main, offset);
                            main.mAdapter.setItemList(main.mDatabase.load());
                            main.mAdapter.update(main.isSearchMode);
                            main.mRecView.scrollDown();

                            // todo 시간대 변경하면 item.time이 바뀔 수 있다
                            undoItem.clear();
                        }

                        // 저장 시점
                        int settingSavingTime = spinnerSaveTime.getSelectedItemPosition();
                        if (settingSavingTime != PrefUtil.settingSavingTime) {
                            PrefUtil.setSettingSavingTime(main, settingSavingTime);
                            if (!main.isSearchMode) {
                                if (settingSavingTime == 1) {
                                    main.mInputText.addTimeRecordTextWatcher();
                                } else {
                                    main.mInputText.removeTimeRecordTextWatcher();
                                }
                            }
                        }

                        // 수정시 시간 갱신
                        PrefUtil.setSettingEditTime(main, checkBoxEditTime.isChecked());

                        // 화면 보안
                        PrefUtil.setScreenSecure(main, screenSecure.isChecked());
                        if (screenSecure.isChecked()) {
                            main.getWindow().setFlags(WindowManager.LayoutParams.FLAG_SECURE, WindowManager.LayoutParams.FLAG_SECURE);
                        } else {
                            main.getWindow().clearFlags(WindowManager.LayoutParams.FLAG_SECURE);
                        }

                        // 키패드 예측
                        if (Build.VERSION.SDK_INT >= 19) {
                            CheckBox checkBox = contentView.findViewById(R.id.setting_allow_keypad_prediction);
                            PrefUtil.setOnStartKeypad(main, checkBox.isChecked());
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

    private void showDateFormatChangeDialog() {
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

    private void showShortcutSettingDialog() {
        final View contentView = LayoutInflater.from(main).inflate(R.layout.setting_layout_shortcut_setting, main.mRootLayout, false);

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

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class ExportBackupFile extends PopupMenuManager.MenuFunction {
        ExportBackupFile() {
            titleRes = R.string.backup;
            function = new Runnable() {
                @Override
                public void run() {
                    exportBackupFile();
                }
            };
        }
    }

    private void exportBackupFile() {
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

    private class CreateShortcut extends PopupMenuManager.MenuFunction {
        CreateShortcut() {
            titleRes = R.string.shortcut;
            function = new Runnable() {
                @Override
                public void run() {
                    showCreateShortcutDialog();
                }
            };
        }
    }

    private void showCreateShortcutDialog() {
        // Make Content View
        final EditText editText = new EditText(main);
        editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
        editText.setSingleLine();
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
        if (label == null) {
            return;
        }
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
            Toast.makeText(main, R.string.msg_shortcut_created, Toast.LENGTH_SHORT).show();
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class StartHiddenActivity extends PopupMenuManager.MenuFunction {
        StartHiddenActivity() {
            titleRes = R.string.hidden;
            function = new Runnable() {
                @Override
                public void run() {
                    startHiddenActivity();
                }
            };
        }
    }

    private void startHiddenActivity() {
        main.startActivity(new Intent(main, HiddenActivity.class));
        main.finish();
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////

    private class ChangePassword extends PopupMenuManager.MenuFunction {
        ChangePassword() {
            titleRes = R.string.change_password;
            function = new Runnable() {
                @Override
                public void run() {
                    showChangePasswordDialog();
                }
            };
        }
    }

    private void showChangePasswordDialog() {
        if (HiddenActivity.mode != 2) {
            Toast.makeText(main, R.string.msg_password_first, Toast.LENGTH_SHORT).show();
        } else {
            // Make Content View
            final EditText editText = new EditText(main);
            editText.setInputType(InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
            editText.setSingleLine();
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
                        changePassword(dialog, afterPassword);
                    }
                }
            });

            UIUtil.isPopupEditing = true;
        }
    }

    private void changePassword(final AlertDialog baseDialog, final String afterPassword) {
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
    // 애니메이션
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


}
