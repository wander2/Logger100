package park.haneol.project.logger.component;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import park.haneol.project.logger.R;
import park.haneol.project.logger.item.LogItem;
import park.haneol.project.logger.util.ActivityObserver;
import park.haneol.project.logger.util.Database;
import park.haneol.project.logger.view.ShortcutEditText;

public class ShortcutActivity extends AppCompatActivity {

    public static final String EXTRA_DEFAULT_TEXT = "extra_default_text";

    private ShortcutEditText editText;

    private boolean isSaved = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setFinishOnTouchOutside(false);
        getWindow().clearFlags(WindowManager.LayoutParams.FLAG_DIM_BEHIND);
        getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        setContentView(R.layout.shortcut_layout);
        editText = findViewById(R.id.edit_text);
        String defaultText = getIntent().getStringExtra(EXTRA_DEFAULT_TEXT);
        if (defaultText != null) {
            editText.setText(defaultText);
            editText.setSelection(defaultText.length());
        }
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
        Button negativeButton = findViewById(R.id.negative_button);
        Button positiveButton = findViewById(R.id.positive_button);
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
                if (text.length() > 0) {
                    onSave(text);
                }
            }
        });
    }

    private void onSave(String text) {
        Database database = new Database(this);
        LogItem item = database.insert(text);

        MainActivity activity = ActivityObserver.getInstance().getActivity();
        if (activity != null) {
            activity.onTextSavedFromOutside(item);
        }

        Toast.makeText(this, getString(R.string.saved_message), Toast.LENGTH_SHORT).show();
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
