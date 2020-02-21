package park.haneol.project.logger.view;

import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatEditText;

import park.haneol.project.logger.util.PrefUtil;
import park.haneol.project.logger.util.TimeUtil;

public class InputText extends AppCompatEditText {

    private TextWatcher timeRecordTextWatcher = null;

    public InputText(Context context) {
        super(context);
    }

    public InputText(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public InputText(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public void addTimeRecordTextWatcher() {
        timeRecordTextWatcher = new TextWatcher() {
            int c = PrefUtil.getTextPreserved(getContext()).length();
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (c != s.length()) {
                    if (c == 0) {
                        // 시간 저장
                        PrefUtil.setTimePreserved(getContext(), TimeUtil.getCurrentTime());
                    }
                    c = s.length();
                }
            }
            @Override
            public void afterTextChanged(Editable s) {}
        };
        addTextChangedListener(timeRecordTextWatcher);
    }

    public void removeTimeRecordTextWatcher() {
        removeTextChangedListener(timeRecordTextWatcher);
        timeRecordTextWatcher = null;
    }

}
