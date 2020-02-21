package park.haneol.project.logger.component;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import park.haneol.project.logger.R;
import park.haneol.project.logger.item.LogItem;
import park.haneol.project.logger.util.ActivityObserver;
import park.haneol.project.logger.util.Database;
import park.haneol.project.logger.util.PrefUtil;
import park.haneol.project.logger.util.TimeUtil;
import park.haneol.project.logger.view.ShortcutEditText;

public class ShortcutActivity extends AppCompatActivity {

    public static final String EXTRA_SHORTCUT_LABEL = "extra_shortcut_label";

    private ShortcutEditText editText;
    private String label;

    private boolean isSaved = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // 바깥 터치 불가
        setFinishOnTouchOutside(false);

        // 바깥 흐려짐 없앰
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        // 키보드 올림
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);

        // 라벨 타이틀
        label = getIntent().getStringExtra(EXTRA_SHORTCUT_LABEL);
        if (label != null && label.length() > 0) {
            setTitle(label);
        } else {
            setTitle(R.string.shortcut_name);
        }

        // 뷰 설정
        setContentView(R.layout.shortcut_layout);
        editText = findViewById(R.id.edit_text);
        Button neutralButton = findViewById(R.id.neutral_button);
        Button negativeButton = findViewById(R.id.negative_button);
        Button positiveButton = findViewById(R.id.positive_button);

        // 백 버튼 동작
        editText.setListener(new ShortcutEditText.Listener() {
            @Override
            public void onBackPressed() {
                String text = editText.getString();
                if (text.length() > 0) {
                    onSave(text);
                } else {
                    isSaved = false;
                    finish();
                }
            }
        });

        // (저장시점 설정시) 입력창 변경 -> 길이가 0이면 저장시간 초기화
        if (PrefUtil.getSettingSavingTime(this) == 1) {
            editText.addTextChangedListener(new TextWatcher() {
                int c = 0;
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if (c != s.length()) {
                        if (c == 0) {
                            // 시간 저장
                            PrefUtil.timePreserved = TimeUtil.getCurrentTime();
                        }
                        c = s.length();
                    }
                }
                @Override
                public void afterTextChanged(Editable s) {}
            });
        }

        // 버튼 동작
        neutralButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getString();
                onSearch(text);
            }
        });
        negativeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                isSaved = false;
                finish();
            }
        });
        positiveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getString();
                onSave(text);
            }
        });
    }

    private void onSave(String text) {
        if (text.length() == 0) {
            text = " ";
        }
        if (label != null && label.length() > 0) {
            text = PrefUtil.getLabelSeparatorLeft(this) + label + PrefUtil.getLabelSeparatorRight(this) + text;
        }

        Database database = new Database(this, Database.DATABASE_NAME);
        LogItem item = database.append(text, TimeUtil.getSaveTime());

        MainActivity activity = ActivityObserver.getInstance().getActivity();
        if (activity != null) {
            activity.onTextSavedFromOutside(item);
        }

        Toast.makeText(this, getString(R.string.saved_message), Toast.LENGTH_SHORT).show();
        finish();
    }

    private void onSearch(String text) {
        String searchText = "";
        if (label != null && label.length() > 0) {
            searchText = label;
        }
        if (text.trim().length() > 0) {
            searchText = text.trim();
        }
        Intent intent = new Intent(ShortcutActivity.this, MainActivity.class);
        intent.setAction(Intent.ACTION_VIEW);
        intent.putExtra(MainActivity.EXTRA_SEARCH_INTENT, true);
        intent.putExtra(MainActivity.EXTRA_SEARCH_INTENT_TEXT, searchText);

        MainActivity activity = ActivityObserver.getInstance().getActivity();
        if (activity != null) {
            activity.finish();
        }
        startActivity(intent);
        finish();
    }

    // PreIme 가 안 듣는 경우 예비용
    @Override
    public void onBackPressed() {
        String text = editText.getString();
        if (text.length() > 0) {
            onSave(text);
        } else {
            isSaved = false;
        }
        super.onBackPressed();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (isFinishing()){
            if (isSaved) {
                overridePendingTransition(0, R.anim.slide_out_top);
            } else {
                overridePendingTransition(0, R.anim.slide_out_bottom);
            }
        }
    }

}
