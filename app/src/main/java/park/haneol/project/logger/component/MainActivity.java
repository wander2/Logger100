package park.haneol.project.logger.component;

import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import park.haneol.project.logger.R;
import park.haneol.project.logger.item.LogItem;
import park.haneol.project.logger.recyclerview.DataAdapter;
import park.haneol.project.logger.recyclerview.RecView;
import park.haneol.project.logger.util.ActionManager;
import park.haneol.project.logger.util.ActivityObserver;
import park.haneol.project.logger.util.ColorUtil;
import park.haneol.project.logger.util.Database;
import park.haneol.project.logger.util.PrefUtil;
import park.haneol.project.logger.util.TimeUtil;
import park.haneol.project.logger.util.UIUtil;
import park.haneol.project.logger.view.InputText;
import park.haneol.project.logger.view.RootLayout;
import park.haneol.project.logger.view.SaveButton;

public class MainActivity extends AppCompatActivity {

    private static final String RESTORE_SCROLL_POSITION = "restore_scroll_position";
    private static final String RESTORE_SCROLL_OFFSET = "restore_scroll_offset";
    private static final String RESTORE_EDIT_POSITION = "restore_edit_position";
    private static final String RESTORE_EDIT_DIALOG_TEXT = "restore_edit_dialog_text";
    private static final String RESTORE_EDIT_DIALOG_SELECTION = "restore_edit_dialog_selection";
    private static final String RESTORE_IS_SEARCH_MODE = "restore_is_search_mode";

    public RootLayout mRootLayout;
    public RecView mRecView;
    public InputText mInputText;
    public SaveButton mSaveButton;
    public ImageButton mMenuButton;
    public ImageButton mThemeButton;
    public ImageButton mUndoButton;

    public Database mDatabase;
    public DataAdapter mAdapter;

    public ActionManager mActionManager;

