package park.haneol.project.logger.component;

import android.os.Build;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

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
import park.haneol.project.logger.view.RootLayout;

// todo later: 알림 클릭으로 시작되었을 때는 키보드 안 띄움

public class MainActivity extends AppCompatActivity {

    private RootLayout rootLayout;
    private RecView recView;
    private EditText editText;

    private Database database;
    private DataAdapter adapter;

    private ActionManager actionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        UIUtil.fitCount = 0;
        UIUtil.keypadShown = true;

        if (Build.VERSION.SDK_INT >= 19) {
            getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
        PrefUtil.init(this);
        TimeUtil.init(this);
        ColorUtil.init(this);

        rootLayout = findViewById(R.id.root_layout);
        recView = findViewById(R.id.rec_view);
        editText = findViewById(R.id.edit_text);
        Button saveButton = findViewById(R.id.save_button);
        ImageButton menuButton = findViewById(R.id.menu_button);
        ImageButton themeButton = findViewById(R.id.theme_button);
        ImageButton undoButton = findViewById(R.id.undo_button);
        ColorUtil.applyColor(rootLayout, recView, editText, menuButton, themeButton, undoButton);

        database = new Database(this);
        adapter = (DataAdapter) recView.getAdapter();
        adapter.setItemList(database.load());

        actionManager = new ActionManager(rootLayout, database);
        adapter.setActionManager(actionManager);

        // 복원
        String textPres = PrefUtil.getTextPreserved(this);
        editText.setText(textPres);
        editText.setSelection(textPres.length());
        // 엔터 동작
        editText.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                if (event.getAction() == KeyEvent.ACTION_DOWN && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && !event.isShiftPressed()) {
                    actionManager.onClickSaveButton();
                    return true;
                }
                return false;
            }
        });

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionManager.onClickSaveButton();
            }
        });
        menuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionManager.onClickMenu(v);
            }
        });
        themeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionManager.onClickTheme();
            }
        });
        undoButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                actionManager.onClickUndo();
            }
        });

        ActivityObserver.getInstance().setActivity(this);
    }

    @Override
    protected void onStop() {
        if (Build.VERSION.SDK_INT >= 24 && !UIUtil.isPopupEditing && UIUtil.keypadShown) {
            UIUtil.fitCount = 0; // 초기화
        }
        UIUtil.setKeypadShown(getWindow(), UIUtil.keypadShown);
        UIUtil.predictMargin(rootLayout, false);
        PrefUtil.setTextPreserved(this, editText.getText().toString());
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        database.close();
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
            UIUtil.predictMargin(rootLayout, false);
        }
        super.onRestart();
    }

    // 공유 받음
    void onTextShared(LogItem item) {
        adapter.addItem(item);
        recView.scrollToItemPosition(adapter.getItemCount() - 1);
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        UIUtil.predictMargin(rootLayout, !isInMultiWindowMode);
        UIUtil.keypadShown = !isInMultiWindowMode;
    }
}