    public boolean isSearchMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null) {
            isSearchMode = savedInstanceState.getBoolean(RESTORE_IS_SEARCH_MODE, false);
            if (isSearchMode) {
                setTheme(R.style.SearchTheme);
            }
        }
        setContentView(R.layout.activity_main);
        if (!UIUtil.isLandscape(this)) {
            UIUtil.fitCount = 0;
            UIUtil.keypadShown = true;
        }
        if (UIUtil.isLandscape(this)) {
            UIUtil.keypadShown = false;
            UIUtil.fitCount = 2;
        }

        // apply before widget initialize
        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        PrefUtil.init(this);
        TimeUtil.init(this);
        ColorUtil.init(this);

        // widget initialize
        mRootLayout = findViewById(R.id.root_layout);
        mRecView = findViewById(R.id.rec_view);
        mInputText = findViewById(R.id.edit_text);
        mSaveButton = findViewById(R.id.save_button);
        mMenuButton = findViewById(R.id.menu_button);
        mThemeButton = findViewById(R.id.theme_button);
        mUndoButton = findViewById(R.id.undo_button);
        if (isSearchMode) {
            mSaveButton.setText(R.string.search);
        }
        if (UIUtil.keypadShown) {
            UIUtil.showSoftInput(mInputText);
        }

        // apply after widget initialize
        ColorUtil.applyColor(this);

        mDatabase = new Database(this);
        mAdapter = mRecView.adapter;
        mAdapter.setItemList(mDatabase.load());
        mAdapter.update(isSearchMode);
        mRecView.scrollDown();

        mActionManager = new ActionManager(this);
        mAdapter.setActionManager(mActionManager);

        // 복원
        String textPres = PrefUtil.getTextPreserved(this);
        mInputText.setText(textPres);
        mInputText.setSelection(textPres.length());

        // 검색 모드인지에 따라서 이벤트 처리
        if (isSearchMode) {
            // 텍스트 내용으로 우선 검색 실행
            mAdapter.search(textPres);
            mRecView.scrollDown();

            // 버튼 -> 다음 검색
            mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    int position = mAdapter.searchNext();
                    if (position != -1) {
                        mRecView.scrollToItemPosition(position);
                    }
                }
            });

            // 엔터 동작 -> 다음 검색
            mInputText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_INSERT) {
                        int position = mAdapter.searchNext();
                        if (position != -1) {
                            mRecView.scrollToItemPosition(position);
                        }
                        return true;
                    }
                    return false;
                }
            });

            // 입력창 변경 -> 변경된 검색 실행
            mInputText.addTextChangedListener(new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    mAdapter.search(s.toString());
                    mRecView.scrollDown();
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });

        } else {
            // 버튼 -> 저장
            mSaveButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    mActionManager.onClickSaveButton();
                }
            });

            // 엔터 동작 -> 저장
            mInputText.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {
                    if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_INSERT) {
                        mActionManager.onClickSaveButton();
                        return true;
                    }
                    return false;
                }
            });
        }

        // 검색 모드 토글
        mSaveButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                isSearchMode = !isSearchMode;
                recreate();
                return true;
            }
        });
        mMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionManager.onClickMenu(v);
            }
        });
        mThemeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionManager.onClickTheme();
            }
        });
        mUndoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mActionManager.onClickUndo();
            }
        });

        ActivityObserver.getInstance().setActivity(this);

        if (savedInstanceState != null) {
            final int resPosition = savedInstanceState.getInt(RESTORE_SCROLL_POSITION);
            final int resOffset = savedInstanceState.getInt(RESTORE_SCROLL_OFFSET);
            mRecView.post(new Runnable() {
                @Override
                public void run() {
                    mRecView.layoutManager.scrollToPositionWithOffset(resPosition, mRecView.getHeight()-resOffset);
                }
            });
            int editPosition = savedInstanceState.getInt(RESTORE_EDIT_POSITION);
            if (editPosition != -1) {
                String editDialogText = savedInstanceState.getString(RESTORE_EDIT_DIALOG_TEXT);
                int editDialogSelection = savedInstanceState.getInt(RESTORE_EDIT_DIALOG_SELECTION);
                if (editDialogText != null) {
                    mActionManager.onClickEdit(editPosition);
                    mActionManager.editDialogView.setText(editDialogText);
                    mActionManager.editDialogView.setSelection(editDialogSelection);
                }
            }
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        UIUtil.keypadShown = false;
        UIUtil.fitCount = 2;
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        int position = mRecView.layoutManager.findLastVisibleItemPosition();
        outState.putInt(RESTORE_SCROLL_POSITION, position);
        View view = mRecView.layoutManager.findViewByPosition(position);
        if (view != null) {
            outState.putInt(RESTORE_SCROLL_OFFSET, mRecView.getHeight()-view.getTop());
        }
        if (mActionManager.editDialogView != null) {
            outState.putInt(RESTORE_EDIT_POSITION, mActionManager.editPosition);
            outState.putString(RESTORE_EDIT_DIALOG_TEXT, mActionManager.editDialogView.getText().toString());
            outState.putInt(RESTORE_EDIT_DIALOG_SELECTION, mActionManager.editDialogView.getSelectionEnd());
        }
        outState.putBoolean(RESTORE_IS_SEARCH_MODE, isSearchMode);
    }

    @Override
    protected void onStop() {
        if (!UIUtil.isLandscape(this)) {
            if (Build.VERSION.SDK_INT >= 24 && !UIUtil.isPopupEditing && UIUtil.keypadShown) {
                UIUtil.fitCount = 0; // 초기화
            }
            UIUtil.setKeypadShown(getWindow(), UIUtil.keypadShown);
            UIUtil.predictMargin(mRootLayout, false);
        }
        Editable text = mInputText.getText();
        if (text != null) {
            PrefUtil.setTextPreserved(this, text.toString());
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        mDatabase.close();
        super.onDestroy();
    }

    @Override
    protected void onResume() {
        if (Build.VERSION.SDK_INT >= 24 && isInMultiWindowMode()) {
            UIUtil.fitCount = 1;
        }
        super.onResume();
    }

    @Override
    protected void onRestart() {
        if (UIUtil.fitCount == 0) {
            UIUtil.fitCount = 1;
            UIUtil.predictMargin(mRootLayout, false);
        }
        super.onRestart();
    }

    // 공유 받음
    void onTextShared(LogItem item) {
        mAdapter.addItem(item);
        mRecView.scrollToItemPosition(mAdapter.getItemCount() - 1);
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (!UIUtil.isLandscape(this)) {
            UIUtil.predictMargin(mRootLayout, false);
            UIUtil.keypadShown = false;
        }
    }

}
